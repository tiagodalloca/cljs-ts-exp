(ns cljs-ts-exp.export.parse
  (:require [malli.core :as m]
            [cljs-ts-exp.export.transform :refer [transform]]
            [clojure.string :as string]
            ["path" :as path]))

(comment
  (-normalize-file "flow/"))

(defmulti ^:private -parse-node
  (fn [node options]
    (cond
      (:$ref node) :$ref
      (:type node) [:type (:type node)]
      (:union node) :union
      (:intersection node) :intersection)))

(defmethod -parse-node :$ref
  [{:keys [$ref] :as node} {:keys [deref-types
                                   schema-id->type-desc
                                   files-import-alias]
                            :as options}]
  (if (get deref-types $ref)
    (-parse-node (get-in [:definitions :$ref] node) options)
    (let [file (get-in schema-id->type-desc [$ref :file])
          import-alias (get files-import-alias file)
          type-name (get-in schema-id->type-desc [$ref :name])]
      (str import-alias "." type-name))))

(comment
  (-parse-node
   {:$ref :flow/person}
   {:schema-id->type-desc
    {:flow/person {:name "FlowPerson"
                   :file "flow/person/index.d.ts"}}
    :files-import-alias {"flow/person/index.d.ts" "fp"}}))

(defmethod -parse-node [:type :number] [_ _] "number")

(comment (defn parse
           [schemas-v options]
           (let [schemas-v (partition 2 schemas-v)
                 
                 schema-id->type-desc
                 (into {} (map (fn [[k type-desc]]
                                 [k (update type-desc :file -normalize-file)])
                               schemas-v))
                 
                 (for [[schema-id _] schemas-v]
                   )])))

(comment
(parse
 [:flow/person {:name "FlowPerson"
                :file "flow/index.d.ts"
                :import}
  ]
 {:files-import-alias {"flow/index.d.ts" "flow"}
  :registry {}})
)

