(ns governance.models.user
  (:require [governance.models.generic :as generic]
            [governance.models.shared.schemas :refer [add-schema! get-schema]]
            [schema.core :as as]
            [schema.core :as sc]))

(def config
  ;; The name of the spec, and also the name of the table we're pulling from
  {:spec
   ::users
   ;; Each of our fields on this table
   :fields
   ;; Using symbols to call in predefined fields
   [{:name     ::first_name
     ;; Note these need to be quoted if you're doing any functions/macros on them
     :spec     (sc/maybe (get-schema ::generic/string-non-empty))
     :required false}
    {:name     ::last_name
     :spec     (sc/maybe (get-schema ::generic/string-non-empty))
     :required false}
    {:name ::email
     :spec ::generic/string-non-empty}
    {:name     ::admin
     :spec     (sc/maybe sc/Bool)
     :required false}
    {:name     ::last_login
     :spec     (sc/maybe (get-schema ::generic/timestamp))
     :required false}
    {:name ::is_active
     :spec sc/Bool}]

   :properties
   {:timestamped? true
    :id           true
    :validate     ::users}})