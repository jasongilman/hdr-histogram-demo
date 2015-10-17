(defproject webapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/jasongilman/hdr-histogram-demo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.0"]
                 [ring "1.4.0"]
                 [compojure "1.4.0"]
                 [org.hdrhistogram/HdrHistogram "2.1.7"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :profiles {:dev {:source-paths ["dev" "src" "test"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]]}
   ;; TODO specify main
  ;  :uberjar {:main mm.message-store.main
  ;            :aot :all}
   })
