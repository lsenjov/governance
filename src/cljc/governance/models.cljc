(ns governance.models
  (:require
    [clojure.spec.alpha :as s]
    [governance.models.crisis]
    [governance.models.tag]
    [governance.models.user]

    [governance.models.shared :as shared]
    [governance.models.shared.query]))

(defmacro construct-models
  "Constructs all the models, puts them into a map of spec->model"
  [configs]
  (println "configs")
  (println configs)
  (into {}
         (mapv (fn [config]
           (let [config' (eval config)]
             (println "config:")
             (println config' (:spec config'))
             {(:spec config') `(shared/create-model-clj ~config)}))
         configs)))
(def models
  (construct-models [governance.models.crisis/config
                     governance.models.tag/config
                     governance.models.user/config
                     ]))

(comment
  (macroexpand-1 '(construct-models [governance.models.crisis/config
                                     governance.models.tag/config
                                     governance.models.user/config
                                     ]))
  (macroexpand '(construct-models [governance.models.crisis/config
                                   governance.models.tag/config
                                   governance.models.user/config
                                   ]))
  (construct-models configs)

  (macroexpand '(shared/create-model-clj governance.models.crisis/config))
  (shared/create-model-clj governance.models.crisis/config)
  (shared/create-model-clj governance.models.tag/config)

  (governance.models.shared.query/build-queries-map governance.models.user/config)
  (macroexpand '(shared/create-model-clj governance.models.user/config))
  (shared/create-model-clj governance.models.user/config))