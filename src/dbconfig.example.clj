(ns dbconfig)

(def grap-db
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1/grap"
   :user "root"
   :password "a5235013"
   :sslmode "require"})

(def email-db
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1/mail"
   :user "root"
   :password "a5235013"
   :sslmode "require"})