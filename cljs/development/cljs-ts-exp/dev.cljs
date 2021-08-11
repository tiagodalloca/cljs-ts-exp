(ns cljs-ts-exp.dev
  (:require [cljs-ts-exp.lib :as lib]
            [malli.core :as m]
            [malli.clj-kondo :as mc]
            ["fs" :as fs]))

(defn n-letter [n]
  (let [alphabet "abcdefghijklmnopqrstuvwxyz"]
    (aget alphabet n)))

(defn collect-lib []
  (-> (mc/collect 'cljs-ts-exp.lib)))

(comment {:ns cljs-ts-exp.lib,
          :name greet,
          :arity 2,
          :args [:string :string],
          :ret :string})

(defn collected-fn-to-ts-export [{:keys [name arity args ret] :as collected-fn}]
  (let [args-name (map n-letter (range arity))]
    (str "export function " name " "
         "(" (clojure.string/join
              ", "
              (map (fn [a t] (str a ": " (clojure.core/name t))) args-name args)) ")"
         ": " (clojure.core/name ret) ";")))

(comment
  (collected-fn-to-ts-export '{:ns cljs-ts-exp.lib,
                               :name greet,
                               :arity 2,
                               :args [:string :string],
                               :ret :string}))

(defn export-lib-str []
  (->> (collect-lib)
       (map collected-fn-to-ts-export)
       (clojure.string/join "\n")))

(defn export-lib! [where]
  (fs/writeFile (str where "lib.d.ts") (export-lib-str) cljs.core/println))

(comment
  (export-lib! "out/"))
