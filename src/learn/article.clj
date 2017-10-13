(ns learn.article
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [hugsql.core :as hugsql]
            [net.cgrand.enlive-html :as enlive]
            [clojure.core.async :as async]
            [clojure.data]
            [clojure.walk :as w]
            [cheshire.core :as json]
            [learn.utils :as utils]
            [clojure.set :as s])
  (:import (java.io StringReader)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 代理IP
(def proxy-ip
  ["107.172.237.205"
   "155.94.170.116"
   "173.44.166.162"
   "104.247.121.136"
   "69.12.79.156"
   "196.19.115.190"
   "69.12.79.185"
   "155.94.170.26"
   "196.19.115.69"
   "173.44.166.8"
   "155.94.170.252"
   "172.245.251.251"
   "192.210.194.142"
   "172.245.251.222"
   "172.245.10.55"
   "204.44.77.225"
   "45.59.156.136"])

(defn proxy-post [url params]
  (http/post url (merge params
                        {:proxy-host (rand-nth proxy-ip)
                         :proxy-port 80
                         :proxy-user "lsegura"
                         :proxy-pass "avenue40"})))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 数据库相关
(def article-db
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1/article"
   :user "root"
   :password "a5235013"
   :sslmode "require"})

(hugsql/def-db-fns "sql/article.sql" {:quoting :mysql})
(hugsql/def-sqlvec-fns "sql/article.sql" {:quoting :mysql})




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 文章相关
(defn- change-nodes [html]
  (->
    html
    (StringReader.)
    (enlive/html-resource)))


(defn- wrap-paragraph [text]
  (apply str (map #(apply str (enlive/emit* ((enlive/wrap :p) %)))
                  (str/split text #"\n"))))



(defn- create-article-parser [html ])

(defn- parse-article
  "将HTML里面的文章提取出来"
  [html]
  (let [nodes (change-nodes html)
        title-node (enlive/select nodes [:header.entry-header :h1.entry-title])
        content-node (enlive/select nodes [:div.entry-content :> :p])]
    (when (and (not-empty title-node) (not-empty content-node))
      {:title (-> title-node
                  first
                  (enlive/text))
       :article (-> content-node
                    (#(map (fn [n]
                             (enlive/text n))
                           %))
                    (#(str/join "\n" %)))})))


(defn- push-article
  "将文章推送到指定的博客"
  [domain article-info]
  (println (http/post "http://manage.ecigview.com/posts/create"
                      {:form-params {:domain domain
                                     :title (:title article-info)
                                     :content (:article article-info)}})))

(defn- add-image-to-post
  [info]
  (let [url (:source_url (select-rand-image article-db))]
    (update info :article #(str "<img src=\"" url "\" style=\"display:block;\" />" %))))

(defn- push-article-to-blog
  [domain]
  (let [list (select-article2-limit-10 article-db)]
    (doseq [info list]
      (push-article domain (add-image-to-post info)))))

#_(push-article-to-blog "www.ecigblog.in")

(def needto-visit-url (ref []))
(def html-list (atom #{}))

(defn- add-to-need-visit [url]
  (dosync
    (if-not (contains? @needto-visit-url url)
      (alter needto-visit-url conj url))))

(defn- get-need-visit []
  (dosync
    (alter needto-visit-url pop)))

(add-watch needto-visit-url :need-watch
           (fn [_ _ old new]
             (if (< (count new) (count old))
               (if-let [diff (first (clojure.data/diff old new))]
                 (delete-table-data-by-url
                   article-db
                   {:table "need_fetch"
                    :urls (remove nil? diff)})))
             (if (> (count new) (count old))
               (if-let [diff (first (clojure.data/diff new old))]
                 (insert-table-tuple
                   article-db
                   {:table "need_fetch"
                    :cols ["url"]
                    :datas (map #(conj [] %) (remove nil? diff))})))))

(add-watch html-list :article-watch
           (fn [_ _ old new]
             (if (> (count new) (count old))
               (if-let [diff (remove nil? (first (clojure.data/diff new old)))]
                 (insert-table-tuple
                   article-db
                   {:table "source_article3"
                    :cols (keys (first diff))
                    :datas (map #(vals %) diff)})))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; grap all website

(defn- ignore-url [url]
  (or (str/ends-with? url ".jpg")
      (str/ends-with? url ".png")
      (not (str/starts-with? url "https://fr.vapingpost.com"))))

(defn- fetch-and-handle [purl]
  (try
    (let [html (-> purl
                   (http/get)
                   :body)
          urls (-> html
                   (change-nodes)
                   (enlive/select [:a])
                   ((fn [a-nodes] (map #(-> % :attrs :href) a-nodes)))
                   ((fn [urls]
                      (remove #(or
                                 (nil? %)
                                 (ignore-url %))
                              urls)))
                   ((fn [urls]
                      (map #(-> %
                                (str/split #"#")
                                (first)
                                ((fn [url] (if (str/ends-with? url "/") url (str url "/")))))
                           urls))))]
      (commute needto-visit-url (partial apply conj) urls)
      (alter html-list conj {"url" purl "html" html}))
    (catch Exception e
      (println (str "caught exception: " (.getMessage e))))))


#_(defn grap-all-website []
  (loop [url (next-grap-url)]
    (when url
      (dosync (fetch-and-handle url))
      (recur (next-grap-url)))))



#_(dosync (fetch-and-handle "https://fr.vapingpost.com"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; list and article
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- page-type [html-node]
  true)

(defn- get-content-url [html-node]
  )

(defn- next-list-url [html-node]
  )

(defn- fetch-to-node [url]
  (-> url
      (http/get)
      :body
      (change-nodes)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn do-grap-html [url]
  (let [node (fetch-to-node url)
        pagetype (page-type node)]
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 导出
(defn- export-to-cvs
  [list]
  (spit "d:/sss.csv"
        (str/join "\n\n[NEW]\n\n"
                  (map (fn [n]
                         (str/join "\n" (map (fn [[k v]] (str (name k) ": " v)) n)))
                       list))))

(defn export-all-wordai-article []
  (let [html-list (get-all-article-html article-db)]
    (export-to-cvs (remove nil? (map (fn [n] (parse-article (:html n))) html-list)))))

(defn export-all-vaping360-article []
  (let [html-list (select-all article-db {:table "article2"})]
    (export-to-cvs html-list)))

