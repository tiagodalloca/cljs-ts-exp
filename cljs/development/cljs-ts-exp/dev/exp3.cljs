(ns cljs-ts-exp.dev.exp3
  (:require [cljs-ts-exp.export.core :as ts-export]
            [cljs-ts-exp.dev.exp1 :as exp1]
            [malli.core :as m]
            [malli.registry :as mr]
            ["fs" :as fs]))

(comment (mr/set-default-registry!
          (mr/mutable-registry exp1/registry-db*)))

(swap! exp1/registry-db* assoc :flow/person
       [:map
        [:name :string]
        [:age :number]])

(comment
  (mr/-schema @mr/registry* :flow/person))

(def primitive-types-mapping
  {:number {:str "number"}
   :string {:str "string"}
   :boolean {:str "boolean"}})

(def types-mapping
  (merge
   {:flow/person {:str (ts-export/->type-literal-str
                        (mr/-schema @mr/registry* :flow/person)
                        primitive-types-mapping)}}
   primitive-types-mapping))

(comment
  (let [file-content (ts-export/->type-declaration-str
                      "FlowPerson"
                      (-> types-mapping :flow/person :str)
                      {:export? true})
        where "out/"]
    (fs/writeFile (str where "exp3.d.ts") file-content println)))

