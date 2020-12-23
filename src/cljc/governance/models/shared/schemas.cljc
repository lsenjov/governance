(ns governance.models.shared.schemas
  (:require [schema.core :as sc]))

(def Some
  "Anything but nil"
  (sc/constrained sc/Any identity))

(defonce *schemas (atom {}))
;; TODO change some to whatever type a schema is
;; If this is returning nil there's a major problem somewhere
(sc/defn get-schema :- Some
  "Given a keyword or a schema, returns the schema associated"
  [name-or-schema :- (sc/conditional keyword? sc/Keyword
                                     :else sc/Any)]
  (if (keyword? name-or-schema)
    (get @*schemas name-or-schema)
    name-or-schema))

;; TODO change any to whatever type a schema is
(sc/defn add-schema! :- Some
  "Adds a schema to the bank.
  If given a keyword as the schema, will duplicate the named schema"
  [name :- sc/Keyword
   schema :- (sc/conditional keyword? sc/Keyword
                             :else Some)]
  (get
    (if (keyword? schema)
      (swap! *schemas assoc name (get-schema schema))
      (swap! *schemas assoc name schema))
    name))