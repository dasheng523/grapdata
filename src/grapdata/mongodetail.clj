(ns grapdata.mongodetail
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [taoensso.timbre :as timbre]))

(defonce conn (atom nil))

(defn- get-conn []
  (when-not @conn
    (reset! conn (mg/connect)))
  @conn)


(def db (mg/get-db (get-conn) "grapdb"))

(def coll "invail_urls")

(defn insert-invail-url [task-id url]
  (mc/insert db coll {:task_id task-id :url url}))

(defn delete-invail-url [task-id]
  (mc/remove db coll {:task_id task-id}))

(defn insert-htmlcontent [content]
  (mc/insert db "htmlconent" content))


(insert-htmlcontent {:task_id "523" :ccc "5544"})
(println (mc/find-maps db "htmlcontent" {:task_id "523"}))

