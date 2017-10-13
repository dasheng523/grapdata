(ns ave40.grap-image
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [ave40.db :refer :all]
            [net.cgrand.enlive-html :as enlive])
  (:import (java.io StringReader)))


(defn- data-insert!
  [table data]
  (insert-table-data article-db {:table table :cols (keys data) :vals (vals data)}))


(defn- parse-image-url
  "将HTML里面的图片地址提取出来"
  [html]
  (let [nodes (->
                html
                (StringReader.)
                (enlive/html-resource))
        url-nodes (enlive/select nodes [:div.flicr-photo :> :a])]
    (doseq [node url-nodes]
            (let [url (str "http://www.photosforclass.com" (-> node :attrs :href))
                  filename (-> node :attrs :download)]
              (data-insert! "image" {"source_url" url "filename" filename})))))

(defn- fetch-page [n]
  (let [url (str "http://www.photosforclass.com/search/vape/" n)]
    (println url)
    (parse-image-url (:body (http/get url)))))

(defn- do-fetch-all []
  (doseq [n (range 63)]
    (fetch-page (+ n 1))))

