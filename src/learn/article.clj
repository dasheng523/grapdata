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

(defn- data-insert!
  [table data]
  (insert-table-data article-db {:table table :cols (keys data) :vals (vals data)}))


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

(defn- push-article-to-blog
  [domain]
  (let [list (select-all article-db {:table "article"})]
    (doseq [info list]
      (push-article domain info))))



(def needto-visit-url (ref #{}))
(def html-list (ref #{}))

#_(add-watch needto-visit-url :need-watch
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


(defn- fetch-to-node [url]
  (-> url
      (http/get)
      :body
      (change-nodes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; grap all website
(defn- next-grap-url []
  (dosync
    (let [v (first @needto-visit-url)]
      (alter needto-visit-url (fn [n] (remove #(= % v) n)))
      v)))

(defn lazy-contains? [col key]
  (some #{key} col))


(defn- visited-urls []
  (map #(get % "url") @html-list))


(defn- ignore-url [url]
  (or (str/ends-with? url ".jpg")
      (str/ends-with? url ".png")
      (not (str/starts-with? url "https://fr.vapingpost.com"))))

(defn- fetch-and-handle [purl]
  (try
    (let [visiteds (visited-urls)]
      (when-not (lazy-contains? visiteds purl)
        (println purl)
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
                                     (ignore-url %)
                                     (lazy-contains? visiteds %))
                                  urls)))
                       ((fn [urls]
                          (map #(-> %
                                    (str/split #"#")
                                    (first)
                                    ((fn [url] (if (str/ends-with? url "/") url (str url "/")))))
                               urls))))]
          (commute needto-visit-url (partial apply conj) urls)
          (alter html-list conj {"url" purl "html" html}))))
    (catch Exception e
      (println (str "caught exception: " (.getMessage e))))))


(defn grap-all-website []
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
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn do-grap-html [url]
  (let [node (fetch-to-node url)
        pagetype (page-type node)]
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;; vaping360
(defn- vaping360-page-url [page]
  (-> (http/post "http://vaping360.com/wp-admin/admin-ajax.php?td_theme_name=Newspaper&v=7.6"
                 {:form-params {:action "td_ajax_block"
                                :td_atts "{\"limit\":\"9\",\"sort\":\"\",\"post_ids\":\"\",\"tag_slug\":\"\",\"autors_id\":\"\",\"installed_post_types\":\"\",\"category_id\":\"\",\"category_ids\":\"-1, -439\",\"custom_title\":\"\",\"custom_url\":\"\",\"show_child_cat\":\"\",\"sub_cat_ajax\":\"\",\"ajax_pagination\":\"infinite\",\"header_color\":\"\",\"header_text_color\":\"\",\"ajax_pagination_infinite_stop\":\"3\",\"td_column_number\":3,\"td_ajax_preloading\":\"preload\",\"td_ajax_filter_type\":\"\",\"td_ajax_filter_ids\":\"\",\"td_filter_default_txt\":\"All\",\"color_preset\":\"\",\"border_top\":\"\",\"class\":\"td_uid_3_59c0de0eb6be6_rand\",\"el_class\":\"\",\"offset\":\"6\",\"css\":\"\",\"tdc_css\":\"\",\"tdc_css_class\":\"td_uid_3_59c0de0eb6be6_rand\",\"live_filter\":\"\",\"live_filter_cur_post_id\":\"\",\"live_filter_cur_post_author\":\"\"}"
                                :td_block_id "td_uid_3_59c0de0eb6be6"
                                :td_column_number 3
                                :td_current_page page
                                :block_type "td_block_200"
                                :td_filter_value ""}})
      :body
      (json/parse-string true)
      :td_data
      (change-nodes)
      (enlive/select [:h3.entry-title :> :a])
      ((fn [data] (map #(-> % :attrs :href) data)))))

(defn get-save-all-vaping360-url []
  (dotimes [n 125]
    (insert-table-tuple
      article-db
      {:table "need_fetch"
       :cols ["url"]
       :datas (map #(conj [] %) (vaping360-page-url (+ n 1)))})))

(defn- fetch-and-save-html [url]
  (let [proxy (str/split (rand-nth proxy-ip) #":")
        resp (http/get url)
        html (:body resp)]
    (if (or (not= 200 (:status resp)))
      (throw (Exception. "访问失败"))
      (data-insert! "source_article2" {"url" url "html" html}))))

(defn fetch-all-vaping360-article []
  (let [list (select-all article-db {:table "need_fetch"})]
    (doseq [info list]
      (println (str "fetch:" (:url info)))
      (fetch-and-save-html (:url info)))))

(defn vaping360-get-html-text [html]
  (let [nodes (change-nodes html)
        title-node (enlive/select nodes [:header.td-post-title :h1.entry-title])
        content-node (enlive/select nodes [:div.vc_column-inner])
        author-node (enlive/select nodes [:div.td-post-author-name :> :a])
        posttime-node (enlive/select nodes [:span.td-post-date :> :time])
        tags-node (enlive/select nodes [:ul.td-category])]
    (when (and (not-empty title-node) (not-empty content-node))
      {:title (-> title-node
                  first
                  (enlive/text))
       :article (-> content-node
                    (#(map (fn [n]
                             (enlive/text n))
                           %))
                    (#(str/join "\n" %)))
       :author (-> author-node
                   first
                   (enlive/text))
       :post_time (-> posttime-node
                      first
                      (enlive/text))
       :tags (-> tags-node
                 (#(map (fn [n]
                          (enlive/text n))
                        %))
                 (#(str/join ", " %)))})))

(defn vaping360-get-all-html-article []
  (let [list (select-all article-db {:table "source_article2"})]
    (doseq [info list]
      (data-insert! "article2"
                    (w/stringify-keys (vaping360-get-html-text
                                        (:html info)))))))



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





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; spinner
(defn- spinner-post [params]
  (let [resp (-> (http/post "http://thebestspinner.com/api.php"
                            {:form-params params})
                 :body
                 (utils/xml->map)
                 :thebestspinner)]
    (if (= "false" (:success resp))
      (throw (Exception. (:error resp)))
      resp)))

(defn- spinner-login []
  (spinner-post {:action "authenticate"
                 :format "xml"
                 :username "515462418@qq.com"
                 :password "40U30600U0034383W"}))

#_(spinner-login)

#_(let [resp (-> (http/post "http://thebestspinner.com/api.php"
                          {:form-params {:action "identifySynonyms"
                                         :session "59ca1708aa1d4"
                                         :format "xml"
                                         :text "Sigelei Foresight 220W Kit Preview | Preparing for future trends"}})
               )]
  resp)


(defn- spinner-synonyms [text session]
  (spinner-post {:action "identifySynonyms"
                 :session session
                 :format "xml"
                 :text text}))

(defn- spinner-sentences [text session]
  (spinner-post {:action "rewriteSentences"
                 :session session
                 :format "xml"
                 :text text}))

(defn- spinner-randomSpin [text session]
  (spinner-post {:action "randomSpin"
                 :session session
                 :format "xml"
                 :text text}))

(defn- spinner-parse [session text]
  (->
    text
    (spinner-synonyms session)
    :output
    (spinner-randomSpin session)
    :output
    (spinner-sentences session)
    :output
    (spinner-randomSpin session)
    :output))

(defn create-spinner []
  (let [session (:session (spinner-login))]
    (partial spinner-parse session)))


(defn run-spinner []
  (let [articles (select-all article-db {:table "article2"})
        spinner (create-spinner)]
    (doseq [article articles]
      (let [title (:title article)
            content (:article article)]
        (println (spinner content))
        #_(update-data-by-id
          article-db
          {:table "article2" :updates {:spinner_title title :spinner_article content} :id (:id article)})))))

#_(run-spinner)

#_(let [article (get-by-id article-db {:table "article2" :id 2})
      content (:article article)
      session "59cb11535b5ca"
      resp (-> (http/post "http://thebestspinner.com/api.php"
                          {:form-params {:action "identifySynonyms"
                                         :session session
                                         :format "xml"
                                         :text content}})
               :body
               #_(utils/parse))]
  resp)




(defn run []
  #_(dosync (fetch-and-handle "https://fr.vapingpost.com"))
  #_(grap-all-website))