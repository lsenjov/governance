(ns governance.models.user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [governance.models.generic :as generic]
            [governance.models.shared :as shared
             #?@(:clj [:refer [create-model-clj]]
                 :cljs [:refer-macros [create-model-cljs]])]))

(def config
  ;; The name of the spec, and also the name of the table we're pulling from
  {:spec
   ::users
   ;; Each of our fields on this table
   :fields
   ;; Using symbols to call in predefined fields
   [{:name     ::first_name
     ;; Note these need to be quoted if you're doing any functions/macros on them
     :spec     '(s/nilable ::generic/string-non-empty)
     :required false}
    {:name     ::last_name
     :spec     '(s/nilable ::generic/string-non-empty)
     :required false}
    {:name ::email
     :spec ::generic/string-non-empty}
    {:name     ::admin
     :spec     '(s/nilable boolean?)
     :required false}
    {:name     ::last_login
     :spec     '(s/nilable ::generic/timestamp)
     :required false}
    {:name ::is_active
     :spec 'boolean?}]

   :properties
   {:timestamped? true
    :id true
    :validate ::users}

   ;; Pass these directly to toucan, overriding any defaults presented in g.models.shared
   ;; These go into the record declaration
   :toucan/opts
   {:hydration-keys '([_] [:users])}})

;; I'd like to simplify this more, but this may be the limit on what I can do with it for now
;; Mainly because in macros, they're being done at compilation _inside clj_, so reader macros always see clj
;; I think. Probably needs more testing
#?(:clj (create-model-clj config)
   :cljs (create-model-cljs config))
;(Users :email "something@somewhere.com")
(comment
  (clojure.pprint/pprint (-> '(shared/create-model-clj config)
                             macroexpand))
  (-> '(shared/create-model config)
      macroexpand-1)
  (shared/apply-properties config)
  (create-model-cljs config)

  (Users)
  (Users :email "something@somewhere.com")
  (toucan.db/insert! Users {})
  (toucan.db/insert! Users
                     {:email     "something@somewhere.com"
                      :is_active true})
  (toucan.db/update! Users "9809ccf6-14cf-461f-aab5-854ea1fd2e82"
                     {:email     "something@somewhere.com"
                                   :is_active false})
  ;; Deletes _all_ the things
  (toucan.db/delete! Users)

  (gen/sample (s/gen ::users)))