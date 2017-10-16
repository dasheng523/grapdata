(ns grapdata.ave40.db
  (:require [hugsql.core :as hugsql]))


(def article-db
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1/article"
   :user "root"
   :password "a5235013"
   :sslmode "require"})


(hugsql/def-db-fns "sql/article.sql" {:quoting :mysql})
(hugsql/def-sqlvec-fns "sql/article.sql" {:quoting :mysql})

(defn data-insert!
  [table data]
  (insert-table-data article-db {:table table :cols (keys data) :vals (vals data)}))