(ns grapdata.ave40.extra
  [:require [dk.ative.docjure.spreadsheet :as sheet]
            [clojure.data.json :as json]
            [net.cgrand.enlive-html :as enlive]
            [clojure.data :as data]
            [clojure.string :as str]
            [clj-http.client :as http]
            [grapdata.ave40.db :refer :all]
            [grapdata.ave40.utils :refer :all]
            [grapdata.ave40.article :as article]
            [grapdata.ave40.grap-article :as grap]
            [clojure.java.io :as io]]
  (:import (java.io StringReader)))

(defn- parse-excel-data [list]
  (cons (map name (keys (first list))) (map #(vals %) list)))

#_(parse-excel-data (select-all article-db {:table "cms_block" :cols ["block_id" "title" "content"]}))

(defn save-to-excel [data]
  (let [wb (sheet/create-workbook "html" (parse-excel-data data))]
    (sheet/save-workbook! "d:/exponents.xlsx" wb)))

(defn list-dir [path]
  (file-seq (io/file path)))

(defn- split-trim [s]
  (str/split s #"\n|\t+")
  #_(filter not-empty (map (fn [pie]
                 (let [temp (str/trim pie)]
                   (if-not (empty? temp) temp))) (str/split s #"\n"))))

(defn run-extra []
  (let [grapper (grap/simple-grapper
                  (grap/create-default-selector [:div.node :h2.title :a] "http://www.autoexpress.co.uk")
                  #(str "http://www.autoexpress.co.uk/car-news/page/" % "/0"))]
    (future (grapper 1 200))
    (future (grapper 200 400))
    (future (grapper 400 600))
    (future (grapper 600 860))))

(defn do-parse-article []
  (article/do-parse-and-save {:domain "https://www.vapingpost.com"
                      :selector {:title [:header :> :h1.entry-title]
                                 :article [:div.td-ss-main-content :> :div.td-post-content]}
                      :cond "html like '%entry-title%' and html like '%td-post-content%'"}))


(defn find-all-file [dir-path]
  (let [dir-seq (file-seq (io/file dir-path))
        phtml-list (filter #(and (.isFile %)
                                 (.endsWith (.getName %) ".phtml")) dir-seq)
        ready-list (-> (slurp "D:\\Ave40_Translate.csv")
                 (str/split #"\r\n")
                 (#(map (fn [s]
                          (first (str/split (str/replace s #"\"" "") #","))) %)))]
    (save-to-excel
      (into #{} (remove #(and (nil? %) (lazy-contains? ready-list %))
                        (for [item phtml-list]
                          (if-let [ffind (re-find #"\$this->__\((['\"])([^\n]+?)\1\)" (slurp item))]
                            (conj (zipmap ["source" "quoti" "value"] ffind)
                                  {"file" (.getAbsolutePath item)}))))))))

#_(find-all-file "E:\\ave40_mg\\app\\design\\frontend\\default\\se105")

(defn- get-ready-translate-list []
  (-> (slurp "D:\\Ave40_Translate.csv")
      (str/split #"\r\n")
      (->> (map #(first (str/split (str/replace % #"\"" "") #","))))
      (->> (into #{}))))


(defn find-match-file [path]
  (let [phtml-list (-> (list-dir path)
                       (->> (filter #(.endsWith (.getName %) ".phtml"))))
        ready-list (get-ready-translate-list)]
    (->> phtml-list
         (mapcat #(re-seq #"Mage::helper\(\"Ave40_Translate\"\)->__\((['\"])([^\n\$]+?)\1\)" (slurp %)))
         #_(map #(get % 2))
         (into #{})
         (remove #(or (nil? %) (lazy-contains? ready-list (get % 2)))))))

(defn save-match-data [data]
  (let [wb (sheet/create-workbook "html" data)]
    (sheet/save-workbook! "d:/data.xlsx" wb)))

#_(-> "E:\\ave40_mg\\app\\design\\frontend\\default\\se105"
    find-match-file
    save-match-data)



(defn replace-all-file [dir-path]
  (let [dir-seq (file-seq (io/file dir-path))
        phtml-list (filter #(and (.isFile %)
                                 (.endsWith (.getName %) ".phtml")) dir-seq)]
    (doseq [item phtml-list]
      (spit (.getAbsolutePath item)
            (str/replace (slurp item) #"\$this->__" "Mage::helper(\"Ave40_Translate\")->__")))))


#_(replace-all-file "E:\\ave40_mg\\app\\design\\frontend\\default\\se105")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;  导入产品数据


(defn copy-images []
  (let [dir-seq (file-seq (io/file "E:\\fayusource"))
        phtml-list (filter #(and (.isFile %)
                                 (or (.endsWith (.getName %) ".jpg")
                                     (.endsWith (.getName %) ".gif")
                                     (.endsWith (.getName %) ".png"))) dir-seq)]
    (doseq [file phtml-list]
      (let [filename (-> (.getAbsolutePath file)
                         (str/split #"\\")
                         (get 2)
                         (str/replace #" " "-")
                         (subs 0 10)
                         (str "_" (.getName file)))]
        (println filename)
        (io/copy file (io/file (str "E:\\fayuimages\\" filename)))))))

#_(copy-images)

(defn- do-one-html [file]
  (let [content (slurp file :encoding "gb2312")
        filename (.getName file)
        incontent (-> content
                      (StringReader.)
                      (enlive/html-resource)
                      (enlive/select [:div.WordSection1])
                      (first)
                      :content
                      (enlive/emit*)
                      (->> (apply str)))
        piclist (-> incontent
                    (->> (re-seq #"<img[^<>]*src=\"(.*?)\"[^<>]*>"))
                    (->> (map #(get % 1))))]
    (spit (str "E:\\fayuhtml\\" filename)
          (reduce (fn [s picurl]
                    (let [newpicurl (-> (str/replace picurl #"%20" "-")
                                        (subs 0 10)
                                        (str "_" (second (str/split picurl #"/")))
                                        (->> (str "http://www.demo.ave40.com/media/Frenchimages/")))]
                      (str/replace s picurl newpicurl))) incontent piclist))))
(defn copy-htmls []
  (let [dir-seq (file-seq (io/file "E:\\fayusource"))
        phtml-list (filter #(and (.isFile %)
                                 (.endsWith (.getName %) ".htm")) dir-seq)]
    (doseq [file phtml-list]
      (do-one-html file))))

#_(copy-htmls)

(def ave40-db
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1/ttttt"
   :user "root"
   :password "a5235013"
   :sslmode "require"})

(defn- find-product-id [s]
  (let [sku (-> (str/replace s #"_" "-")
                (str/split #"-")
                (second))]
    (if sku (:entity_id (select-one ave40-db {:table "catalog_product_entity"  :where (str "sku='" sku "'")})))))

#_(find-product-id "FR-1080-Wismec_Predator_228W_TC_Kit_with_Elabo")

(defn- insert-product-data []
  (doseq [file (filter #(.endsWith (.getName %) ".htm")
                       (file-seq (io/file "E:\\fayuhtml")))]
    (let [filename (.getName file)
          filecontent (slurp file)
          id (find-product-id filename)
          name (-> filecontent
                   (StringReader.)
                   (enlive/html-resource)
                   (enlive/select [#{:p.LO-Normal :p.MsoNormal} :b :span])
                   (first)
                   (enlive/text)
                   (str/replace #"\n" " "))
          desc-data {"entity_type_id" 4
                     "attribute_id" 72
                     "store_id" 4
                     "entity_id" id
                     "value" filecontent}
          title-data {"entity_type_id" 4
                      "attribute_id" 71
                      "store_id" 4
                      "entity_id" id
                      "value" name}]
      (when id
        (try
          (println [id filename])
          (insert-table-data ave40-db {:table "catalog_product_entity_text"
                                       :cols (keys desc-data) :vals (vals desc-data)})
          (insert-table-data ave40-db {:table "catalog_product_entity_varchar"
                                       :cols (keys title-data) :vals (vals title-data)})
          (catch Exception e
            (println (str "error: " [id filename]))))))))




#_(-> (select-all ave40-db {:table "cms_block" :where "`content` NOT LIKE '{{block%'"})
    (->> (mapcat (fn [info]
                   (-> info
                       :content
                       (StringReader.)
                       (enlive/html-resource)
                       (->> (map #(-> (enlive/text %) (str/trim))))
                       (->> (mapcat #(str/split % #"\n")))
                       (->> (mapcat #(str/split % #"\/")))
                       (->> (mapcat #(str/split % #"  \|  ")))
                       (->> (map (fn [s] (str/trim s))))
                       (->> (remove #(= "" %)))
                       (->> (map (fn [x] {:words x :block_id (:block_id info)})))))))
    (save-to-excel))


#_(let [data (->> (sheet/load-workbook "f:\\blocks.xlsx")
                (sheet/select-sheet "Sheet1")
                (sheet/select-columns {:A :block_id, :B :content :C :fr}))]
  (doseq [{:keys [block_id content fr]} data]
    (let [id (int block_id)
          mydata (select-one ave40-db {:table "cms_block" :where (str "block_id=" id)})
          my-content (:content mydata)
          new-content (str/replace my-content content (str "{{t t=\"" content "\"}}"))]
      (update-data ave40-db {:table "cms_block"
                             :where (str "block_id=" block_id)
                             :updates {:content new-content}}))))


#_(let [data (->> (sheet/load-workbook "f:\\blocks.xlsx")
                (sheet/select-sheet "Sheet1")
                (sheet/select-columns {:A :block_id, :B :content :C :fr})
                (map #(str "\"" (:content %) "\"," "\"" (:fr %) "\""))
                (str/join "\n"))]
  (spit "f:\\ddd.csv" data))