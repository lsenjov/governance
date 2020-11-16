(ns governance.db.toucan
  (:require
    [governance.config :refer [env]]
    [toucan.db :as db]
    [toucan.models :refer [add-type! add-property!]])
  (:import (java.sql Timestamp)))


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