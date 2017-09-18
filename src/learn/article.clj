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
               (if-let [diff (first (clojure.data/diff new old))]
                 (insert-table-tuple
                   article-db
                   {:table "source_article"
                    :cols (keys diff)
                    :datas (map #(vals %) diff)})))))

(defn- reset-data
  []
  (let [need-visit (get-all-needfetch article-db)
        articles (get-all-urlhtml article-db)]
    (reset! needto-visit-url (set need-visit))
    (async/go (doseq [info need-visit]
                (async/>! needto-visit-url (:url info))))
    (dosync (doseq [info articles]
              (alter visited-url conj (:url info))))))




(defn- push-urls!
  [urls]
  (doseq [url urls] (swap! needto-visit-url conj url)))

(defn- pop-url!
  []
  (let [v (pop (vec @needto-visit-url))]
    (swap! needto-visit-url disj v)
    v))


(defn- is-visited
  "是否已经访问过"
  [url]
  (contains? @visited-url (first (str/split url #"#"))))

(defn- set-visited
  "将Url设置为已访问"
  [url]
  (alter visited-url conj url))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 文章相关
(defn- change-nodes [html]
  (->
    html
    (StringReader.)
    (enlive/html-resource)))


(defn change-article
  "将一篇文章转成另一篇文章"
  [text]
  (Thread/sleep (* 1000 30))
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


(parse-save-articles)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 抓取网页
(defn- handle-html [url]
  (println "fetching:" url)
  (try
    (dosync
      (when-not (is-visited url)
        (set-visited url)
        (let [html (http/get url)
              html-node (change-nodes (:body html))
              domain (subs url 0 (str/index-of url "/" 7))
              unvisit-url (filter
                            #(and (.startsWith % domain)
                                  (not (str/index-of % "com/inbound"))
                                  (not (is-visited %))
                                  (not= url %))
                            (into #{} (map #(-> % :attrs :href) (enlive/select html-node [:a]))))]
          (save-html url html)
          (when (not-empty unvisit-url)
            (push-urls! unvisit-url)))))
    (catch Exception e
      (data-insert! "source_article" {"url" url "html" (str "caught exception: " (.getMessage e))})
      (println (str "caught exception: " (.getMessage e)))))
  (println "done:" url))


(handle-html "http://www.vaporvanity.com/")

(defn- run-task [url]
  (try
    (when url
      (handle-html url)
      (pop-url!))
    (finally (send-off *agent* run-task))))

(defn start-task [n]
  (reset-data)
  (let [agents (repeatedly n #(agent (pop-url!)))]
    (doseq [a agents] (send-off a run-task))))

#_(start-task 10)

(get-all-article-html article-db)
