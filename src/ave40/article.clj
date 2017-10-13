(ns ave40.article
  (:require [clj-http.client :as http]
            [ave40.db :refer :all]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as enlive]
            [clojure.walk :as w])
  (:import (java.io StringReader)))

(defn- export-to-cvs
  [list]
  (spit "d:/sss.csv"
        (str/join "\n"
                  (map (fn [n]
                         (str/join "," (map (fn [[k v]] (str "\"" v "\"")) n)))
                       list))))




(defn create-parser [selectors]
  "从html提取信息"
  (fn [html]
    (reduce
      conj
      {}
      (for [[k selector] selectors]
        (let [html-nodes (-> html
                             (StringReader.)
                             (enlive/html-resource))]
          {k (-> html-nodes
                 (enlive/select selector)
                 (#(map (fn [n]
                          (enlive/text n))
                        %))
                 (#(str/join "\n" %)))})))))


(defn do-parse-and-save [{:keys [domain selector cond]}]
  (let [source-list (select-all article-db
                                {:table "source_article"
                                 :where (str "url like '" domain "%' and " cond)})
        parser (create-parser selector)]
    (println (str "total:" (count source-list)))
    (doseq [source source-list]
      (data-insert! "articles"
                    (w/stringify-keys
                      (merge (parser (:html source))
                             {:source_url (:url source)
                              :grap_time (:created_at source)}))))))



#_(do-parse-and-save {:domain "http://www.vaporvanity.com"
                    :selector {:title [:header.entry-header :h1.entry-title]
                               :article [:main.site-main :article #{:p :h2}]}
                    :cond "html like '%entry-header overlay%' and html like '%<article%'"})

#_(do-parse-and-save {:domain "http://vaping360.com"
                    :selector {:title [:header.td-post-title :h1.entry-title]
                               :article [:div.vc_column-inner]}
                    :cond "html like '%entry-title%' and html like '%td-post-content%'"})


