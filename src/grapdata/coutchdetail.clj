(ns grapdata.coutchdetail
  (:require [com.ashafa.clutch :as clutch]))

(def html-db (clutch/get-database "html-db"))
(def domain-db (clutch/get-database "domain-db"))

;就是想做一个可以传入db选项的东西，但可以返回一个方法的集合。

(declare couchdb-impl-generator)


(defn couchdb-impl-generator [db]
  {:put-doc (fn [db-entity]
              (clutch/put-document db (into {} db-entity)))
   :get-doc (fn [db-entity]
              (clutch/get-document db (into {} db-entity)))
   :del-doc (fn [db-entity]
              (clutch/delete-document db (into {} db-entity)))})

(defprotocol couchdb-operetor
  (put-doc [db-entity])
  (get-doc [db-entity])
  (del-doc [db-entity]))

(defrecord HtmlContent [_id domain-id html create-time])

(defrecord DomainContent [_id ])

(extend HtmlContent
  couchdb-operetor
  (couchdb-impl-generator html-db))

(extend DomainContent
  couchdb-operetor
  (couchdb-impl-generator domain-db))


(defn testcluth []
  (let [htmlc (HtmlContent. 5566 66777 "<html></html>" (System/currentTimeMillis))]
    (put-doc htmlc)))

(defn testte3s []
  (clutch/put-document html-db {:_id 5566 :gg "bar"}))