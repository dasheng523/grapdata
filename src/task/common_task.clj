(ns task.common_task
  (:require [grapdata.grap-engine :as engine]
            [taoensso.timbre :as timbre]
            [net.cgrand.enlive-html :as enlive :only [select]]
            [grapdata.grap-executor]
            [datawarehouse.dbsqldetail :as sql])
  (:use [clojure.string :only [trim join]]))

(defn defalt-task []
  {:task-id "5566"
   :start-link "http://www.15fen.com/category.php?id=1"})

(defn- page-type [html]
  (let [redir (first (:trace-redirects html))]
    (cond
      (.contains redir "www.15fen.com/category.php")
      "list"
      (.contains redir "www.15fen.com/goods.php")
      "detail"
      :else (throw (Exception. "Òì³£ÄÚÈÝ")))))

(defn- get-html-text
  ([html pathset sp]
   (join sp (map trim (enlive/select html [pathset enlive/text-node]))))
  ([html pathset]
   (get-html-text html pathset ",")))

(defn- get-html-attr
  [html selectset attr]
  (map (fn [data] (-> data :attrs attr)) (enlive/select html selectset)))

(defn- handlehtml [html]
  (sql/insert-vegetable-data {:price (Float/parseFloat (.substring (get-html-text html #{:b.bt_infos_price}) 1))
                              :name (get-html-text html #{:h1.bt_title})
                              :place (get-html-text html #{:ul.bt_price_list})
                              :intro (get-html-text html #{:div.bt_onsale :> :p})}
                             (get-html-attr html
                                            #{[:div.bt_infos_l :> :img] [:div.bt_layout :> :p :> :img]}
                                            :src)))



(defn defalt-engine []
  (engine/engine-generator
    page-type
    #{[:dt.pro_list_pic :> :a] [:a.page_next]}
    (:task-id (defalt-task))
    handlehtml))


(def engine (defalt-engine))
(def task-executor (grapdata.grap-executor/send-task
                   (grapdata.grap-executor/create-task-actuator engine)
                   "http://www.15fen.com/category.php?id=1"))

(defn run []
  (grapdata.grap-executor/start-task-actuator task-executor))

(defn stop []
  (grapdata.grap-executor/stop-task-actuator task-executor))


