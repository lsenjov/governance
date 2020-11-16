(ns governance.models.shared
  (:require [clojure.spec.alpha :as s]
            [governance.db.toucan :as toucan]
            [toucan.models]
            [toucan.db]))

(defmacro create-model
  [ks]
  (let [{:keys [required-keys optional-keys spec] :as ks'} (eval ks)]
    ;; Evaluate these separately, should return a nice list of things
    [`(s/def ~spec (s/keys
                     ~@(when required-keys [:req-un required-keys])
                     ~@(when optional-keys [:opt-un optional-keys])))
     `(toucan.models/defmodel ~(symbol (clojure.string/capitalize (name spec)))
                              ~spec
                              toucan.models/IModel)]))