(defproject perf-tester "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.hdrhistogram/HdrHistogram "2.1.7"]
                 [clj-http "2.0.0"]]

  :profiles {:dev {:source-paths ["dev" "src" "test"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]]}
   ;; TODO specify main
  ;  :uberjar {:main mm.message-store.main
  ;            :aot :all}
   })
