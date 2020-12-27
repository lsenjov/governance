(ns governance.components.raw.macros
  "Macros for raw declarations")

(defmacro
  cdefs
  "Declare a bunch of components from another item.
  Mainly to save having to declare
   (def a (adapt-react-class (oget+ lib a))) over and over"
  [from-sym items]
  (->> items
       (map (fn [item]
              `(def ~item
                 ~(format "A wrapped blueprint component")
                 (reagent.core/adapt-react-class
                   (oops.core/oget+ ~from-sym
                                    ~(str item))))))
       (cons `do)))

(comment
  (macroexpand '(cdefs somewhere [a b c d])))