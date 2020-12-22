(ns governance.models.shared
  #?(:cljs (:require-macros [governance.models.shared :refer [create-field create-model-cljs]]))
  (:require [clojure.spec.alpha :as s]
            [taoensso.timbre :as log]
            [honeysql.core :as sql]
            [honeysql.helpers :as sqlh]
            [governance.models.generic :as generic]
            [governance.models.shared.query]))

(defmacro create-field
  [field]
  `(s/def ~(:name field) ~(:spec field)))

(comment
  (macroexpand-1 '(create-field {:name ::test :spec boolean?}))
  (create-field {:name ::test :spec boolean?}))

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
     (update model :fields concat [generic/updated_at generic/created_at]))})
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
(comment
  (sql/format
    {:select [:*]
     :from   [:crises]})
  (-> (sqlh/select :*)
      (sqlh/from :crises)
      (sqlh/select :crises/id)
      (sql/format)))
(defmacro create-model-clj
  [ks]
  (let [ks' (-> ks
                eval
                apply-properties)]
    `(merge (create-shared ~ks')
            {:crud (governance.models.shared.query/build-queries-map '~ks')})))
(defmacro create-model-cljs
  [ks]
  (let [ks' (apply-properties (eval ks))]
    `(merge (create-shared ~ks'))))

(comment
  (macroexpand '(create-model-clj {:spec ::something}))
  (macroexpand '(governance.models.shared/create-shared {:spec :governance.models.shared/something}))
  (create-model-clj {:spec ::something})
  (-> '(create-model-cljs {:spec ::something})
      macroexpand
      macroexpand)
  (macroexpand '(create-model-cljs {:spec       ::something
                                    :properties {:id           true
                                                 :timestamped? true
                                                 :validated    ::something}})))