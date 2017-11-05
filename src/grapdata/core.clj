(ns grapdata.core
  (:require #_[grapdata.ave40.spinner :as sp]
            #_[grapdata.ave40.push :as push]
            #_[grapdata.ave40.grap-article :as grap]
            #_[grapdata.ave40.extra :as extra]
            [grapdata.toutiao.logic :as logic])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (logic/run))

#_(let [inp (read-line)]
  (case inp
    "spinner" (sp/simple-run-spinner)
    "push" (push/do-push)
    "simple-grap" (grap/do-simple-grap)
    "extra" (extra/run-extra)
    "11" (println 1)))
