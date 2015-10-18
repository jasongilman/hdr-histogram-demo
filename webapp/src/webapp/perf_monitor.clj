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
  `(let [cmd-req-ch# (:cmd-req-ch ~pm)
         start# (System/currentTimeMillis)
         result# (do ~@body)]
     (a/>!! cmd-req-ch# {:cmd :record-latency
                         :latency (- (System/currentTimeMillis) start#)})
     result#))

(defn get-percentile-distribution
  "Returns the percentile distribution as a string from the histogram"
  [{:keys [cmd-req-ch]}]
  (let [response-ch (a/chan)]
    (a/>!! cmd-req-ch {:cmd :get-percentile-distribution
                       :response-ch response-ch})
    (a/<!! response-ch)))

(defn reset
  "Clears any recorded latencies"
  [{:keys [cmd-req-ch]}]
  (a/>!! cmd-req-ch {:cmd :reset}))

(defmulti process-request
  (fn [histogram request]
    (:cmd request)))

(defmethod process-request :record-latency
  [^Histogram histogram {:keys [^long latency]}]
  (.recordValue histogram latency))

(defmethod process-request :get-percentile-distribution
  [^Histogram histogram {:keys [response-ch]}]
  (let [baos (ByteArrayOutputStream.)
        _ (.outputPercentileDistribution
           histogram (PrintStream. baos) 1.0)
        s (.toString baos "UTF-8")]
    (a/>!! response-ch s)))

(defmethod process-request :reset
  [^Histogram histogram _]
  (.reset histogram))

(defn- start-monitor-thread
  [{:keys [cmd-req-ch histogram]}]
  (println "Starting monitor thread")
  (a/thread
   (try
     (loop []
       (when-let [request (a/<!! cmd-req-ch)]
         (process-request histogram request)
         (recur)))
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
   ;; A channel of requests of different kinds of commands
   cmd-req-ch

   ;; The channel returned for the thread listening on the other channels.
   monitor-thread-ch

   ;; The hdr histogram
   histogram

   ]
  c/Lifecycle
  (start
    [this]
    (let [this (-> this
                   (assoc :cmd-req-ch (a/chan 10))
                   (assoc :histogram (create-histogram)))
          monitor-thread-ch (start-monitor-thread this)]
      (assoc this :monitor-thread-ch monitor-thread-ch)))

  (stop
    [this]
    (a/close! cmd-req-ch)
    ;; Wait for thread to finish
    (a/<!! monitor-thread-ch)
    (assoc this
           :cmd-req-ch nil
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
