(ns governance.routes.crud
  (:require [reitit.coercion.spec]
            [reitit.ring.coercion :as coercion]
            [governance.models :refer [models]]
            [governance.middleware :as middleware]
            [toucan.db :as db]
            [clojure.spec.alpha :as s]))

(defonce *_t (atom nil))
(defn crud-routes
  []
  (concat
    ["/api"
     {:coercion   reitit.coercion.spec/coercion
      :middleware [middleware/wrap-csrf
                   middleware/wrap-formats
                   coercion/coerce-response-middleware
                   coercion/coerce-request-middleware]}
     (->> models
          (map (fn [{spec :spec
                     :as  model}]
                 (let [route-name (name spec)]
                   [(str "/" route-name)
                    {:name spec
                     :get  {:summary    (format "Get a %s by id or other fields" route-name)
                            :responses  {200 {:body (s/coll-of spec)}}
                            :parameters {
                                         ;; TODO WHY are these getting checked as vectors?
                                         :params (s/nilable (:query-spec model))
                                         :query  (s/nilable (:query-spec model))
                                         }
                            ;; TODO move this out
                            :handler    (fn [request]
                                          (reset! *_t request)
                                          {:status 200
                                           :body   (apply
                                                     db/select
                                                     (:toucan-model model)
                                                     (-> request :params vec flatten))})}}]))))]))
