(ns governance.models.crisis
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [governance.models.generic :as generic]
            [governance.models.shared :as shared
             #?@(:clj [:refer [create-model-clj]]
                 :cljs [:refer-macros [create-model-cljs]])]))

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

#?(:clj (create-model-clj config)
   :cljs (create-model-cljs config))