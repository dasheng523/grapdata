
(defn fac-cps [n k]
  (letfn [(cont [v] (k (* v n)))]
    (if (zero? n)
      (k 1)
      (recur (dec n) cont))))

(defn fac [n]
  (fac-cps n identity))

(defn dddd [coll n]
  (get (group-by identity coll) n))

(defn testloop [_]
  (loop []
    (println 111)
    (recur)))