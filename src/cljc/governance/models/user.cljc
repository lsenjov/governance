(ns governance.models.user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [governance.db.toucan :as toucan]
            [toucan.models]
            [toucan.db]
            [governance.models.generic :as generic]
            [governance.models.shared :as shared]))

(def config
  ;; The name of the spec, and also the name of the table we're pulling from
  {:spec ::users
   ;; Each of our fields on this table
   :fields
   ;; Using symbols to call in predefined fields
   [generic/id
    generic/created_at
    generic/updated_at
    {:name     ::first_name
     ;; Note these need to be quoted if you're doing any functions/macros on them
     :spec     '(s/nilable ::generic/string-non-empty)
     :required false}
    {:name     ::last_name
     :spec     '(s/nilable ::generic/string-non-empty)
     :required false}
    {:name ::email
     :spec ::generic/string-non-empty}
    {:name     ::admin
     :spec     '(s/nilable boolean?)
     :required false}
    {:name     ::last_login
     :spec     '(s/nilable ::generic/timestamp)
     :required false}
    {:name ::is_active
     :spec boolean?}]
   ;; Pass these directly to toucan, overriding any defaults presented in g.models.shared
   ;:toucan/opts
   ;'[(properties [_] {:timestamped? true})
   ;  (pre-insert
   ;    [record]
   ;    (update record :id #(or % (str (java.util.UUID/randomUUID)))))]
   })

(shared/create-model config)
(comment
  (-> '(shared/create-model config)
     macroexpand)

  (Users)
  (toucan.db/insert! Users {})
  (toucan.db/insert! Users
                     {:email "something@somewhere.com"
                      :is_active true})

 (gen/sample (s/gen ::users)))