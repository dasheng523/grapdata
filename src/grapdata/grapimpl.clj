(ns grapdata.grapimpl
  (:require [clojure.core.async :as async]
            [clj-http.client :as http :only [get]]
            [net.cgrand.enlive-html :as enlive :only [select]]
            [grapdata.mongodetail :as mongo]
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
       (mongo/insert-invail-url task-id url)
       (timbre/error "error in url:" url))
     :html-handler                                          ;页面处理器
     (fn [html]
       (when (= "detail" (page-type html))
         (mongo/insert-htmlcontent (assoc html :task_id task-id))))
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


(def iscontinue (atom 1))

(defn graper-generator [{:keys [url-visitor next-link-extractor html-handler error-handler]}]
  ;TODO 这个数字需要配置形式拿出来
  (let [need-to-fetch (async/chan (async/buffer 1000))
        add-url-to-chan (fn [url] (async/go (async/>! need-to-fetch url)))]
    (fn [start-url]
      (add-url-to-chan start-url)
      (async/go-loop []
        (let [url (async/<! need-to-fetch)]
          (try
            (when-let [htmlcontent (url-visitor url)]
              (doseq [link (next-link-extractor htmlcontent)]
                (add-url-to-chan link))
              (html-handler htmlcontent))
            (catch Exception e
              (.printStackTrace e)
              (error-handler url))))
        (when @iscontinue
          (recur))))))

(defn stop-task []
  (reset! iscontinue nil))

(defn start-task []
  (let [task {:task-id "4433" :taskname "7776644"}
        taskruner (graper-generator (engine-generator task))]
    (taskruner "http://www.15fen.com/category.php?id=1")))


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



(def engine (engine-generator {:task-id "789"}))
(def task-server (grapdata.sikedaodi/create-task-actuator engine))
(def addtask (grapdata.sikedaodi/send-task task-server {:start-link "http://www.15fen.com/category.php?id=1"}))
(grapdata.sikedaodi/start-task-actuator addtask)
