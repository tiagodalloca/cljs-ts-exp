(ns cljs-ts-exp.lib
  (:require-macros [malli.core :refer [=>]])
  (:require [malli.core :as m]
            [malli.clj-kondo :as mc]))

(defn ^:export square [x] (* x x))
(binding [clojure.core/*ns* 'cljs-ts-exp.lib]
  (m/=> square [:=> [:cat number?] number?]))

(defn ^:export greet
  ([a] (str "Hello there, " a))
  ([a b] (str "Hello there, " a " and " b)))
(binding [clojure.core/*ns* 'cljs-ts-exp.lib]
  (m/=> greet [:function
               [:=> [:cat string?] string?]
               [:=> [:cat string? string?] string?]]))

(defn ^:export say-hi
  [a] (str "Hi, " a "!"))
(binding [clojure.core/*ns* 'cljs-ts-exp.lib]
  (m/=> say-hi [:=> [:cat string?] string?]))

(comment
  (->> 'cljs-ts-exp.lib
       cljs-ts-exp.export.tools/export-lib-str
       (cljs-ts-exp.export.tools/write-export "out/lib.d.ts")))

(def exports
  #js {:square square
       :greet greet
       :say-hi say-hi})

