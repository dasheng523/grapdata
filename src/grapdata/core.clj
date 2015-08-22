(ns grapdata.core
  (:require [clojure.core.async :as async :only [<! >!]])
  (:gen-class))

;TODO ���log4j�ľ���
;TODO �Ժ�����һ��������LOG��
;TODO ʧ�ܺ���Ҫ�ܹ��ָ�������Ҫ�Ƿ�������ת���Ǿ��鷳�ˣ����Բ鿴trace-redirects�ͺ��ˣ���
;TODO �ڴ���html��ʱ��Ҳ��Ҫ֪����ʽ�Բ��ԣ����ָ�ʽ���Եĵ�ַ������Ҳ����Ҫ���ֵġ�
;TODO ������Ҫ��װһ��Э����������

;chan�ĳ���
(def buffer-len 1000)

(deftype GrapConfig [channel task-id])

(defn graper-generator [fn-fetch-url fn-next-link-from fn-handle-html fn-handle-error]
  ;TODO ���������Ҫ������ʽ�ó���
  (let [need-to-fetch (async/chan (async/buffer buffer-len))
        add-url-to-chan (fn [url] (async/go (async/>! need-to-fetch url)))]
    (fn [start-url]
      (add-url-to-chan start-url)
      (async/go-loop []
        (let [url (async/<! need-to-fetch)]
          (try
            (when-let [htmlcontent (fn-fetch-url url)]
              (doseq [link (fn-next-link-from htmlcontent)]
                (add-url-to-chan link))
              (fn-handle-html htmlcontent))
            (catch Exception e
              (.printStackTrace e)
              (fn-handle-error url))))
        (recur)))))


(defrecord GrapEngine [url-visitor next-link-extractor html-handler error-handler])
(defprotocol Graper
  (task-execution [grap-engine]))

(extend-protocol Graper
  GrapEngine
  (task-execution [grap-engine]
    ;TODO ���������Ҫ������ʽ�ó���
    (let [need-to-fetch (async/chan (async/buffer buffer-len))
          add-url-to-chan (fn [url] (async/go (async/>! need-to-fetch url)))]
      (fn [start-url]
        (add-url-to-chan start-url)
        (async/go-loop []
          (let [url (async/<! need-to-fetch)]
            (try
              (when-let [htmlcontent ((:url-fetcher grap-engine) url)]
                (doseq [link ((:next-link-extractor grap-engine) htmlcontent)]
                  (add-url-to-chan link))
                ((:html-handler grap-engine) htmlcontent))
              (catch Exception e
                (.printStackTrace e)
                ((:error-handler grap-engine) url))))
          (recur))))))




(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

