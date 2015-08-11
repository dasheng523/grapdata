(ns learn.clutch
  (:require [com.ashafa.clutch :as clutch]))


(def html-db (clutch/get-database "html-db"))
(def domain-db (clutch/get-database "domain-db"))

;就是想做一个可以传入db选项的东西，但可以返回一个方法的集合。



(defn couchdb-impl-generator [db]
  {:put-doc (fn [db-entity]
              (clutch/put-document db (into {} db-entity)))
   :get-doc (fn [db-entity]
              (clutch/get-document db (:_id db-entity)))
   :del-doc (fn [db-entity]
              (clutch/delete-document db db-entity))
   :update-doc (fn [db-entity & [mod & args]]
                 (clutch/update-document db db-entity mod args))})

(defprotocol couchdb-operetor
  (put-doc [db-entity])
  (get-doc [db-entity])
  (del-doc [db-entity])
  (update-doc [db-entity & [mod & args]]))

(defrecord HtmlContent [_id domain-id html create-time])

(defrecord DomainContent [_id ])

(extend HtmlContent
  couchdb-operetor
  (couchdb-impl-generator html-db))

(extend DomainContent
  couchdb-operetor
  (couchdb-impl-generator domain-db))


(defn testcluth []
  (let [htmlc (HtmlContent. "5566" "66777werwer" "<html></html>" (System/currentTimeMillis))]
    (del-doc (get-doc htmlc))))

