(ns grapdata.enginegenerator
  (:require [clojure.core.async :as async]
            [clj-http.client :as http :only [get]]
            [net.cgrand.enlive-html :as enlive :only [select]]
            [taoensso.timbre :as timbre]
            [datawarehouse.mongodetail :as mongodetail])
  (:import (java.io StringReader)))

;速度
(def slepp-time (atom 1000))

;转化nodes
(defn- change-nodes [html]
  (->
    html
    :body
    (StringReader.)
    (enlive/html-resource)))

;转换URL真实地址
(defn- change-true-address [url currenturl]
  (cond
    (.startsWith url "/")
    (str (.substring currenturl 0 (.indexOf currenturl "/" 7)) url)
    (.startsWith url "http://")
    url
    :else
    (let [handurl (if (.endsWith currenturl "/")
                    (.substring currenturl 0 (- (.length currenturl) 1))
                    currenturl)]
      (str (.substring handurl 0 (.lastIndexOf handurl "/")) "/" url))))


;生成具体的抓取引擎
(defn engine-generator [fn-page-type next-nodes task-intro]
  (let [task-id (:task-id task-intro)]
    {:error-handler                                         ;错误处理器
     (fn [url]
       (mongodetail/insert-invail-url task-id url)
       (timbre/error "error in url:" url))
     :html-handler                                          ;页面处理器
     (fn [html]
       (when (= "detail" (fn-page-type html))
         (mongodetail/insert-htmlcontent (assoc html :task_id task-id))))
     :next-link-extractor                                   ;下一页解析器
     (fn [html]
       (when (= "list" (fn-page-type html))
         (let [htmlnodes (change-nodes html)]
           (->
             (enlive/select htmlnodes next-nodes)
             ((fn [info] (println info) info))
             ((fn [linknodes]
                (reduce #(conj %1
                               (change-true-address
                                 (-> %2 :attrs :href)
                                 (first (:trace-redirects html))))
                        #{} linknodes)))))))
     :url-visitor                                           ;url访问器
     (fn fetch-url [url]
       (async/go (async/<! (async/timeout @slepp-time)))
       (timbre/info "visiting url:" url)
       (http/get url))}))


