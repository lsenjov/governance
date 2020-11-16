(ns governance.models.generic
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clj-time.core]))

(s/def ::string-non-empty
  (s/and string?
         #(pos? (count %))))
(s/def ::id ::string-non-empty)

(s/def ::timestamp
  (s/with-gen
    #(= (class %)
       java.sql.Timestamp)
    #(gen/fmap (fn [l] (java.sql.Timestamp.
                         ;; This does times _around about_ 2017
                         (+ l 1500000000000)))
               (gen/large-integer))))
(s/def ::created_at ::timestamp)
(s/def ::updated_at ::timestamp)