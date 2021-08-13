(ns cljs-ts-exp.dev.exp2-schema-parsing
  (:require [cljs-ts-exp.dev.exp1 :as exp1]
            [malli.core :as m]
            [malli.registry :as mr]
            [camel-snake-kebab.core :as csk]))

(comment (mr/set-default-registry!
          (mr/mutable-registry exp1/*registry-db)))

(exp1/register-schema! :string string?)

(defmulti type-literal-str
  (fn [schema _]
    (first schema)))

(defmethod type-literal-str :map
  [[_ & schema-items] typedef-registry]
  (let [fst-item (first schema-items)
        
        k-schemas (if (map? fst-item) ;; checking options map
                    (rest schema-items)
                    schema-items)
        
        entry-literals
        (for [k-schema k-schemas]
          (if (vector? k-schema)
            (let [[k & xs] k-schema
                  fst-schema-item (first xs)
                  are-there-options? (map? fst-schema-item)
                  optional? (when are-there-options?
                              (:optional fst-schema-item))
                  entry-schema  (if are-there-options?
                                  (second xs)
                                  fst-schema-item)
                  another-schema? (map? entry-schema)
                  type-literal (if another-schema?
                                 (->typedef-str entry-schema typedef-registry)
                                 (or (get-in typedef-registry [entry-schema :str])
                                     "any"))
                  entry-name (csk/->camelCaseString k)]
              (str entry-name (when optional? "?") ": " type-literal))
            (comment "fix when k-schema is not a vector")))]
    (str "{\n"(clojure.string/join ",\n" entry-literals) "\n}")))

(comment
  (type-literal-str
   [:map
    [:a :number]
    [:b :string]]
   {:number {:str "number"}
    :string {:str "string"}}))
