(ns grapdata.grapimpl
  (:require [clojure.core.async :as async :only [<! timeout]]
            [clj-http.client :as http :only [get]]
            [net.cgrand.enlive-html :as enlive :only [select]])
  (:import (java.io StringReader)))

;�ٶ�
(def slepp-time (atom 1000))

;������ҳ
(defn fetch-url [url]
  (async/<! (async/timeout @slepp-time))
  (http/get url))

;�ж����б�ҳ��������ҳ
(defn- page-type [html]
  (let [redir (first (:trace-redirects html))]
    (cond
      (.contains redir "www.15fen.com/category.php")
      "list"
      (.contains redir "www.15fen.com/goods.php")
      "detail"
      :else (throw (Exception. "�쳣����")))))

;ת��nodes
(defn- change-nodes [html]
  (->
    html
    :body
    (StringReader.)
    (enlive/html-resource)))

;������ҳ�е�һЩ����
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
  ;��ʼץȡ����
  (start-grap [grap-task])
  ;��ͣץȡ����
  (stop-grap [grap-task])
  ;����ץȡ����
  (end-grap [grap-task])
  ;���¿�ʼץȡ����
  (restart-grap [grap-task]))

(extend GrapTask
  Grapable
  )