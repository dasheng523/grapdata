(ns grapproject.core
  (:require [clojure.core.async :as async :refer [<! >! <!! >!!  go-loop close! alts! timeout chan alt! go]])
  (:gen-class))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(def ss (chan))

(defn addchan []
  (go (>! ss "test")))

(defn consumechan []
  (go (spit  "test.log" (<! ss))))
