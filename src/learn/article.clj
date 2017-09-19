(ns learn.article
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [hugsql.core :as hugsql]
            [net.cgrand.enlive-html :as enlive]
            [clojure.core.async :as async]
            [clojure.data]
            [cheshire.core :as json])
  (:import (java.io StringReader)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 数据库相关
(def article-db
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1/article"
   :user "root"
   :password "a5235013"
   :sslmode "require"})

(hugsql/def-db-fns "sql/article.sql" {:quoting :mysql})

(defn- data-insert!
  [table data]
  (insert-table-data article-db {:table table :cols (keys data) :vals (vals data)}))

(defn- save-html
  "保存网页内容"
  [url html]
  (data-insert! "source_article" {"url" url "html" (:body html)}))


(def needto-visit-url (atom #{}))
(def article-list (ref #{}))

(add-watch needto-visit-url :need-watch
           (fn [_ _ old new]
             (if (> (count new) (count old))
               (if-let [diff (first (clojure.data/diff new old))]
                 (insert-table-tuple
                   article-db
                   {:table "need_fetch"
                    :cols ["url"]
                    :datas (map #(conj [] %) (remove nil? diff))})))))


(add-watch article-list :article-watch
           (fn [_ _ old new]
             (if (> (count new) (count old))
               (if-let [diff (remove nil? (first (clojure.data/diff new old)))]
                 (insert-table-tuple
                   article-db
                   {:table "source_article"
                    :cols (keys diff)
                    :datas (map #(vals %) diff)})))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 文章相关
(defn- change-nodes [html]
  (->
    html
    (StringReader.)
    (enlive/html-resource)))


(defn change-article
  "将一篇文章转成另一篇文章"
  [text]
  (Thread/sleep (* 1000 3))
  (let [resp (->
               (http/post
                 "http://wordai.com/users/turing-api.php"
                 {:form-params {:s text
                                :quality "Readable"
                                :email "327220796@qq.com"
                                :hash "7785dd959e67f57afa766d6aecea2629"
                                :output "json"
                                :nooriginal "on"
                                :nonested  "on"}})
               :body
               (json/parse-string true))]
    (if (= (:status resp) "Failure")
      (throw (Exception. (:error resp)))
      (-> resp
          :text
          (str/replace #"\<p\>" "")
          (str/replace #"\</p\>" "")
          (str/replace #"\{ \}" "")
          (str/replace #"\{\}" "")))))

(defn- wrap-paragraph [text]
  (apply str (map #(apply str (enlive/emit* ((enlive/wrap :p) %)))
                  (str/split text #"\n"))))

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

(defn- push-article-to-blog
  [domain]
  (let [list (select-all article-db {:table "article"})]
    (doseq [info list]
      (push-article domain info))))

#_(push-article-to-blog "www.vaping10.com")


(defn- parse-save-articles
  []
  (let [html-list (get-all-article-html article-db)]
    (doseq [info html-list]
      (let [html (:html info)
            url (:url info)]
        (when-not (get-article-by-url article-db {:url url})
          (println url)
          (when-let [original-article (parse-article html)]
            (data-insert! "article" {"url" url
                                     "article" (wrap-paragraph (change-article (:article original-article)))
                                     "title" (str/trim (change-article (:title original-article)))})))))))
(defn run []
  (while true
    (try
      (parse-save-articles)
      (catch Exception e
        (println (.getMessage e)))
      (finally
        (Thread/sleep (* 1000 60 10))))))


