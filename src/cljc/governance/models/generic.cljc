(ns governance.models.generic
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [governance.models.shared.schemas :refer [add-schema! get-schema]]
            [schema.core :as sc])
  #?(:clj (:import (java.util UUID))))

(s/def ::string-non-empty
  (s/and string?
         #(pos? (count %))))
(add-schema! ::string-non-empty (sc/constrained sc/Str #(pos? (count %))))
(s/def ::id ::string-non-empty)
(add-schema! ::id sc/Uuid)

(s/def ::timestamp
  #?(:clj
     (s/with-gen
       #(= (class %)
           java.sql.Timestamp)
       #(gen/fmap (fn [l] (java.sql.Timestamp.
                            ;; This does times _around about_ 2017
                            (+ l 1500000000000)))
                  (gen/large-integer)))
     :cljs
     (s/with-gen
       #(= (type %) js/Date)
       #(gen/fmap (fn [l] (new js/Date (+ l 1500000000000)))
                  (gen/large-integer)))))
(add-schema!
  ::timestamp
  #?(:clj java.sql.Timestamp
     :cljs (sc/pred #(= (type %) js/Date))))
(s/def ::created_at ::timestamp)
(add-schema! ::created_at ::timestamp)
(s/def ::updated_at ::timestamp)
(add-schema! ::updated_at ::timestamp)

(def id
  ;; Name we're using for the spec - must be qualified
  {:name     ::id
   ;; Actual spec. Can always throw in a with-gen here or whatever
   :spec     sc/Uuid
   ;; Optional field, assumes true
   ;; For properties, we generally set this to false
   ;; Just because it's really hard to validate properties in the right order
   :required false})
(def created_at
  {:name     ::created_at
   :spec     ::timestamp
   ;; Required is false, because properties are applied _after_ pre-insert
   ;; So the checks don't find it. Not really a big deal though, since this is purely a system thing
   :required false})
(def updated_at
  {:name     ::updated_at
   :spec     ::timestamp
   :required false})

(defmacro foreign-ref
  "For creating a foreign reference to another table"
  [field-name opts]
  `(merge
     {:name     (keyword (str *ns*) ~field-name)
      :spec     ::string-non-empty
      :required false}
    ~opts))