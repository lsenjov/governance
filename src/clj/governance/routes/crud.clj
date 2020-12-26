(ns governance.routes.crud
  (:require [reitit.coercion.schema]
            [reitit.ring.coercion :as coercion]
            [governance.models :refer [models]]
            [governance.middleware :as middleware]
            [governance.models.shared.schemas :refer [get-schema]]
            [honeysql.helpers :as sqlh]
            [clojure.spec.alpha :as s]
            [schema.core :as sc]
            [spec-tools.core :as st]))

(defonce *_t (atom nil))
(defonce *_m (atom nil))
(defn crud-routes
  []
  (concat
    ["/crud"
     {:coercion   reitit.coercion.schema/coercion
      :middleware [middleware/wrap-formats
                   coercion/coerce-exceptions-middleware
                   coercion/coerce-request-middleware
                   coercion/coerce-response-middleware]
      :swagger    {:info {:title "Crud API"}}}]
    (->> (models)
         vals
         (map (fn [{spec   :spec
                    schema :schema
                    :as    model}]
                (let [route-name (name spec)]
                  [(str "/" route-name)
                   {:name    spec
                    :swagger {:tags [(name spec)]}
                    :get     {:summary    (format "GET one or more %s" route-name)
                              :responses  {200 {:body [schema]}}
                              :parameters {:params (sc/maybe (:query model))
                                           :query  (sc/maybe (:query model))}
                              ;; TODO move this out
                              :handler    (fn [request]
                                            (reset! *_t request)
                                            (reset! *_m model)
                                            {:status 200
                                             :body   ((-> model :crud :select)
                                                      (cond->
                                                        {:select [:*]
                                                         :from   [(symbol (name spec))]}

                                                        ;; Got parameters? We only check for equality atm
                                                        (-> request :parameters :query count pos?)
                                                        (sqlh/where
                                                          (concat
                                                            [:and]
                                                            (map (fn [[k v]] [:= k v])
                                                                 (-> request :parameters :query))))))})}
                    :post    {:summary    (format "Create a %s" route-name)
                              :responses  {200 {:body (:collection model)}}
                              :parameters {:body (:collection model)}
                              :handler    (fn [request]
                                            (reset! *_t request)
                                            (reset! *_m model)
                                            {:status 200
                                             :body   ((-> model :crud :insert)
                                                      {:values (get-in request [:parameters :body])})})}
                    :put     {:summary    (format "Update one of %s" route-name)
                              ;; TODO figure out exactly what we're returning
                              :responses  {:200 {:body {:success [sc/Int]}}}
                              :parameters {:body (:query model)}
                              :handler    (fn [request]
                                            (let [params (get-in request [:parameters :body])]
                                              (reset! *_t request)
                                              (reset! *_m model)
                                              {:status 200
                                               :body   {:success ((-> model :crud :update)
                                                                  (->
                                                                    (sqlh/sset (dissoc params :id))
                                                                    (sqlh/where [:= :id (:id params)])))}}))}
                    :delete  {:summary (format "Delete one of %s" route-name)
                              :responses  {:200 {:success [sc/Int]}}
                              :parameters {:query {:id sc/Uuid}}
                              :handler (fn [request]
                                         (reset! *_t request)
                                         (reset! *_m model)
                                         {:status 200
                                          :body   {:success
                                                   ((-> model :crud :delete)
                                                    (sqlh/where [:= :id (-> request :parameters :query :id)]))}})}}]))))))
