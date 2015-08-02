(ns grapproject.core
  (:require [clojure.core.async :as async :refer [<! >! <!! >!! buffer  go-loop close! alts! timeout chan alt! go]])
  (:gen-class))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;TODO
(def need-to-fetch (chan (buffer 1000)))


(defn add-url-to-chan []
  (go (>! need-to-fetch "test")))



