(ns grapdata.ave40.extra
  [:require [dk.ative.docjure.spreadsheet :as sheet]
            [grapdata.ave40.db :refer :all]
            [grapdata.ave40.utils :refer :all]
            [grapdata.ave40.article :as article]
            [grapdata.ave40.grap-article :as grap]
            [net.cgrand.enlive-html :as enlive]
            [clojure.string :as str]
            [clj-http.client :as http]]
  (:import (java.io StringReader)))

(defn parse-excel-data [list]
  (cons (map name (keys (first list))) (map #(vals %) list)))

#_(parse-excel-data (select-all article-db {:table "cms_block" :cols ["block_id" "title" "content"]}))

(defn save-to-excel [data]
  (let [wb (sheet/create-workbook "html" (parse-excel-data data))]
    (sheet/save-workbook! "d:/exponents.xlsx" wb)))

(defn- split-trim [s]
  (str/split s #"\n|\t+")
  #_(filter not-empty (map (fn [pie]
                 (let [temp (str/trim pie)]
                   (if-not (empty? temp) temp))) (str/split s #"\n"))))

(defn run-extra []
  (let [selector [:div.td-module-thumb :a]
        grapper1 (grap/simple-grapper
                   selector
                  #(str "https://www.vapingpost.com/category/politics/page/" %))
        grapper2 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/science/page/" %))
        grapper3 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/business/page/" %))
        grapper4 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/society/page/" %))
        grapper5 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/community/page/" %))
        grapper6 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/deals/page/" %))
        grapper7 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/legal/page/" %))
        grapper8 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/opinion/page/" %))
        grapper9 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/press/page/" %))
        grapper10 (grap/simple-grapper
                   selector
                   #(str "https://www.vapingpost.com/category/review/page/" %))]
    #_(grapper1 1 37)
    #_(grapper2 1 20)
    #_(grapper3 1 15)
    #_(grapper4 1 11)
    (future (do (grapper9 1 13)
                (grapper10 1 11)))
    (future (do (grapper5 1 15)
                (grapper6 1 2)
                (grapper7 1 5)
                (grapper8 1 4)))))

(defn do-parse-article []
  (article/do-parse-and-save {:domain "https://www.vapingpost.com"
                      :selector {:title [:header :> :h1.entry-title]
                                 :article [:div.td-ss-main-content :> :div.td-post-content]}
                      :cond "html like '%entry-title%' and html like '%td-post-content%'"}))