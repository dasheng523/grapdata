(ns grapdata.sikedaodi
  (:require [clojure.core.async :as async]
            [taoensso.timbre :as timbre]))


;创建任务执行器
(defn create-task-actuator [task-engine]
  {:need-fetch-urls (async/chan)
   :fail-fetch-urls (atom [])
   :is-continue (atom 1)
   :task-engine task-engine})

;启动任务执行器
(defn start-task-actuator [{:keys [need-fetch-urls fail-fetch-urls is-continue task-engine] :as actuator}]
  (let [add-url-to-chan (fn [url] (async/go (async/>! need-fetch-urls url)))]
    (timbre/info "任务执行器，启动")
    (async/go-loop []
      (let [url (async/<! need-fetch-urls)]
        (try
          (when-let [htmlcontent ((:url-visitor task-engine) url)]
            (doseq [link ((:next-link-extractor task-engine) htmlcontent)]
              (add-url-to-chan link))
            ((:html-handler task-engine) htmlcontent))
          (catch Exception e
            (.printStackTrace e)
            (swap! fail-fetch-urls conj url)
            ((:error-handler task-engine) e))))
      (if @is-continue
        (recur)
        (timbre/info "任务执行器，关闭")))
    actuator))

;停止任务执行器
(defn stop-task-actuator [{:keys [need-fetch-urls is-continue] :as actuator}]
  (if @is-continue
    (do
      (swap! is-continue dec)
      (async/close! need-fetch-urls)
      (timbre/info "操作成功"))
    (timbre/error "操作失败，任务早就停止了！"))
  actuator)

;任务执行器状态持久化
(defn save-task-actuator [{:keys [need-fetch-urls fail-fetch-urls]} task-info]
  (async/go-loop []
    (when-let [link (async/<! need-fetch-urls)]
      (grapdata.mongodetail/insert-needto-url (:task-id task-info) link)
      (recur)))
  (doseq [link @fail-fetch-urls]
    ((grapdata.mongodetail/insert-invail-url (:task-id task-info) link))))

;恢复任务执行器状态
(defn recover-task-actuator [actuator task-info]
  (let [need-fetch-urls
        (async/to-chan
          (grapdata.mongodetail/find-and-remove-needto-urls (:task-id task-info)))
        fail-fetch-urls
        (grapdata.mongodetail/find-and-remove-invail-urls (:task-id task-info))]
    (assoc actuator :need-fetch-urls need-fetch-urls :fail-fetch-urls (atom fail-fetch-urls))))

;发送任务
(defn send-task [{:keys [need-fetch-urls] :as actuator} {:keys [start-link]}]
  (async/go
    (async/>! need-fetch-urls start-link))
  actuator)

