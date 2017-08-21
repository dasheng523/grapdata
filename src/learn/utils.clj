(ns learn.utils)

(defn find-first
  [f coll]
  (first (filter f coll)))