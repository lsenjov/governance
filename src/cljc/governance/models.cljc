(ns governance.models
  (:require
    [governance.models.crisis]
    [governance.models.tag]
    [governance.models.user]))

(defmacro add-links
  "Takes a full-config, adds links from link tables"
  [f-config]
  (let [full-config (eval f-config)]
    full-config))

(def configs
  [governance.models.crisis/config
   governance.models.tag/config
   ;governance.models.user/config
   ])

(def links
  [{:tables
    {:governance.models.crisis/crisis {}
     :governance.models.tag {}}}])

(def full-config
  {:configs configs
   :links links})

(macroexpand '(add-links full-config))

(def models
  [governance.models.crisis/model
   governance.models.tag/model
   governance.models.user/model])
