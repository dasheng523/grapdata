(ns grapdata.grapimpl
  (:require [clojure.core.async :as async]
            [clj-http.client :as http :only [get]]
            [net.cgrand.enlive-html :as enlive :only [select]]
            [taoensso.timbre :as timbre])
  (:import (java.io StringReader)))

;速度
(def slepp-time (atom 1000))

;判断是列表页还是详情页
(defn- page-type [html]
  (let [redir (first (:trace-redirects html))]
    (cond
      (.contains redir "www.15fen.com/category.php")
      "list"
      (.contains redir "www.15fen.com/goods.php")
      "detail"
      :else (throw (Exception. "异常内容")))))

;转化nodes
(defn- change-nodes [html]
  (->
    html
    :body
    (StringReader.)
    (enlive/html-resource)))

(defn engine-generator [task-intro]
  (let [task-id (:task-id task-intro)]
    {:error-handler                                         ;错误处理器
     (fn [url]
       (datawarehouse.mongodetail/insert-invail-url task-id url)
       (timbre/error "error in url:" url))
     :html-handler                                          ;页面处理器
     (fn [html]
       (when (= "detail" (page-type html))
         (datawarehouse.mongodetail/insert-htmlcontent (assoc html :task_id task-id))))
     :next-link-extractor                                   ;下一页解析器
     (fn [html]
       (when (= "list" (page-type html))
         (let [htmlnodes (change-nodes html)]
           (->
             (enlive/select htmlnodes #{[:dt.pro_list_pic :> :a] [:a.page_next]})
             ((fn [info] (println info) info))
             ((fn [linknodes]
                (reduce #(conj %1
                               (str
                                 "http://www.15fen.com/"
                                 (-> %2 :attrs :href))) #{} linknodes)))))))
     :url-visitor                                           ;url访问器
     (fn fetch-url [url]
       (async/go (async/<! (async/timeout @slepp-time)))
       (timbre/info "visiting url:" url)
       (http/get url))}))



(def engine (engine-generator {:task-id "789"}))
(def task-server (grapdata.sikedaodi/create-task-actuator engine))
(def addtask (grapdata.sikedaodi/send-task task-server {:start-link "http://www.15fen.com/category.php?id=1"}))
(grapdata.sikedaodi/start-task-actuator addtask)
