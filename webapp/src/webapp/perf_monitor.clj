(ns webapp.perf-monitor
  (:require [com.stuartsierra.component :as c]
            [clojure.core.async :as a])
  (:import org.HdrHistogram.Histogram
           [java.io
            ByteArrayOutputStream
            PrintStream]))

(defmacro measure
  "Measures the performance of the body in the current histogram and returns the
  result of the body."
  [pm & body]
  `(let [latency-ch# (:latency-ch ~pm)
         start# (System/currentTimeMillis)
         result# (do ~@body)]
     (a/>!! latency-ch# (- (System/currentTimeMillis) start#))
     result#))

(defn get-percentile-distribution
  "Returns the percentile distribution as a string from the histogram"
  [{:keys [histogram-req-ch]}]
  (let [read-ch (a/chan)]
    (a/>!! histogram-req-ch read-ch)
    (a/<!! read-ch)))

(defn- start-monitor-thread
  [{:keys [latency-ch histogram-req-ch ^Histogram histogram]}]
  (println "Starting monitor thread")
  (a/thread
   (try
     (loop []
       (let [[val read-ch] (a/alts!! [latency-ch histogram-req-ch])]
         (when (and val read-ch)
           (condp = read-ch
             latency-ch
             (.recordValue histogram ^long val)

             histogram-req-ch
             (let [baos (ByteArrayOutputStream.)
                   _ (.outputPercentileDistribution
                      histogram (PrintStream. baos) 1.0)
                   s (.toString baos "UTF-8")]
               (a/>!! val s))

             (println "Unrecognized channel use to read value:" (pr-str val)))
           (recur))))
     (catch Exception e
       (println "Unexpected exception from monitor thread")
       (.printStackTrace e)))
   (println "Stopping monitor thread")))

(defn- create-histogram
  []
  (Histogram.
   ;; highest trackable value. We're concerned with the ms level only
   ;; Track up to 1 hours
   (* 1 3600 1000)

   ;; Number of significant digits
   3))

(defrecord PerformanceMonitor
  [
   ;; A channel containing latency measurements to record
   latency-ch

   ;; A channel of requests to get the current histogram numbers
   histogram-req-ch

   ;; The channel returned for the thread listening on the other channels.
   monitor-thread-ch

   ;; The hdr histogram
   histogram

   ]
  c/Lifecycle
  (start
    [this]
    (let [this (-> this
                   (assoc :latency-ch (a/chan 10))
                   (assoc :histogram-req-ch (a/chan 1))
                   (assoc :histogram (create-histogram)))
          monitor-thread-ch (start-monitor-thread this)]
      (assoc this :monitor-thread-ch monitor-thread-ch)))

  (stop
    [this]
    (a/close! latency-ch)
    (a/close! histogram-req-ch)
    ;; Wait for thread to finish
    (a/<!! monitor-thread-ch)
    (assoc this
           :latency-ch nil
           :histogram-req-ch nil
           :monitor-thread-ch nil
           :histogram nil)))

(defn create-performance-monitor
  []
  (map->PerformanceMonitor {}))


(comment

 (def pm (c/start (create-performance-monitor)))

 (c/stop pm)

 (measure pm (Thread/sleep 100))

 (println (get-percentile-distribution pm))



 )
