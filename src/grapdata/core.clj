(ns grapdata.core
  (:require [clojure.core.async :as async :only [<! >!]]
            [grapdata.common :as common]
            [grapdata.grapimpl :as impl])
  (:gen-class))
;[clojure.core.async :as async :refer [<! >! <!! >!! buffer  go-loop close! alts! timeout chan alt! go]]

;TODO 解决log4j的警告
;TODO 以后再用一个像样的LOG吧
;TODO 失败后需要能够恢复环境，要是发生了跳转，那就麻烦了（可以查看trace-redirects就好了）。
;TODO 在处理html的时候，也需要知道格式对不对，那种格式不对的地址和内容也是需要保持的。
;TODO 好像需要封装一个协议才能玩得起。

;chan的长度
(def buffer-len 1000)

(deftype GrapConfig [channel task-id])

(defn graper-generator [fn-fetch-url fn-next-link-from fn-handle-html fn-handle-error]
  ;TODO 这个数字需要配置形式拿出来
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

(defn graper (graper-generator
               impl/fetch-url
               impl/parse-link
               (impl/handle-html-generator "523")
               (impl/handle-error-generator "523")))

(def error-log (common/write-file "error.log"))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

