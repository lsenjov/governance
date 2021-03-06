(ns governance.db.toucan
  (:require
    [governance.config :refer [env]]
    [toucan.db :as db]
    [toucan.models :refer [add-type! add-property!]]
    [clojure.spec.alpha :as s])
  (:import (java.sql Timestamp)
           (java.util UUID)))


(db/set-default-db-connection!
  {:classname   "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname     "//localhost:5432/governance_dev"
   :user        "postgres"
   :password "Pass2020!"})
(add-property! :timestamped?
               :insert (fn [obj _]
                         (let [now (Timestamp. (System/currentTimeMillis))]
                           (assoc obj :created_at now, :updated_at now)))
               :update (fn [obj _]
                         (assoc obj :updated_at (Timestamp. (System/currentTimeMillis)))))
(add-property! :id
               :insert (fn [obj _]
                         (update obj :id #(or % (str (UUID/randomUUID))))))
(add-property! :validate
               :insert (fn [obj spec]
                         (assert (s/valid? spec obj) (s/explain spec obj))
                         obj)
               :update (fn [obj spec]
                         (assert (s/valid? spec obj) (s/explain spec obj))
                         obj))