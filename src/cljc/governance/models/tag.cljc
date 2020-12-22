(ns governance.models.tag
  (:require [governance.models.generic :as generic]))

(def config
  {:spec
   ::tag
   :fields
   [{:name ::name
     :spec ::generic/string-non-empty
     :component/type :input/text}]
   :properties
   {:id true
    :timestamped? true
    :validate ::tag}})