(ns grapdata.actuator-test
  (:require [clojure.test :refer :all]
            [grapdata.grap_actuator :refer :all]))

(defn- handlefn []
  (println "test"))

(deftest start-test
  (let [actuator (create-actuator handlefn)]
    (start actuator)))
