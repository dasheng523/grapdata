(ns grapdata.mongodetail
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]))

(def conn (mg/connect))
(def db (mg/get-db conn "grapdb"))
(def coll "invail_urls")

(defn insert-invail-url [task-id url]
  (mc/insert db coll {:task_id task-id :url url}))

(defn delete-invail-url [task-id]
  (mc/remove db coll {:task_id task-id}))