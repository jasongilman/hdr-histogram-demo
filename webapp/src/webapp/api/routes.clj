(ns webapp.api.routes
  (:require [compojure.route :as route]
            [compojure.core :refer :all]))

(defn perform-request
  [ms]
  (Thread/sleep ms)
  {:status 200})


(defn define-routes
  [app]
  (routes
   (context "/requests" []
            (GET "/slow" {:keys [params]}
                 (perform-request 1000))
            (GET "/medium" {:keys [params]}
                 (perform-request 100))
            (GET "/fast" {:keys [params]}
                 (perform-request 1)))
   (route/not-found "Not Found")))
