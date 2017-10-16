(ns grapdata.ave40.utils
  (:require [net.cgrand.enlive-html :as enlive]
            [clojure.string :as str]))

(defn lazy-contains? [col key]
  (not (empty? (filter #(= key %) col))))

(defn get-html-node-text [nodes]
  (map (fn [n]
         (str/trim (enlive/text n))) nodes))