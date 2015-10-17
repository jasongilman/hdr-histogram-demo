(ns webapp.api.routes
  (:require [compojure.route :as route]
            [compojure.core :refer :all]
            [webapp.perf-monitor :as p]))

(defn perform-request
  [pm ms]
  (p/measure
   pm
   (Thread/sleep ms))
  {:status 200})


(defn define-routes
  [pm]
  (routes
   (context "/requests" []
            (GET "/slow" []
                 (perform-request pm 1000))
            (GET "/medium" []
                 (perform-request pm 100))
            (GET "/fast" []
                 (perform-request pm 1)))
   (GET "/percentile-distribution" []
        {:status 200
         :body (p/get-percentile-distribution pm)})
   (route/not-found "Not Found")))
