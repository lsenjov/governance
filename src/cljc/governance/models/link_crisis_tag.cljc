(ns governance.models.link-crisis-tag
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [governance.models.generic :as generic]
            [governance.models.shared :as shared
             #?@(:clj  [:refer [create-model-clj]]
                 :cljs [:refer-macros [create-model-cljs]])]))

(def config
  {:spec
   ::link_crisis_tag
   :fields
   [(generic/foreign-ref "crisis" {})
    (generic/foreign-ref "tag" {})]
   :toucan/opts
   {:hydration-keys '([_] [:tag_id :crisis_id])}
   })

#?(:clj (create-model-clj config)
   :cljs (create-model-cljs config))
