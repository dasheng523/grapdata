(ns grapdata.model
  (:use [korma.db]
        [korma.core])
  (:import (java.util Date)))

(defdb bigdata-db (postgres {:db "bidata"
                             :user "postgres"
                             :password "a5235013"}))



(defdb stock-db (postgres {:db "stock"
                           :user "postgres"
                           :password "a5235013"}))


(defentity grapdomains
           (database bigdata-db))

(defentity grapdatas
           (database bigdata-db))

(defn insert-grapdomain [domain-info]
  (-> (insert* grapdomains)
      (values domain-info)
      (insert)))


;TODO 这里一直连不上数据库
(defn test-insert-grapdomain []
  (insert-grapdomain {:name    "test"
                      :mainurl "http://www.baidu.com"
                      :intro   "百度"}))

(defn test-insert-grapdata []
  (->
    (insert* grapdatas)
    (values {:id "tete"})
    (insert)))



(defentity ingredients
           (database stock-db))

(defn testest []
  (-> (insert* ingredients)
      (values {:name "测试测试"
               :price 5566
               :intro "5566"})
      (insert)))