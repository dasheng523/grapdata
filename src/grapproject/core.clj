(ns grapproject.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


(defn fac-cps [n k]
  (letfn [(cont [v] (k (* v n)))]
    (if (zero? n)
      (k 1)
      (recur (dec n) cont))))

(defn fac [n]
  (fac-cps n identity))