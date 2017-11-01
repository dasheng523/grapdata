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

(defn get-file-content [path]
  (-> path
      slurp))

(defn parse-info [node]
  {:link (-> node
           (enlive/select [:figure [:a (enlive/nth-child 3)]])
           (enlive/texts)
           first)
   :name (-> node
             (enlive/select [:figure [:a (enlive/nth-child 1)]])
             (enlive/texts)
             first
             (str/trim))
   :desc (-> node
             (enlive/select [:figure :figcaption])
             (first)
             (enlive/text)
             (str/trim))})

(let [node-tree (-> "/Users/huangyesheng/Documents/11.html"
                    (get-file-content)
                    (change-string-to-nodes))
      title-selector (create-default-text-selector [:div.tit :h1])
      title (-> node-tree title-selector)
      atlas-url (-> node-tree ((create-default-href-selector [[:div.container (enlive/nth-child 1)] :span :a])))
      figure-list (-> node-tree
                      (enlive/select [:figure])
                      (->> (map parse-info)))]
  figure-list)


(defn fetch-atlas-pic [url]
  (-> url
      (http/get)
      :body
      (->> (re-find #"sub_images\":([\s\S]+?),\"max_img_width"))
      second
      (json/parse-string true)
      (->> (map :url))))

(count (fetch-atlas-pic "https://www.toutiao.com/a6482162230036005389/#p=1"))

(defn- download-file [url]
  (let [filename (str (quot (System/currentTimeMillis) 1000) (rand 10000) ".png")
        pic (slurp url)]
    (spit (str "/Users/huangyesheng/Documents/" filename) pic)))

(download-file "http://pb3.pstatp.com/origin/42c00002497dde7a96db")

(defn copy [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(copy "http://pb3.pstatp.com/origin/42c00002497dde7a96db" "/Users/huangyesheng/Documents/ddd.png")
