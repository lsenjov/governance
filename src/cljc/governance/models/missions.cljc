(ns governance.models.missions
  (:require [governance.models.generic :as generic]))

(def config
  {:spec
   ::missions
   :fields
   [{:name ::mission-text
     :spec ::generic/string-non-empty
     :component/type :input/text}]
   :properties
   {:id true
    :timestamped? true
    :validate ::crisis}})
