(ns grapdata.common)

(defn write-file [file]
  (fn [text]
    (with-open [f (clojure.java.io/writer file :append true)]
      (binding [*out* f]
        (println text)))))
