(ns governance.db.toucan
  (:require
    [governance.config :refer [env]]
    [toucan.db :as db]
    [toucan.models :refer [defmodel IModel add-type! add-property!]])
  (:import (java.util UUID)))


(db/set-default-db-connection!
  {:classname   "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname     "//localhost:5432/governance_dev"
   :user        "postgres"
   :password "Pass2020!"})
(add-type! :integer
           :in str
           :out #(Integer/parseInt %))
(add-property! :timestamped?
               :insert (fn [obj _]
                         (let [now (java.sql.Timestamp. (System/currentTimeMillis))]
                           (assoc obj :created_at now, :updated_at now)))
               :update (fn [obj _]
                         (assoc obj :updated_at (java.sql.Timestamp. (System/currentTimeMillis)))))

(defmodel User ::users
          IModel
          (types [_] {})
          (properties [_] {:timestamped? true})
          (pre-insert
            [user]
            (update user :id #(or % (str (UUID/randomUUID))))))

(comment
  (db/insert! User {:first_name "Logan"
                    :last_name  "Senjov"
                    :email      "logansenjov@gmail.com"
                    :admin      true})
  (db/select User)
  (User)
  (User :id "1")
  (User :email "logansenjov@gmail.com"))