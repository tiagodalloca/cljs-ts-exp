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

(defn- underscorefy [fn-name]
  (clojure.string/replace (str fn-name) #"-" "_"))

(defn- to-ts-str-type [k-type]
  (clojure.core/name t))

(defn- collected-fn-to-ts-export [{:keys [name arity args ret] :as collected-fn}]
  (let [fn-name name
        args-name (map n-letter (range arity))
        joined-args (->>
                     (map (fn [arg-name arg-type] (str arg-name  ": " (to-ts-str-type arg-type)))
                          args-name args)
                     (clojure.string/join ", "))
        ret-type-str (to-ts-str-type ret)]

    (str "export function " (underscorefy fn-name) " (" joined-args ")"
         ": " ret-type-str ";")))

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

