(ns governance.routes.crud
  (:require [reitit.coercion.spec]
            [reitit.ring.coercion :as coercion]
            [governance.models :refer [models]]
            [governance.middleware :as middleware]
            [toucan.db :as db]
            [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

(defonce *_t (atom nil))
(defn crud-routes
  []
  (concat
    ["/crud"
     {:coercion   reitit.coercion.spec/coercion
      :middleware [middleware/wrap-formats
                   coercion/coerce-response-middleware
                   coercion/coerce-request-middleware]
      :swagger    {:info {:title "Crud API"}}}]
    (->> models
         (map (fn [{spec :spec
                    :as  model}]
                (let [route-name (name spec)]
                  [(str "/" route-name)
                   {:name    spec
                    :get     {:summary (format "GET one or more %s" route-name)
                              :responses  {200 {:body (s/coll-of spec)}}
                              :parameters {
                                           ;; TODO WHY are these getting checked as vectors?
                                           :params (s/nilable (:query-spec model))
                                           :query  (s/nilable (:query-spec model))}
                              ;; TODO move this out
                              :handler    (fn [request]
                                            (reset! *_t request)
                                            {:status 200
                                             :body   (apply
                                                       db/select
                                                       (:toucan-model model)
                                                       (-> request :params vec flatten))})}
                    :post {:summary (format "Create one of %s" route-name)
                           :responses {200 {:body spec}}
                           :parameters {:body spec}
                           :handler (fn [request]
                                      (reset! *_t request)
                                      {:status 200
                                       :body (db/insert!
                                               (:toucan-model model)
                                               (:params request))}
                                      )}}]))))))
