(ns datawarehouse.mongodetail
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [taoensso.timbre :as timbre])
  (:import org.bson.types.ObjectId))

(defonce conn (atom nil))

(defn- get-conn []
  (when-not @conn
    (reset! conn (mg/connect)))
  @conn)

(defn close-conn []
  (when-not @conn
    (mg/disconnect @conn)
    (reset! conn nil)))

(def db (mg/get-db (get-conn) "grapdb"))

(defn insert-data [table data]
  (let [id (ObjectId.)]
    (mc/insert db table (assoc data :_id id))
    id))

(defn insert-invail-url [task-id url]
  (insert-data "invail_urls" {:task_id task-id :url url}))

(defn insert-needto-url [task-id url]
  (insert-data "needto_urls" {:task_id task-id :url url}))

(defn delete-invail-url [task-id]
  (mc/remove db "invail_urls" {:task_id task-id}))

(defn insert-htmlcontent [content]
  (insert-data "htmlcontent" content))

(defn insert-task [task]
  (insert-data "task" task))

(defn find-and-remove-needto-urls [task-id]
 (let [urls (mc/find-maps db "needto_urls" {:task_id task-id})]
   (mc/remove db "neeto_urls" {:task_id task-id})
   urls))

(defn find-and-remove-invail-urls [task-id]
  (let [urls (mc/find-maps db "invail_urls" {:task_id task-id})]
    (mc/remove db "invail_urls" {:task_id task-id})
    urls))