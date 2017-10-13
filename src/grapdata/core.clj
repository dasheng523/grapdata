(ns grapdata.core
  (:require [ave40.spinner :as sp]
            [ave40.push :as push]
            [ave40.grap-article :as grap]
            [ave40.extra :as extra])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "please enter function:")
  (let [inp (read-line)]
    (cond
      (= inp "spinner") (sp/simple-run-spinner)
      (= inp "push") (push/do-push)
      (= inp "simple-grap") (grap/do-simple-grap)
      (= inp "extra") (extra/run-extra))))

#_(-main)