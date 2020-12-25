(ns governance.models.shared.schemas
  (:require [schema.core :as s]
            [schema.coerce :as sc]))

(def Timestamp
  #?(:clj  java.sql.Timestamp
     :cljs (s/pred #(= (type %) js/Date))))

(defn string->date
  [s]
  ;; TODO clj
  #?(:clj  identity
     :cljs (js/Date. s)))
(def extra-coercions
  (merge sc/+json-coercions+
         {Timestamp string->date}))
(defn extra-matcher
  [schema]
  (extra-coercions schema))
(defn make-coercer
  [schema]
  (sc/coercer schema extra-matcher))

(def Some
  "Anything but nil"
  (s/constrained s/Any identity))

(defonce *schemas (atom {}))
;; TODO change some to whatever type a schema is
;; If this is returning nil there's a major problem somewhere
(s/defn get-schema :- Some
  "Given a keyword or a schema, returns the schema associated"
  [name-or-schema :- (s/conditional keyword? s/Keyword
                                    :else s/Any)]
  (if (keyword? name-or-schema)
    (get @*schemas name-or-schema)
    name-or-schema))

;; TODO change any to whatever type a schema is
(s/defn add-schema! :- Some
  "Adds a schema to the bank.
  If given a keyword as the schema, will duplicate the named schema"
  [name :- s/Keyword
   schema :- (s/conditional keyword? s/Keyword
                            :else Some)]
  (get
    (if (keyword? schema)
      (swap! *schemas assoc name (get-schema schema))
      (swap! *schemas assoc name schema))
    name))