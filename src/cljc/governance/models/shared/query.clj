(ns governance.models.shared.query
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [honeysql.core :as sql]
            [honeysql.helpers :as sqlh]
            [governance.db.core :refer [*db*]]
            [governance.models.shared.schemas :refer [get-schema]]
            [schema.core :as sc]
            [clojure.spec.alpha :as s])
  (:import (java.util UUID)
           (java.sql Timestamp))
  (:gen-class))

(defn default-queries
  "The very very default base queries that are executed.
  To be wrapped in middleware"
  [table-name]
  {:select (fn [query]
             (log/debug "default-queries: select: " query)
             (jdbc/query
               {:datasource *db*}
               (sql/format query)))
   :insert (fn [query]
             (jdbc/execute!
               {:datasource *db*}
               (-> query
                   (sqlh/insert-into table-name)
                   (sql/format)))
             ;; Return the values we just inserted
             (:values query))})

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
                  (mapv (fn [obj] (sc/validate (get-schema spec) obj))
                        (:values query))
                  (handler query))))
      (update :update
              (fn [handler]
                (fn [query]
                  (mapv (fn [obj] (sc/validate (get-schema spec) obj))
                        (:values query))
                  (handler query))))))

(defn build-queries-map
  [{:keys [spec properties] :as ks}]
  (cond-> (default-queries (keyword (name spec)))

          (:validate properties)
          (wrap-validate spec)

          (:id properties)
          (wrap-id nil)

          (:timestamped? properties)
          (wrap-timestamped nil)))