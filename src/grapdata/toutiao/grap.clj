(ns grapdata.toutiao.grap
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as enlive]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import (java.io StringReader)))

(defn- change-string-to-nodes [s]
  (-> s
      (StringReader.)
      (enlive/html-resource)))

(defn- fetch-to-enlive [url]
  (-> url
      (http/get)
      :body
      (change-string-to-nodes)))

(defn- read-file-enlive [path]
  (-> path
      slurp
      (change-string-to-nodes)))

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


(defn parse-info [node]
  (let [link-selector (create-default-text-selector [:figure [:a (enlive/nth-child 3)]])
        name-selector (create-default-text-selector [:figure [:a (enlive/nth-child 1)]])
        desc-selector (create-default-text-selector [:figure :figcaption])]
    {:link (link-selector node)
     :name (name-selector node)
     :desc (desc-selector node)}))

(let [node-tree (read-file-enlive "f:\\111.html")
      title-selector (create-default-text-selector [:div.tit :h1])
      toutiao-selector (create-default-href-selector [[:div.container (enlive/nth-child 1)] :span :a])
      title (-> node-tree title-selector)
      atlas-url (-> node-tree toutiao-selector)
      figure-list (-> node-tree
                      (enlive/select [:figure])
                      (->> (map parse-info)))]
  (count figure-list))


(defn fetch-atlas-pic [content]
  (-> content
      (->> (re-find #"sub_images\":([\s\S]+?),\"max_img_width"))
      second
      (json/parse-string true)
      (->> (map :url))))

(count (fetch-atlas-pic "https://www.toutiao.com/a6482162230036005389/#p=1"))

(defn download-file [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn- create-filename
  []
  (quot (System/currentTimeMillis) 1000))

(download-file "http://pb3.pstatp.com/origin/42c00002497dde7a96db" "/Users/huangyesheng/Documents/ddd.png")
