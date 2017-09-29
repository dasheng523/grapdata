(ns grapdata.main
  (:require [clj-http.client :as http]
            [net.cgrand.enlive-html :as enlive]
            [korma.core :as korma]
            [korma.db :refer [defdb postgres]]
            [me.raynes.fs :as fs])
  (:import (java.io StringReader)))


(defn get-url-html [url]
  (http/get url))


(defdb db (postgres {:db "stock"
                     :user "postgres"
                     :password "a5235013"}))


(declare tongzhouwangshop tongzhouwangdian)
(korma/defentity tongzhouwangshop)
(korma/defentity tongzhouwangdian)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn all-url []
  (for [i (range 1 21)]
    (str "http://zz.bltzw.com/index_c0_d0_p" i ".html?keyword=")))

(defn get-html-data [htmlstr]
  (let [html (enlive/html-resource (StringReader. htmlstr))
        nodes (enlive/select html [:div.bdK :li])]
    (map (fn [n]
           (let [name (-> (enlive/select n [:div.con :div.tit :h3 :a])
                          (first)
                          (enlive/text))
                 phone (-> (enlive/select n [:span.phone])
                           (first)
                           (enlive/text))
                 cate (->> (enlive/select n [:div.con :span.sort :a])
                           (map #(enlive/text %))
                           (clojure.string/join ","))
                 address (-> (enlive/select n [:div.con :div.address])
                             (first)
                             (enlive/text))]
             {:name name :phone phone :cate cate :address address}))
         nodes)))

(defn save-shop-list [shop-list]
  (korma/insert tongzhouwangshop
          (korma/values shop-list)))

(defn doShop []
  (map (fn [url]
         (-> url
             (get-url-html)
             (:body)
             (get-html-data)
             (save-shop-list)))
       (all-url)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn all-dian-url []
  (for [i (range 2 13)]
    (str "http://sj.bltzw.com/c_index_a0_b0_c0_d0_e0_f0_g0_h0_i0_p" i ".html")))

(defn get-dian-data [htmlstr]
  (let [html (enlive/html-resource (StringReader. htmlstr))
        nodes (enlive/select html [:div.company :li])]
    (map (fn [n]
           (let [images (-> (enlive/select n [:div.img :img])
                            (first)
                            (:attrs)
                            (:src))
                 name (-> (enlive/select n [:div.title :h3 :a])
                          (first)
                          (enlive/text))
                 address (-> (enlive/select n [:p.info2])
                             (first)
                             (enlive/text))
                 cate (-> (enlive/select n [:p.info])
                          (first)
                          (enlive/text))
                 url (-> (enlive/select n [:div.img :a])
                         (first)
                         (:attrs)
                         (:href))]
             {:name name :cate cate :address address :img images :url url}))
         nodes)))

(defn get-dian-detail [data]
  (let [url (:url data)
        htmlstr (:body (get-url-html url))
        html (enlive/html-resource (StringReader. htmlstr))]
    (let [phone (-> (enlive/select html [:div.contact :span.tel])
                    (first)
                    (enlive/text))
          qq (-> (enlive/select html [:div.contact :span.qq :a])
                    (first)
                    (enlive/text))
          intro (-> (enlive/select html [:div.jianjie])
                    (first)
                    (enlive/text))
          img (->> (enlive/select html [:ul.xiangce_list :a])
                   (map (fn [imgnode] (-> imgnode :attrs :href)))
                   (#(conj % (:img data)))
                   (clojure.string/join ","))]
      (-> data
          (assoc
            :phone phone
            :qq qq
            :intro intro
            :img img)
          (dissoc :url)))))

(defn fill-dian-detail [list]
  (map (fn [data]
         (get-dian-detail data))
       list))

(defn save-dian-list [shop-list]
  (korma/insert tongzhouwangdian
                (korma/values shop-list)))

(defn doDian []
  (map (fn [url]
         (-> url
             (get-url-html)
             (:body)
             (get-dian-data)
             (fill-dian-detail)
             (save-dian-list)))
       (all-dian-url)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn copy-uri-to-file [uri file]
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))

(defn download-imgs [url]
  (if (.startsWith url "http")
    (let [urlpath (-> url
                      (clojure.string/replace #"http://www.bltzw.com/" "resources/"))
          dir (-> urlpath
                  (clojure.string/split #"/")
                  (pop)
                  (#(clojure.string/join "/" %)))]
      (println dir)
      (if-not (fs/directory? dir)
        (fs/mkdirs dir))
      (if-not (fs/file? urlpath)
        (try
          (copy-uri-to-file url urlpath)
          (catch Exception e
            (println "error:" url)))))))

(defn do-download-images []
  (let [list (korma/select tongzhouwangdian
                           (korma/fields :img))]
    (->
      (map #(-> % (:img) (clojure.string/split #",")) list)
      (#(apply concat %))
      (#(map (fn [url] (download-imgs url)) %)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn update-address [info]
  (let [address (:address info)
        new-address (clojure.string/replace address #"　交通路线：" "")]
    (korma/update tongzhouwangdian
                  (korma/set-fields {:address new-address})
                  (korma/where {:id (:id info)}))))

(defn do-update-address []
  (-> (korma/select tongzhouwangdian)
      ((fn [n]
         (map update-address n)))))


(defn do-update-intro []
  (-> (korma/select tongzhouwangdian)
      ((fn [data]
         (map (fn [info]
                (update-in info
                           [:intro]
                           #(clojure.string/replace % #"...　详细" "")))
              data)))
      ((fn [data]
         (map (fn [info]
                (korma/update tongzhouwangdian
                              (korma/set-fields {:intro (:intro info)})
                              (korma/where {:id (:id info)})))
              data)))))

(defn do-update-img []
  (-> (korma/select tongzhouwangdian)
      ((fn [data]
         (map (fn [info]
                (let [imgs (clojure.string/split (:img info) #",")
                      newimgs (->> imgs
                                   (remove #(if-not (.startsWith % "http:") true false))
                                   (map #(clojure.string/replace % #"http://www.bltzw.com/" ""))
                                   (clojure.string/join ","))]
                  (assoc info :img newimgs)))
              data)))
      ((fn [data]
         (map (fn [info]
                (korma/update tongzhouwangdian
                              (korma/set-fields {:img (:img info)})
                              (korma/where {:id (:id info)})))
              data)))))



(defn main
  "I don't do a whole lot ... yet."
  [& args]
  (doShop))
