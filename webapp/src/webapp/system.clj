(ns webapp.system
  (:require [com.stuartsierra.component :as c]
            [webapp.api.jetty-server :as jetty]
            [webapp.api.routes :as routes]
            [webapp.perf-monitor :as pm]))

(def PORT 3000)

(defn create-system
  []
  (c/system-map
    :api (c/using (jetty/create-web-api PORT routes/define-routes)
                  {:app :perf-monitor})
    :perf-monitor (pm/create-performance-monitor)))
