(ns user
  (:require [clojure.pprint :refer (pprint pp)]
            [clojure.test :refer (run-all-tests)]
            [clojure.tools.namespace.repl :as tnr]
            [clojure.repl :refer :all]
            [com.stuartsierra.component :as c]
            [webapp.system :as s]))


(def system nil)

(defn start []
  (let [the-system (s/create-system)]
    (alter-var-root #'system
                    (constantly (c/start the-system))))
  nil)

(defn stop []
  (alter-var-root #'system #(when % (c/stop %)))
  nil)

(defn reset []
  (stop)
  (tnr/refresh :after 'user/start))

(println "user.clj loaded.")
