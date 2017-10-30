(ns grapdata.utils
  (:import (java.io PushbackReader FileReader FileWriter File)))

(defn serialize
  "Save a clojure form to a file"
  [file form]
  (with-open [w (FileWriter. (clojure.java.io/file file))]
    (print-dup form w)))

(defn deserialize
  "Load a clojure form from file."
  [file]
  (with-open [r (PushbackReader. (FileReader. (clojure.java.io/file file)))]
    (read r)))

