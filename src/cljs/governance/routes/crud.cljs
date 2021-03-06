(ns governance.routes.crud
  (:require [ajax.core :as ajax]
            [cljs.core :refer [uuid random-uuid]]
            [day8.re-frame.http-fx]
            [governance.models]
            [governance.models.shared.schemas :refer [get-schema make-coercer]]
            [re-frame.core :as rf]
            [schema.core :as s]
            [schema.coerce :as sc]))

(defonce *_t (atom nil))

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
    ;; Create
    (let [create-kw (spec->endpoint-kw spec "create")
          create-success-kw (spec->endpoint-kw spec "create-success")]
      (rf/reg-event-fx
        create-kw
        (s/fn [_ [_ objs] :- [(s/one s/Keyword "k") (s/one [schema] "objs")]]
          {:http-xhrio {:method          :post
                        :params          objs
                        :uri             endpoint
                        :timeout         3000
                        :format          (ajax/transit-request-format)
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
          (reset! *_t result)
          (println "objs:" (pr-str result))
          ;; Take the result, add it into the database
          {:db (update-in db [:crud spec] merge (coll->id-map (coercer result)))})))
    ;; Update
    (let [update-kw (spec->endpoint-kw spec "update")
          update-success-kw (spec->endpoint-kw spec "update-success")]
      (rf/reg-event-fx
        update-kw
        (s/fn [_ [_ obj]]
          {:http-xhrio {:method          :put
                        :params          obj
                        :uri             endpoint
                        :timeout         3000
                        :format          (ajax/transit-request-format)
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [update-success-kw obj]
                        ;; TODO on-failure
                        }}))
      (rf/reg-event-fx
        update-success-kw
        ;; If successful, we assume our updates were successful
        (s/fn [{:keys [db]}
               [_ obj result] :- [(s/one s/Keyword "k")
                                  (s/one (:query model) "changes")
                                  (s/one s/Any "result")]]
          {:db (update-in db [:crud spec (:id obj)] merge obj)})))
    ;; Delete
    (let [delete-kw (spec->endpoint-kw spec "delete")
          delete-success-kw (spec->endpoint-kw spec "delete-success")]
      (rf/reg-event-fx
        delete-kw
        (s/fn [_ [_ {id :id}] :- [(s/one s/Keyword "k")
                                  (s/one {:id (get-schema :governance.models.generic/id)} "req")]]
          {:http-xhrio {:method          :delete
                        :params          {:id id}
                        :uri             endpoint
                        :timeout         3000
                        :format          (ajax/transit-request-format)
                        :response-format (ajax/json-response-format {:keywords? true})
                        :on-success      [delete-success-kw id]}}))
      (rf/reg-event-fx
        delete-success-kw
        (s/fn [{:keys [db]}
               [_ id _] :- [(s/one s/Keyword "k")
                            (s/one (get-schema :governance.models.generic/id) "id")
                            (s/one s/Any "response")]]
          {:db (update-in db [:crud spec] dissoc id)})))
    {(name spec)
     {:create (spec->endpoint-kw spec "create")
      :read   (spec->endpoint-kw spec "read")
      :update (spec->endpoint-kw spec "update")
      :delete (spec->endpoint-kw spec "delete")}}))
(comment
  (-> (governance.models/models)
      vals
      first))
(s/defn utils
  :- {s/Str {:create s/Keyword
             :read   s/Keyword
             :update s/Keyword
             :delete s/Keyword}}
  "Returns a listing of tables, with associated CRUD keywords for re-frame"
  []
  (->> (governance.models/models)
       vals
       (map create-endpoints)
       (into {})))