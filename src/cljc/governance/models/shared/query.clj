(ns governance.models.shared.query
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as sqlh]
            [governance.db.core :refer [*db*]]
            [clojure.spec.alpha :as s])
  (:import (java.util UUID)
           (java.sql Timestamp))
  (:gen-class))

(defn default-queries
  "The very very default base queries that are executed.
  To be wrapped in middleware"
  [table-name]
  {:select (fn [query]
             (jdbc/query
               {:datasource *db*}
               (sql/format query)))
   :insert (fn [query]
             (jdbc/execute!
               {:datasource *db*}
               (-> query
                   (sqlh/insert-into table-name)
                   (sql/format)))
             (:values query)
             ;(-> query
             ;    (sqlh/insert-into table-name)
             ;    (sql/format))
             )})

(defn wrap-id-inner
  [handler]
  (fn [query]
    (-> query
        (update :values (fn [values]
                          (map (fn [row] (update row :id #(or % (UUID/randomUUID))))
                               values)))
        handler)))
(defn wrap-id
  [handlers _]
  (update handlers :insert wrap-id-inner))

(defn wrap-timestamped
  [handlers _]
  (println "handlers:")
  (println handlers)
  (-> handlers
      (update :insert
              (fn [handler]
                (fn [query]
                  (handler
                    (let [now (Timestamp. (System/currentTimeMillis))]
                      (update query :values
                              (fn [values]
                                (map (fn [row] (assoc row :created_at now, :updated_at now))
                                     values))))))))
      (update :update
              (fn [handler]
                (fn [query]
                  (handler
                    (let [now (Timestamp. (System/currentTimeMillis))]
                      (update query :values
                              (fn [values]
                                (map (fn [row] (assoc row :updated_at now))
                                     values))))))))))

(defn wrap-validate
  "Wraps insert and update to ensure they're valid going into the database.
  Doesn't wrap select, because we assume they're good, or are partial requests"
  [handlers spec]
  (-> handlers
      (update :insert
              (fn [handler]
                (fn [query]
                  (mapv (fn [obj] (assert (s/valid? spec obj) (s/explain spec obj)))
                        (:values query))
                  (handler query))))
      (update :update
              (fn [handler]
                (fn [query]
                  (mapv (fn [obj] (assert (s/valid? spec obj) (s/explain spec obj)))
                        (:values query))
                  (handler query))))))

(defn build-queries-map
  [{:keys [spec properties] :as ks}]
  (println "ks:")
  (println ks)
  (cond-> (default-queries (keyword (name spec)))

          (:validate properties)
          (wrap-validate spec)

          (:id properties)
          (wrap-id nil)

          (:timestamped? properties)
          (wrap-timestamped nil)))


;(add-property! :timestamped?
;               :insert (fn [obj _]
;                         (let [now (Timestamp. (System/currentTimeMillis))]
;                           (assoc obj :created_at now, :updated_at now)))
;               :update (fn [obj _]
;                         (assoc obj :updated_at (Timestamp. (System/currentTimeMillis)))))
;(add-property! :id
;               :insert (fn [obj _]
;                         (update obj :id #(or % (str (UUID/randomUUID))))))
;(add-property! :validate
;               :insert (fn [obj spec]
;                         (assert (s/valid? spec obj) (s/explain spec obj))
;                         obj)
;               :update (fn [obj spec]
;                         (assert (s/valid? spec obj) (s/explain spec obj))
;                         obj))