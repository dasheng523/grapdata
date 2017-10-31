(ns grapdata.toutiao.grap
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as enlive]
            [clojure.tools.logging :as log])
  (:import (java.io StringReader)))


"http://www.51taojinge.com/jinri/temai_content_article.php?id=4060779"

(defn- fetch-to-enlive [url]
  (-> url
      (http/get)
      :body
      (StringReader.)
      (enlive/html-resource)))

(defn create-default-text-selector [node-select]
  (fn [nodes]
    (str/join
      "\n"
      (map (fn [n]
             (enlive/text n))
           (enlive/select nodes node-select)))))

(defn create-default-href-selector [node-select]
  (fn [nodes]
    (let [find-rs (enlive/select nodes node-select)]
      (if-not (empty? find-rs)
        (-> find-rs first :attrs :href)))))


(let [node-tree (fetch-to-enlive "http://www.51taojinge.com/jinri/temai_content_article.php?id=4060779")
      title-selector (create-default-text-selector [:div.tit :h1])
      title (-> node-tree title-selector)
      url (-> node-tree ((create-default-href-selector [[:div.container (enlive/nth-child 1)] :a])))
      figure-node (-> node-tree (enlive/select [:figure]))]
  (-> figure-node
      first
      (enlive/select [:a])))