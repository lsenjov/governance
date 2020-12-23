(ns governance.models
  (:require
    [clojure.spec.alpha :as s]
    [governance.models.crisis]
    [governance.models.tag]
    [governance.models.user]

    [governance.models.shared :as shared]
    [governance.models.shared.query]))

(def configs
  [governance.models.crisis/config
   governance.models.tag/config
   governance.models.user/config])
(defn construct-models
  [models]
  (->> models
       (map (fn [model] {(:spec model) (shared/create-model model)}))
       (apply merge)))
(def models
  (construct-models configs))

(comment
  (shared/create-model governance.models.user/config)

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