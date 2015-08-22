(ns grapdata.grapimpl
  (:require [clojure.core.async :as async]
            [clj-http.client :as http :only [get]]
            [net.cgrand.enlive-html :as enlive :only [select]]
            [grapdata.mongodetail :as mongo]
            [taoensso.timbre :as timbre])
  (:import (java.io StringReader)))

;�ٶ�
(def slepp-time (atom 1000))

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

(defn engine-generator [task-intro]
  (let [task-id (:task-id task-intro)]
    {:error-handler                                         ;��������
     (fn [url]
       (mongo/insert-invail-url task-id url)
       (timbre/error "error in url:" url))
     :html-handler                                          ;ҳ�洦����
     (fn [html]
       (when (= "detail" (page-type html))
         (mongo/insert-htmlcontent (assoc html :task_id task-id))))
     :next-link-extractor                                   ;��һҳ������
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
     :url-visitor                                           ;url������
     (fn fetch-url [url]
       (async/go (async/<! (async/timeout @slepp-time)))
       (timbre/info "visiting url:" url)
       (http/get url))}))


(def iscontinue (atom 1))

(defn graper-generator [{:keys [url-visitor next-link-extractor html-handler error-handler]}]
  ;TODO ���������Ҫ������ʽ�ó���
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
  ;��ʼץȡ����
  (start-grap [grap-task])
  ;��ͣץȡ����
  (stop-grap [grap-task])
  ;����ץȡ����
  (end-grap [grap-task])
  ;���¿�ʼץȡ����
  (restart-grap [grap-task]))



(def engine (engine-generator {:task-id "789"}))
(def task-server (grapdata.sikedaodi/create-task-actuator engine))
(def addtask (grapdata.sikedaodi/send-task task-server {:start-link "http://www.15fen.com/category.php?id=1"}))
(grapdata.sikedaodi/start-task-actuator addtask)
