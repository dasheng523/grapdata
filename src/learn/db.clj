(ns learn.db
  (:require [hugsql.core :as hugsql]
            [dbconfig :refer :all]))

(hugsql/def-db-fns "sql/users.sql" {:quoting :mysql})
(hugsql/def-sqlvec-fns "sql/users.sql" {:quoting :mysql})

(defn grap-insert
  [table data]
  (mysql-insert-table-data grap-db {:table table :cols (keys data) :vals (vals data)}))

(defn insert-mail-user
  [email password]
  (mysql-insert-mail-user email-db {:email email :password password}))


(defn create-email-tables
  "create email database tables"
  []
  (mysql-create-domains-table email-db)
  (mysql-create-forwardings-table email-db)
  (mysql-create-users-table email-db)
  (mysql-create-transport-table email-db))


(defn create-grap-tables
  []
  (mysql-create-users-table grap-db))