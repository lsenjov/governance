(ns governance.models.user
  (:require [governance.models.generic :as generic]))

(def config
  ;; The name of the spec, and also the name of the table we're pulling from
  {:spec
   ::users
   ;; Each of our fields on this table
   :fields
   ;; Using symbols to call in predefined fields
   [{:name     ::first_name
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
     :spec 'boolean?}]

   :properties
   {:timestamped? true
    :id true
    :validate ::users}})