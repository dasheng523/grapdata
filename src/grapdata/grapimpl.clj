(ns grapdata.grapimpl
  (:require [clojure.core.async :as async :only [<! timeout]]
            [clj-http.client :as http :only [get]]
            [net.cgrand.enlive-html :as enlive :only [select]])
  (:import (java.io StringReader)))

;速度
(def slepp-time (atom 1000))

;访问网页
(defn fetch-url [url]
  (async/<! (async/timeout @slepp-time))
  (http/get url))

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

;查找网页中的一些连接
(defn parse-link [html]
  (when (= "list" (page-type html))
    (let [htmlnodes (change-nodes html)]
      (->
        (enlive/select htmlnodes #{[:dt.pro_list_pic :> :a] [:a.page_next]})
        ((fn [info] (println info) info))
        ((fn [linknodes] (reduce #(conj %1 (str "http://www.15fen.com/" (-> %2 :attrs :href))) #{} linknodes)))))))

(defn handle-html [html]
  (when (= "detail" (page-type html))
    (let [htmlnodes (change-nodes html)]
      )))

(defn- full-task []
  )


(defrecord GrapTask [task-id start-url])

(defprotocol Grapable
  ;开始抓取任务
  (start-grap [grap-task])
  ;暂停抓取任务
  (stop-grap [grap-task])
  ;结束抓取任务
  (end-grap [grap-task])
  ;重新开始抓取任务
  (restart-grap [grap-task]))

(extend GrapTask
  Grapable
  )