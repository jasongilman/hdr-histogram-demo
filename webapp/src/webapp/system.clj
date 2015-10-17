(ns webapp.system
  (:require [com.stuartsierra.component :as c]
            [webapp.api.jetty-server :as jetty]
            [webapp.api.routes :as routes]))

(def PORT 3000)

(defn create-system
  []
  (c/system-map
    :api (jetty/create-web-api PORT routes/define-routes)))
