(ns cljs-ts-exp.dev.exp4
  (:require [cljs-ts-exp.export.parse :refer [parse]]
            [cljs-ts-exp.dev.exp1 :as exp1]
            [malli.core :as m]
            [malli.registry :as mr]
            ["fs" :as fs]))

(comment
  (exp1/register-schema!
   :flow/person
   [:map [:name :string] [:age :int]])

  (exp1/register-schema!
   :flow/company
   [:map [:name :string] [:people [:set :flow/person]]])
  
  
  (let [file->content
        (parse
         [[:flow/person {:name "FlowPerson"
                         :file "flow/index.d.ts"}]
          [:flow/company {:name "FlowCompany"
                          :file "flow/company/index.d.ts"}]] 
         {:export-default true
          :files-import-alias {"flow/index.d.ts" "flow"
                               "flow/company/index.d.ts" "fCompany"}})]
    
    (doseq [[file content] file->content]
      (println
       (str "-- "file " --" \newline
            content \newline)))))

