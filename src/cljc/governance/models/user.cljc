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
   :fields
   [generic/id
    generic/created_at
    generic/updated_at
    {:name     ::first_name
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
     :spec 'boolean?}]})

(shared/create-model config)
(comment
  (-> '(shared/create-model config)
     macroexpand)

  (toucan.models/defmodel Users :governance.models.user/users toucan.models/IModel)

 (gen/sample (s/gen ::users)))