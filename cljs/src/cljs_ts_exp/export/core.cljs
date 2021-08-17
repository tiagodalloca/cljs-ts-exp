(ns cljs-ts-exp.export.core
  (:require [malli.core :as m]
            [camel-snake-kebab.core :as csk]))

(defmulti ->type-literal-str
  (fn [schema _]
    (first schema)))

(defmethod ->type-literal-str :map
  [schema typedef-registry]
  (let [entry-literals
        (for [[k opts child] (m/children schema)
              :let [entry-name (csk/->camelCaseString k)
                    s-form (m/-form child)
                    another-schema? (coll? s-form)
                    type-literal (if another-schema?
                                   (->type-literal-str s-form typedef-registry)
                                   (or (get-in typedef-registry [s-form :str])
                                       "any"))
                    optional? (:optional opts)]]
          (str entry-name (when optional? "?") ":" type-literal))]
    (str "{"(clojure.string/join "," entry-literals) "}")))

(defn ->type-declaration-str
  [type-name type-literal opts]
  (let [{:keys [export?]} opts]
    (str (if export? "export " nil)
         "type "  type-name " = " type-literal ";")))

