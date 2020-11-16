(ns governance.models.shared
  (:require [clojure.spec.alpha :as s]
            [governance.db.toucan :as toucan]
            [toucan.models]
            [toucan.db]))

(defmacro create-field
  [field]
  `(s/def ~(:name field) ~(:spec field)))

(macroexpand-1 '(create-field {:name ::test :spec boolean?}))
(create-field {:name ::test :spec boolean?})

(defmacro create-model
  [ks]
  (let [{:keys [fields spec] :as ks'} (eval ks)
        {required-fields true optional-fields false} (group-by :required fields)
        required-keys (->> fields (filter (comp not false? :required)) (map :name))
        optional-keys (->> fields (filter (comp false? :required)) (map :name))
        ]
    ;; Evaluate these separately, should return a nice list of things

    ;; Set up specs for each of the individual fields
    `(do ~(mapv #(list `create-field %) fields)
         ;; Now a spec for the object itself
         (s/def ~spec (s/keys
                         ~@(when required-keys [:req-un required-keys])
                         ~@(when optional-keys [:opt-un optional-keys])))
         ;; Create the toucan object pointing to the db
         (toucan.models/defmodel ~(symbol (clojure.string/capitalize (name spec)))
                                  ~spec
                                  toucan.models/IModel)
         )))