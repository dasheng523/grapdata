(ns grapdata.core
  (:require [ave40.spinner :as sp])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (sp/simple-run-spinner))

#_(-main)