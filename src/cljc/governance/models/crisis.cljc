(ns governance.models.crisis
  (:require [governance.models.generic :as generic]))

(def config
  {:spec
   ::crisis
   :fields
   [{:name ::announcement
     :spec ::generic/string-non-empty
     :component/type :input/text}
    {:name ::description
     :spec ::generic/string-non-empty
     :component/type :input/text}]
   :properties
   {:id true
    :timestamped? true
    :validate ::crisis}})