(ns grapproject.model
  (:use [korma.db]
        [korma.core])
  (:gen-class))

(defdb bigdata-db (postgres {:db "bidata"
                             :user "postgres"
                             :password "postgres"
                             :host "localhost"}))

(defentity grapdomains
           (database bigdata-db))

(defentity grapdata
           (database bigdata-db))

