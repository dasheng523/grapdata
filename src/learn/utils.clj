(ns learn.utils
  (:require [clojure.xml])
  (:import (java.io ByteArrayInputStream)))

(defn find-first
  [f coll]
  (first (filter f coll)))

(defn parse [s] (clojure.xml/parse (ByteArrayInputStream. (.getBytes s))))

(defn- different-keys? [content]
  (when content
    (let [dkeys (count (filter identity (distinct (map :tag content))))
          n (count content)]
      (= dkeys n))))


(defn- xml-element->json [element]
  (cond
    (nil? element) nil
    (string? element) element
    (sequential? element) (if (> (count element) 1)
                            (if (different-keys? element)
                              (reduce into {} (map (partial xml-element->json ) element))
                              (map xml-element->json element))
                            (xml-element->json  (first element)))
    (and (map? element) (empty? element)) {}
    (map? element) (if (:attrs element)
                     {(:tag element) (xml-element->json (:content element))
                      (keyword (str (name (:tag element)) "Attrs")) (:attrs element)}
                     {(:tag element) (xml-element->json  (:content element))})
    :else nil))

(defn xml->map [s]
  (xml-element->json (parse s)))