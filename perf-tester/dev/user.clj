(ns user
  (:require [clojure.tools.namespace.repl :as tnr]))

(defn reset []
  (tnr/refresh))

(println "user.clj loaded.")
