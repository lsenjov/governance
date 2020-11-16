(ns governance.models.user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [governance.db.toucan :as toucan]
            [toucan.models]
            [toucan.db]
            [governance.models.generic :as generic]
            [governance.models.shared :as shared]))

(s/def ::first_name ::generic/string-non-empty)
(s/def ::last_name ::generic/string-non-empty)
(s/def ::email ::generic/string-non-empty)
(s/def ::admin (s/nilable boolean?))
(s/def ::last_login (s/nilable ::generic/timestamp))
(s/def ::is_active (s/nilable boolean?))

(def config
  {:required-keys
   [::generic/id]
   :optional-keys
   [::generic/created_at ::generic/updated_at
    ::first_name ::last_name ::email ::admin ::last_login ::is_active]
   ;; This shold match up with the table name
   :spec ::users})

(shared/create-model config)
(comment
  (-> '(shared/create-model config)
     macroexpand
     second)

 (gen/sample (s/gen ::users)))