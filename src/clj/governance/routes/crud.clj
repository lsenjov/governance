(ns governance.routes.crud
  (:require [reitit.coercion.spec]
            [reitit.ring.coercion :as coercion]
            [governance.models :refer [models]]
            [governance.middleware :as middleware]
            [honeysql.helpers :as sqlh]
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
                   {:name   spec
                    :get    {:summary    (format "GET one or more %s" route-name)
                             :responses  {200 {:body (s/coll-of spec)}}
                             :parameters {
                                          ;; TODO WHY are these getting checked as vectors?
                                          :params (s/nilable (:query-spec model))
                                          :query  (s/nilable (:query-spec model))}
                             ;; TODO move this out
                             :handler    (fn [request]
                                           (reset! *_t request)
                                           {:status   200
                                            :body     ((-> model :crud :select)
                                                       (cond->
                                                         {:select [:*]
                                                          :from   (symbol (name spec))}

                                                         ;; Got parameters? We only check for equality atm
                                                         (-> request :params count pos?)
                                                         (sqlh/where
                                                           [:and (map (fn [[k v]] [:= k v])
                                                                      (:params request))])))})}
                    :post   {:summary    (format "Create a %s" route-name)
                             :responses  {200 {:body spec}}
                             :parameters {:body spec}
                             :handler    (fn [request]
                                           (reset! *_t request)
                                           {:status 200
                                            :body   (
                                                     (constantly nil)
                                                     (:toucan-model model)
                                                     (:params request))})}
                    :put    {:summary    (format "Update one of %s" route-name)
                             :responses  {:200 {:body spec}}
                             :parameters {:body spec}
                             :handler    (fn [request]
                                           {:status 200
                                            :body   {:success (
                                                               (constantly nil)
                                                               (:toucan-model model)
                                                               (:id model)
                                                               (:params request))}})}
                    :delete {:summary    (format "Delete one of %s" route-name)
                             :responses  {:200 {}}
                             :parameters {}
                             :handler    (fn [request]
                                           ;; For gods sake, don't let us delete _everything_ in the database in one go
                                           (assert (pos? (count (:params request))))
                                           {:status 200
                                            :body   (
                                                     (constantly nil)
                                                     (:toucan-model model)
                                                     (-> request :params vec flatten))})}}]))))))
