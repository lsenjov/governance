(ns governance.models.shared
  #?(:cljs (:require-macros [governance.models.shared :refer [create-field create-model-cljs]]))
  (:require [clojure.spec.alpha :as s]
            [taoensso.timbre :as log]
            [honeysql.core :as sql]
            [honeysql.helpers :as sqlh]
            [governance.models.generic :as generic]
            [governance.models.shared.schemas :refer [add-schema! get-schema]]
            [governance.models.shared.query]
            [schema.core :as sc]))


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
(defn create-schemas
  [{:keys [fields spec]
    :as   ks}]
  (let [required-fields (->> fields (filter (comp not false? :required)))
        optional-fields (->> fields (filter (comp false? :required)))
        query-spec-kw (keyword (namespace spec) "query")
        coll-spec-kw (keyword (namespace spec) "collection")
        schema (merge
                 ;; Required fields, no modification, just adding in
                 (->> required-fields
                      (map (fn [field] {(keyword (name (:name field)))
                                        (get-schema (:spec field))}))
                      (into {}))
                 (->> optional-fields
                      (map (fn [field] {(sc/optional-key (keyword (name (:name field))))
                                        (get-schema (:spec field))}))
                      (into {})))
        ;; Like the schema, but all the fields are optional here
        query-schema (->> fields
                          (map (fn [field] {(sc/optional-key (keyword (name (:name field))))
                                            (get-schema (:spec field))}))
                          (into {}))]
    (add-schema! spec schema)
    (add-schema! coll-spec-kw [schema])
    (add-schema! query-spec-kw query-schema)
    {:schema schema
     :collection [schema]
     :query query-schema}))

(defn create-model
  [ks]
  (let [ks' (-> ks
                apply-properties)]
    (merge
      ks'
      (create-schemas ks')
      {:crud (governance.models.shared.query/build-queries-map ks')})))

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