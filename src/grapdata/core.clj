(ns grapdata.core
  (:require [clojure.core.async :as async]
            [clj-http.client :as client]
            [net.cgrand.enlive-html :as enlive]
            [grapdata.common :as common])
  (:gen-class)
  (:import (java.io StringReader)))
;[clojure.core.async :as async :refer [<! >! <!! >!! buffer  go-loop close! alts! timeout chan alt! go]]

;TODO 解决log4j的警告
;TODO 以后再用一个像样的LOG吧
;TODO 失败后需要能够恢复环境，要是发生了跳转，那就麻烦了（可以查看trace-redirects就好了）。
;TODO 在处理html的时候，也需要知道格式对不对，那种格式不对的地址和内容也是需要保持的。

(def error-log (common/write-file "error.log"))

;TODO 这个数字需要配置形式拿出来


(defn graper-generator [fn-fetch-url fn-next-link-from fn-handle-html]
  (let [need-to-fetch (async/chan (async/buffer 1000))
        add-url-to-chan (fn [url] (async/go (async/>! need-to-fetch url)))]
    (fn [start-url]
      (add-url-to-chan start-url)
      (async/go-loop []
        (let [url (async/<! need-to-fetch)]
          (try
            (when-let [htmlcontent (fn-fetch-url url)]
              (doseq [link (fn-next-link-from htmlcontent)]
                (add-url-to-chan link))
              (fn-handle-html htmlcontent url))
            (catch Exception e
              ;TODO 要处理这里的异常
              )))
        (recur)))))




(defn get-url-by-clojure [url]
  (let [resp (client/get url)]
    (if (= (:status resp) 200)
      resp
      (do
        (error-log (str "\n error in this url:" url "\n"
                        (pr-str resp)))
        nil))))

(defn post-url-by-clojure [url form-data]
  (let [resp (client/post url {:form-params form-data})]
    (if (= (:status resp) 200)
      resp
      (do
        (error-log (str "\n error in this url:" url "\n"
                        (pr-str resp)))
        nil))))


;解析html内容
(defn parse-link [html]
  (->
    html
    :body
    (StringReader.)
    enlive/html-resource
    (enlive/select [:dt.pro_list_pic :> :a])
    ((fn [link-node] (map #(str "http://www.15fen.com/" (-> % :attrs :href)) link-node)))))

(defn handle-html [html _]
  (->
    html
    :body
    (StringReader.)
    enlive/html-resource
    (enlive/select [:head :title])
    println))

(defn handle-domain [domain]
  (println "save domain"))





(def grapfn (graper-generator get-url-by-clojure parse-link handle-html))

(defn testtest []
  (grapfn "http://www.15fen.com/category.php?id=1"))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))



#_(defn tttt []
  (def test-chan (async/chan))
  (async/go (async/>! test-chan 1))
  (def datas [1 2 3 4])
  (async/go (map #(async/>! test-chan %) datas)))
