(ns cljs-ts-exp.export.tools

  (:require [cljs-ts-exp.lib :as lib]
            [malli.core :as m]
            [malli.clj-kondo :as mc]
            ["fs" :as fs]))

(defn- n-letter [n]
  (let [alphabet "abcdefghijklmnopqrstuvwxyz"]
    (aget alphabet n)))

(defn- collect-lib [ns-sym]
  (-> (mc/collect ns-sym)))

(defn- collected-fn-to-ts-export [{:keys [name arity args ret] :as collected-fn}]
  (let [args-name (map n-letter (range arity))]
    (str "export function " (clojure.string/replace (str name) #"-" "_") " "
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

(defn export-lib-str [ns-sym]
  (->> (collect-lib ns-sym)
       (map collected-fn-to-ts-export)
       (clojure.string/join "\n")))

(defn write-export [where export-str]
  (fs/writeFile where export-str identity))

