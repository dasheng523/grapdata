(ns grapdata.toutiao.grap
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as enlive]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as tf])
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
    {:link (str/trim (link-selector node))
     :title (str/trim (name-selector node))
     :desc (str/trim (desc-selector node))}))

(defn fetch-atlas-pic [content]
  (-> content
      (->> (re-find #"sub_images\":([\s\S]+?),\"max_img_width"))
      second
      (json/parse-string true)
      (->> (map :url))))


(defn download-file [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn- generate-filename
  [suffix]
  (str (quot (System/currentTimeMillis) 1000) (rand-int 1000) suffix))

(defn- time-base-dir [base]
  (let [cformat (tf/formatter "yyyyMMdd")
        timestr (tf/unparse cformat (t/now))
        path (str base "/" timestr "/")]
    path))

(defn- download-toutiao-piture [url]
  (let [filename (str (time-base-dir "/Users/huangyesheng/Documents/pics") (generate-filename ".png"))]
    (io/make-parents filename)
    (download-file url filename)
    filename))



(defn change-pic-md5 [pic-path]
  (with-open [w (io/writer pic-path :append true)]
    (.write w "sdfsdfsf")))


(defn product-item-info [url]
  (let [node-tree (fetch-to-enlive url)
        title-selector (create-default-text-selector [:div.tit :h1])
        toutiao-selector (create-default-href-selector [[:div.container (enlive/nth-child 1)] :span :a])
        title (-> node-tree title-selector)
        atlas-url (-> node-tree toutiao-selector)
        figure-list (-> node-tree
                        (enlive/select [:figure])
                        (->> (map parse-info)))
        pic-list (-> (fetch-atlas-pic (:body (http/get atlas-url)))
                     (->> (map #(download-toutiao-piture (str "http:" %)))))
        goods-list (map #(conj %1 {:pic %2}) figure-list pic-list)]
    {:atitle title :goods goods-list}))

#_(product-item-info "http://www.51taojinge.com/jinri/temai_content_article.php?id=4066141")
