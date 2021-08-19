(ns cljs-ts-exp.export.parse
  (:require [cljs-ts-exp.export.transform :refer [transform]]
            [malli.core :as m]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as string]
            ["path" :as path]))

(comment
  (ns-unmap 'cljs-ts-exp.export.parse '-parse-node))

(defn- -dispatch-parse-node
  [node options]
  (cond
    (:$ref node) :$ref
    (:type node) [:type (:type node)]
    (:union node) :union
    (:intersection node) :intersection
    (some? (:const node)) :const
    :else [:type :any]))

(defmulti ^:private -parse-node
  #'-dispatch-parse-node)

(defmethod -parse-node :$ref
  [{:keys [$ref] :as node} {:keys [deref-types
                                   schema-id->type-desc
                                   files-import-alias]
                            :as options}]
  (if (get deref-types $ref)
    (-parse-node (-> node (get-in [:definitions $ref]) transform)
                 options)
    (let [file (get-in schema-id->type-desc [$ref :file])
          import-alias (get files-import-alias file)
          type-name (get-in schema-id->type-desc [$ref :name])]
      (str import-alias "." type-name))))

(comment
  (-parse-node
   {:$ref :flow/person
    :definitions {:flow/person [:tuple :string :int]}}
   {:deref-types {:flow/person false}
    :schema-id->type-desc
    {:flow/person {:name "FlowPerson"
                   :file "flow/person/index.d.ts"}}
    :files-import-alias {"flow/person/index.d.ts" "fp"}})
  
  (-parse-node
   {:$ref :flow/person
    :definitions {:flow/person [:tuple :string :int]}}
   {:deref-types {:flow/person true}
    :schema-id->type-desc
    {:flow/person {:name "FlowPerson"
                   :file "flow/person/index.d.ts"}}
    :files-import-alias {"flow/person/index.d.ts" "fp"}}))

(defmethod -parse-node [:type :number] [_ _] "number")
(defmethod -parse-node [:type :string] [_ _] "string")
(defmethod -parse-node [:type :boolean] [_ _] "boolean")
(defmethod -parse-node [:type :any] [_ _] "any")
(defmethod -parse-node [:type :undefined] [_ _] "undefined")
(defmethod -parse-node :const [{:keys [const] :as node} options]  
  (cond
    (keyword? const) (str \" (name const) \")
    (string? const) (str \" const \")
    (some #(% const) [boolean? number?]) (str const)
    :else (-parse-node {:type :any} options)))

(defmethod -parse-node [:type :array] [{:keys [items]} _]
  (str "Array<" (-parse-node items) ">"))

(comment
  (-parse-node {:type :array :items {:type :number}} nil))

(defmethod -parse-node [:type :tuple] [{:keys [items]} options]
  (str "[" (string/join "," (map #(-parse-node % options) items)) "]"))

(comment (-parse-node (transform [:tuple :int :string :boolean])))

(defmethod -parse-node :union [{items :union} options]
  (str "(" (string/join "|" (map #(-parse-node % options) items)) ")"))

(defmethod -parse-node :intersection [{items :intersection} options]
  (str "(" (string/join "&" (map #(-parse-node % options) items)) ")"))

(comment
  (-parse-node (transform [:enum 1 :something false]))
  (-parse-node (transform [:maybe :string]))
  (-parse-node (transform [:or :string :int]))
  (-parse-node (transform [:orn [:name :string] [:age :int]]))
  ;; I know this doesn't make sense
  (-parse-node (transform [:and :string :boolean])))

(defmethod -parse-node [:type :object] [{:keys [properties
                                                optional
                                                index-signature]}
                                        options]
  (let [idx-sign-literal (if index-signature
                           (str "[k:" (-parse-node (first index-signature) options) "]:"
                                (-parse-node (second index-signature) options)))
        properties-literal (if-not (empty? properties)
                             (string/join
                              ","
                              (map (fn [[k v]]
                                     (str \" (name k) \" (if (get optional k) "?") ":"
                                          (-parse-node v options)))
                                   properties)))]
    (str "{" (string/join "," (filter (comp not string/blank?)
                                      [idx-sign-literal properties-literal]))
         "}")))

(comment
  (-parse-node (transform [:map [:a :int] [:b :string]]))
  (-parse-node (transform [:map-of :string :int]))
  (-parse-node
   {:$ref :flow/person
    :definitions {:flow/person [:map [:name string?] [:age pos-int?]]}}
   {:deref-types {:flow/person true}
    :schema-id->type-desc
    {:flow/person {:name "FlowPerson"
                   :file "flow/person/index.d.ts"}}
    :files-import-alias {"flow/person/index.d.ts" "fp"}}))

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
                  :file "flow/index.d.ts"}
    ]
   {:files-import-alias {"flow/index.d.ts" "flow"}
    :registry {}})
  )

