(ns grapdata.core
  (:require #_[grapdata.ave40.spinner :as sp]
            #_[grapdata.ave40.push :as push]
            #_[grapdata.ave40.grap-article :as grap]
            #_[grapdata.ave40.extra :as extra]
            [grapdata.toutiao.logic :as logic])
  (:gen-class))

(defn- run-toutiao []
  (println "please enter user code...")
  (logic/run (read-line)))

(defn- run-save []
  (println "please enter user code after login...")
  (logic/do-save-cookies read-line))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "enter 1 or 2")
  (let [inp (read-line)]
    (case inp
      "1" (run-toutiao)
      "2" (run-save))))

#_(let [inp (read-line)]
  (case inp
    "spinner" (sp/simple-run-spinner)
    "push" (push/do-push)
    "simple-grap" (grap/do-simple-grap)
    "extra" (extra/run-extra)
    "11" (println 1)))

