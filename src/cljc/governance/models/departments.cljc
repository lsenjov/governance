(ns governance.models.departments
  (:require [governance.models.generic :as generic]))

(def config
  {:spec
   ::departments
   :fields
   [{:name ::name
     :spec ::generic/string-non-empty
     :component/type :input/text}]
   :properties
   {:id true
    :timestamped? true
    :validate ::crisis}})