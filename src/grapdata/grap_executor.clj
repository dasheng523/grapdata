(ns grapdata.grap-executor
  (:require [clojure.core.async :as async]
            [taoensso.timbre :as timbre]
            [datawarehouse.mongodetail]))


;��������ִ����
(defn create-task-actuator [task-engine]
  {:need-fetch-urls (async/chan)
   :fail-fetch-urls (atom [])
   :is-continue (atom true)
   :task-engine task-engine})

;��������ִ����
(defn start-task-actuator [{:keys [need-fetch-urls fail-fetch-urls is-continue task-engine] :as actuator}]
  (let [add-url-to-chan (fn [url] (async/go (async/>! need-fetch-urls url)))]
    (when-not @is-continue
      (reset! is-continue true))
    (timbre/info "����ִ����������")
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
        (timbre/info "����ִ�������ر�")))
    actuator))

;ֹͣ����ִ����
(defn stop-task-actuator [{:keys [need-fetch-urls is-continue] :as actuator}]
  (if @is-continue
    (do
      (reset! is-continue false)
      (async/close! need-fetch-urls)
      (timbre/info "�����ɹ�"))
    (timbre/error "����ʧ�ܣ��������ֹͣ�ˣ�"))
  actuator)

;����ִ����״̬�־û�
(defn save-task-actuator [{:keys [need-fetch-urls fail-fetch-urls]} task-id]
  (async/go-loop []
    (when-let [link (async/<! need-fetch-urls)]
      (datawarehouse.mongodetail/insert-needto-url task-id link)
      (recur)))
  (doseq [link @fail-fetch-urls]
    ((datawarehouse.mongodetail/insert-invail-url task-id link))))

;�ָ�����ִ����״̬
(defn recover-task-actuator [actuator task-id]
  (let [need-fetch-urls
        (async/to-chan
          (datawarehouse.mongodetail/find-and-remove-needto-urls task-id))
        fail-fetch-urls
        (datawarehouse.mongodetail/find-and-remove-invail-urls task-id)]
    (assoc actuator :need-fetch-urls need-fetch-urls :fail-fetch-urls (atom fail-fetch-urls))))

;��������
(defn send-task [{:keys [need-fetch-urls] :as actuator} start-link]
  (async/go
    (async/>! need-fetch-urls start-link))
  actuator)

