(ns governance.models.shared
  #?(:cljs (:require-macros [governance.models.shared :refer [create-field create-model-cljs]]))
  (:require [clojure.spec.alpha :as s]
            [taoensso.timbre :as log]
            [governance.models.generic :as generic]
            #?@(:clj
                [[toucan.models]
                 [toucan.db]
                 [governance.db.toucan :as toucan]])))

(defmacro create-field
  [field]
  `(s/def ~(:name field) ~(:spec field)))

(comment
  (macroexpand-1 '(create-field {:name ::test :spec boolean?}))
  (create-field {:name ::test :spec boolean?}))

(defn default-toucan-opts
  ;; Takes a map of key->fn-tail, puts in any missing defaults, converts the whole thing to opts
  [{toucan-opts :toucan/opts
    properties  :properties
    :as         ks} spec]
  (->>
    ;; Here's our defaults map
    ;; By default, all objects are timestamped
    {:properties
     (list '[_] properties)}
    ;; Overwrite the above defaults
    (merge toucan-opts)
    ;; Put the symbol name back in the record
    (map (fn [[k v]] (concat [(symbol k)] v)))))
(comment
  (macroexpand-1 '(default-toucan-opts nil ::users))
  (default-toucan-opts {} ::users))

(def model-properties
  "Each property is a function: taking a model and argument and transforming it"
  {:id
   (fn [model _]
     (log/trace "property: id")
     (update model :fields
             concat [generic/id]))
   :timestamped?
   (fn [model _]
     (log/trace "property: timestamped")
     (update-in model [:fields] concat [generic/updated_at generic/created_at]))})
(defn apply-properties
  [{properties :properties
    :as        ks}]
  (log/trace "apply-properties")
  ;; Take the properties
  ((->> properties
        ;; Get the associated function from model-properties
        (map (fn [[k v]] [(get model-properties k) v]))
        ;; Cull any without one
        (filter first)
        ;; Create a function for each
        (map (fn [[k-fn v]] (fn [obj] (k-fn obj v))))
        ;; Compose them
        (apply comp))
   ;; Apply it to ks
   ks))
(defmacro create-fields
  [{:keys [fields]}]
  (mapv #(list `create-field %) fields))
(defmacro create-specs
  [{:keys [fields spec]
    :as   ks}]
  (let [required-keys (->> fields (filter (comp not false? :required)) (map :name))
        optional-keys (->> fields (filter (comp false? :required)) (map :name))
        query-spec-kw (keyword (namespace spec) "query")]
    `(do
       ;; Set up specs for each of the individual fields
       (create-fields ~ks)
       ;; Now a spec for the object itself
       (s/def ~spec (s/keys
                      ~@(when required-keys [:req-un required-keys])
                      ~@(when optional-keys [:opt-un optional-keys])))
       ;; Do a spec where everything is optional - useful for queries
       (s/def ~query-spec-kw (s/keys :opt-un ~(concat required-keys optional-keys)))
       ;; Do a spec where an array of results is nestled under :value
       (s/def ~(keyword (namespace spec) "value") (s/coll-of ~spec))
       (s/def ~(keyword (namespace spec) "get-response") (s/keys :req-un [~(keyword (namespace spec) "value")]))
       {:spec          ~spec
        :query-spec    ~query-spec-kw
        :response-spec ~(keyword (namespace spec) "get-response")})))
(defmacro create-shared
  [ks]
  `(merge
     (create-specs ~ks)))
(defmacro create-model-clj
  "Create all the things we need for crud.
  This defines a `model` in the calling namespace"
  [ks]
  (let [{:keys       [fields spec]
         toucan-opts :toucan/opts
         :as         ks'}
        (apply-properties (eval ks))
        toucan-model (symbol (clojure.string/capitalize (name spec)))
        model-sym 'model]
    ;; Evaluate these separately, should return a nice list of things

    ;; Create the toucan object pointing to the db
    `(do
       ~(concat
          `(toucan.models/defmodel ~toucan-model
                                   ~spec
                                   toucan.models/IModel)
          (default-toucan-opts ks' spec))
       ;; Return a listing of the things we made

       (def ~model-sym
         (merge (create-shared ~ks')
                {:toucan-model ~toucan-model})))))
(defmacro create-model-cljs
  [ks]
  (let [ks' (apply-properties (eval ks))
        model-sym 'model]
    `(def ~model-sym
       (merge (create-shared ~ks')))))

(comment
  (macroexpand '(create-model-cljs {:spec ::something}))
  (macroexpand '(create-model-cljs {:spec ::something
                                    :properties {:id true
                                                 :timestamped? true
                                                 :validated ::something}})))