(ns governance.routes.crud
  (:require [ajax.core :as ajax]
            [cljs.core :refer [uuid random-uuid]]
            [day8.re-frame.http-fx]
            [governance.models]
            [governance.models.shared.schemas :refer [get-schema make-coercer]]
            [re-frame.core :as rf]
            [schema.core :as s]
            [schema.coerce :as sc]))

(defn routes
  []
  ["/crud"
   ])

(s/defn spec->endpoint-kw :- s/Keyword
  "Extends specs to have optional keywords.
  :a.b/c create => :a.b.c/create"
  [spec :- s/Keyword
   method :- s/Str]
  (keyword (str (namespace spec) "." (name spec))
           method))
(s/defn coll->id-map :- {s/Uuid {:id s/Uuid s/Any s/Any}}
  "Takes a collection of objects, marshalls into a map of :id"
  [coll :- [{:id s/Uuid s/Any s/Any}]]
  (->> coll
       (map (juxt :id identity))
       (into {})))
(defn create-endpoints
  [model]
  (let [spec (:spec model)
        schema (get-schema spec)
        endpoint (str "/api/crud/" (name spec))
        coercer (make-coercer [schema])]
    (println "endpoint:" spec endpoint)
    (let [create-kw (spec->endpoint-kw spec "create")
          create-success-kw (spec->endpoint-kw spec "create-success")]
      (rf/reg-event-fx
        create-kw
        (s/fn [_ [_ objs] :- [(s/one s/Keyword "k") (s/one [schema] "objs")]]
          {:http-xhrio {:method          :post
                        :params          objs
                        :uri             endpoint
                        :timeout         3000
                        :format          (ajax/json-request-format)
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [create-success-kw]
                        ;; TODO on-failure
                        }}))
      (rf/reg-event-fx
        create-success-kw
        (s/fn [{:keys [db]} [_ objs]]
          {:db (update-in db [:crud spec] merge (coll->id-map (coercer objs)))})))
    ;; Read
    (let [read-kw (spec->endpoint-kw spec "read")
          read-success-kw (spec->endpoint-kw spec "read-success")]
      (rf/reg-event-fx
        read-kw
        (fn [_ [_ query]]
          {:http-xhrio {:method          :get
                        :params          query
                        :uri             endpoint
                        :timeout         3000
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [read-success-kw]
                        ;; TODO on-failure
                        }}))
      (rf/reg-event-fx
        read-success-kw
        (fn [{:keys [db]} [_ result]]
          ;; Take the result, add it into the database
          {:db (update-in db [:crud spec] merge (coll->id-map (coercer result)))})))
    {(name spec)
     {:create (spec->endpoint-kw spec "create")
      :read   (spec->endpoint-kw spec "read")}}))
(comment
  (-> (governance.models/models)
      vals
      first))
(defn utils
  []
  (-> (governance.models/models)
      vals))