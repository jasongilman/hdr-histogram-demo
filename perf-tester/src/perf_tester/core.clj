(ns perf-tester.core
  "Runs a performance test of the web app."
  (:require [clj-http.client :as h])
  (:import org.HdrHistogram.Histogram
           java.io.PrintStream))

(defn- create-histogram
  "Creates an instance of the HDR Histogram"
  ^Histogram []
  (Histogram.
   ;; highest trackable value. We're concerned with the ms level only
   ;; Track up to 1 hours
   (* 1 3600 1000)

   ;; Number of significant digits
   3))

(defn measure-http-request
  "Fetches the given URL and records the latency. Expected start time is the
  time the request was expected to start to avoid coordinated omission."
  [^Histogram hdr expected-start-time url]
  (h/get url)
  (.recordValue hdr (- (System/currentTimeMillis) expected-start-time)))

(defn perform-run
  "Runs a test on the current thread. Makes the given number of requests trying
  to send a request every interval milliseconds."
  [{:keys [num-requests interval]}]
  (let [h (create-histogram)
        ;; Capture the time this run started
        start-time (System/currentTimeMillis)]
    (doseq
     [n (range num-requests)
      ;; Calculate the expected start time for this request. It will have fallen
      ;; behind if earlier requests took more than interval ms.
      :let [expected-start-time (+ start-time (* n interval))
            ;; Determine how long we need to wait to make the request.
            ahead-by (- expected-start-time (System/currentTimeMillis))]]
     (do
       ;; Wait until it's time to make the request.
       (when (> ahead-by 0)
         (Thread/sleep ahead-by))

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
 ;; Examples of running tests.
 ;; These can be evaluated in the REPL to kick off a test.

 (def test-future
   (future (run-test {:num-threads 1 :num-requests 200 :interval 20
                      :run-name "1_20"})))

 (def test-future
   (future (run-test {:num-threads 10 :num-requests 500 :interval 20
                      :run-name "10_20"})))

 (def test-future
   (future (run-test {:num-threads 10 :num-requests 500 :interval 30
                      :run-name "10_30"})))



 )
