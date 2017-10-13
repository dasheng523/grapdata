(ns ave40.extra
  [:require [dk.ative.docjure.spreadsheet :as sheet]
            [ave40.db :refer :all]
            [ave40.utils :refer :all]
            [ave40.grap-article :as grap]
            [net.cgrand.enlive-html :as enlive]
            [clojure.string :as str]]
  (:import (java.io StringReader)))

(defn parse-excel-data [list]
  (cons (map name (keys (first list))) (map #(vals %) list)))

#_(parse-excel-data (select-all article-db {:table "cms_block" :cols ["block_id" "title" "content"]}))

#_(let [data (select-all article-db {:table "cms_block" :cols ["block_id" "title" "content"]})
      wb (sheet/create-workbook "html" (parse-excel-data data))]
  (sheet/save-workbook! "d:/exponents.xlsx" wb))

(defn- split-trim [s]
  (str/split s #"\n|\t+")
  #_(filter not-empty (map (fn [pie]
                 (let [temp (str/trim pie)]
                   (if-not (empty? temp) temp))) (str/split s #"\n"))))

#_(let [list (map :content
                (select-all article-db
                            {:table "cms_block" :cols ["block_id" "title" "content"]}))]
  (apply concat (map (fn [html]
                       (-> html
                           (StringReader.)
                           (enlive/html-resource)
                           (get-html-node-text)
                           (#(mapcat split-trim %))))
                     list)))

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
    (grapper1 1 37)
    (grapper2 1 20)
    (grapper3 1 15)
    (grapper4 1 11)
    (grapper5 1 15)
    (grapper6 1 1)
    (grapper7 1 5)
    (grapper8 1 4)
    (grapper9 1 13)
    (grapper9 1 11)))

(run-extra)