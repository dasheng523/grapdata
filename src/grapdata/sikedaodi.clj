(ns grapdata.sikedaodi
  (:require [clojure.core.async :as async]
            [taoensso.timbre :as timbre]))


;��������ִ����
(defn create-task-actuator [task-engine]
  {:need-fetch-urls (async/chan)
   :fail-fetch-urls (atom [])
   :is-continue (atom 1)
   :task-engine task-engine})

;��������ִ����
(defn start-task-actuator [{:keys [need-fetch-urls fail-fetch-urls is-continue task-engine] :as actuator}]
  (let [add-url-to-chan (fn [url] (async/go (async/>! need-fetch-urls url)))]
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
      (swap! is-continue dec)
      (async/close! need-fetch-urls)
      (timbre/info "�����ɹ�"))
    (timbre/error "����ʧ�ܣ��������ֹͣ�ˣ�"))
  actuator)

;����ִ����״̬�־û�
(defn save-task-actuator [{:keys [need-fetch-urls fail-fetch-urls]} task-info]
  (async/go-loop []
    (when-let [link (async/<! need-fetch-urls)]
      (grapdata.mongodetail/insert-needto-url (:task-id task-info) link)
      (recur)))
  (doseq [link @fail-fetch-urls]
    ((grapdata.mongodetail/insert-invail-url (:task-id task-info) link))))

;�ָ�����ִ����״̬
(defn recover-task-actuator [actuator task-info]
  (let [need-fetch-urls
        (async/to-chan
          (grapdata.mongodetail/find-and-remove-needto-urls (:task-id task-info)))
        fail-fetch-urls
        (grapdata.mongodetail/find-and-remove-invail-urls (:task-id task-info))]
    (assoc actuator :need-fetch-urls need-fetch-urls :fail-fetch-urls (atom fail-fetch-urls))))

;��������
(defn send-task [{:keys [need-fetch-urls] :as actuator} {:keys [start-link]}]
  (async/go
    (async/>! need-fetch-urls start-link))
  actuator)

