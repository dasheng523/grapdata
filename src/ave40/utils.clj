(ns ave40.utils)

(defn lazy-contains? [col key]
  (not (empty? (filter #(= key %) col))))