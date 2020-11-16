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

(defn default-toucan-opts
  ;; Takes a map of key->fn-tail, puts in any missing defaults, converts the whole thing to opts
  [ks spec]
  (->>
    ;; Here's our defaults map
    ;; By default, all objects are timestamped
    {:properties
     '([_] {:timestamped? true})

     ;; Manually drill down into here, because we need to leave spec unquoted
     :pre-insert
     (list
       ['record]
       ;; Add any modifications first, in this case add ID
       (list 'let '[record-updated
                    (update record :id #(or % (str (java.util.UUID/randomUUID))))]
             ;; Check our record against the spec
             (list 'assert
                   (list 's/valid? spec 'record-updated)
                   ;; Error message will be why the spec failed
                   (list 's/explain spec 'record-updated))
             ;; Return the updated object
             'record-updated))}
    ;; Overwrite the above defaults
    (merge ks)
    ;; Put the symbol name back in the record
    (map (fn [[k v]] (concat [(symbol k)] v)))
    ))
(comment
  (macroexpand-1 '(default-toucan-opts nil ::users))
  (default-toucan-opts {} ::users))

(defmacro create-model
  [ks]
  (let [{:keys       [fields spec]
         toucan-opts :toucan/opts
         :as         ks'}
        (eval ks)

        {required-fields true optional-fields false} (group-by :required fields)
        required-keys (->> fields (filter (comp not false? :required)) (map :name))
        optional-keys (->> fields (filter (comp false? :required)) (map :name))

        toucan-model (symbol (clojure.string/capitalize (name spec)))

        query-spec-kw (keyword (namespace spec) "query")
        ]
    ;; Evaluate these separately, should return a nice list of things

    ;; Set up specs for each of the individual fields
    `(do ~(mapv #(list `create-field %) fields)
         ;; Now a spec for the object itself
         (s/def ~spec (s/keys
                        ~@(when required-keys [:req-un required-keys])
                        ~@(when optional-keys [:opt-un optional-keys])))
         ;; Do a spec where everything is optional - useful for queries
         (s/def ~query-spec-kw (s/keys :opt-un ~(concat required-keys optional-keys)))
         ;; Do a spec where an array of results is nestled under :value
         (s/def ~(keyword (namespace spec) "value") (s/coll-of ~spec))
         (s/def ~(keyword (namespace spec) "get-response") (s/keys :req-un [~(keyword (namespace spec) "value")]))
         ;; Create the toucan object pointing to the db
         ~(concat
            `(toucan.models/defmodel ~toucan-model
                                     ~spec
                                     toucan.models/IModel)
            (default-toucan-opts toucan-opts spec))
         ;; Return a listing of the things we made
         {:spec ~spec
          :query-spec ~query-spec-kw
          :response-spec ~(keyword (namespace spec) "get-response")
          :toucan-model ~toucan-model}
         )))