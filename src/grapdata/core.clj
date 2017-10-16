(ns grapdata.core
  (:require [grapdata.ave40.spinner :as sp]
            [grapdata.ave40.push :as push]
            [grapdata.ave40.grap-article :as grap]
            [grapdata.ave40.extra :as extra])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (sp/simple-run-spinner))

#_(let [inp (read-line)]
  (case inp
    "spinner" (sp/simple-run-spinner)
    "push" (push/do-push)
    "simple-grap" (grap/do-simple-grap)
    "extra" (extra/run-extra)
    "11" (println 1)))