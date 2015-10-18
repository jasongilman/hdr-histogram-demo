(ns perf-tester.core
  (:require [clj-http.client :as h])
  (:import org.HdrHistogram.Histogram
           java.io.PrintStream))

(defn- create-histogram
  ^Histogram []
  (Histogram.
   ;; highest trackable value. We're concerned with the ms level only
   ;; Track up to 1 hours
   (* 1 3600 1000)

   ;; Number of significant digits
   3))

(defn measure-http-request
  [^Histogram hdr expected-start-time url]
  (h/get url)
  (.recordValue hdr (- (System/currentTimeMillis) expected-start-time)))

; expected-start-time = test-start-time + req-num * interval
; latency = end-time - expected-start-time
(defn perform-run
  [{:keys [num-requests interval]}]
  (let [h (create-histogram)
        start-time (System/currentTimeMillis)]
    (doseq
     [n (range num-requests)
      :let [expected-start-time (+ start-time (* n interval))
            ;; Figure out if we're ahead of schedule
            ahead-by (- expected-start-time (System/currentTimeMillis))]]
     (do
       (if (> ahead-by 0)
         (do
          ;  (println "Ahead by" ahead-by ". sleeping")
           (Thread/sleep ahead-by))
         #_(println "Behind by" ahead-by))

       (measure-http-request
        h
        expected-start-time
        "http://localhost:3000/requests/medium")))
    h))

(def default-options
  {:num-threads 1
   :run-name "results"
   :num-requests 1000
   ;; Time between requests in ms.
   :interval 30})

(defn reset-server-statistics
  []
  (h/post "http://localhost:3000/metrics/reset"))

(defn get-server-side-results
  []
  (:body (h/get "http://localhost:3000/metrics/percentile-distribution")))

(defn run-test
  ([]
   (run-test nil))
  ([options]
   (reset-server-statistics)
   (let [{:keys [num-threads run-name] :as options} (merge default-options
                                                              options)
         futures (doall (for [n (range num-threads)]
                          (future (perform-run options))))
         hdr (reduce (fn [^Histogram hdr ^Histogram hdr2]
                       (.add hdr hdr2)
                       hdr)
                     (map deref futures))
         server-results-file (str "hdr_" run-name "_server.hgrm")
         test-results-file (str "hdr_" run-name "_test.hgrm")]
     (.outputPercentileDistribution hdr (PrintStream. test-results-file) 1.0)
     (spit server-results-file (get-server-side-results))
     (println "Results written to file" test-results-file
              "and" server-results-file))))



(comment

 ;; Simulate
 ;; 2 concurrent requests every 1 ms - total time 1000 ms

 (def test-future
   (future (run-test {:num-threads 1 :num-requests 200 :interval 20
                      :run-name "1_20"})))

 (def test-future
   (future (run-test {:num-threads 10 :num-requests 500 :interval 20
                      :run-name "10_20"})))

 (def test-future
   (future (run-test {:num-threads 10 :num-requests 500 :interval 30
                      :run-name "10_30"})))

 (perform-run default-options)



 )
