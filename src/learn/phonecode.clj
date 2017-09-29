;; 这是获取手机验证码的脚本
(ns learn.phonecode
  (:require [clj-http.client :as http]
            [clojure.string :as str]))

(defn- get-value [s]
  (let [[code token] (str/split s #"\|")]
    (if (= code "1")
      token
      (throw (Exception. token)))))

(defn- call-service [url]
  (let [{:keys [status body]}
        (http/get url)]
    (if (= 200 status)
      (get-value body)
      (throw (Exception. "网络错误")))))

(defn login
  [username password]
  (call-service (str "http://api.eobzz.com/api/do.php?action=loginIn&name="
                     username
                     "&password="
                     password)))

(defn get-phone
  [token pid]
  (call-service (str "http://api.eobzz.com/api/do.php?action=getPhone&sid=" pid
                     "&token=" token)))

(defn get-code
  [phone token pid]
  (call-service (str "http://api.eobzz.com/api/do.php?action=getMessage&sid=" pid
                     "&phone=" phone
                     "&token=" token)))

(defn test-phonecode []
  (login "dasheng523" "a5235013")
  (get-phone "d67e492ea08d6f72d15bf2487efc5db2" 9729)
  (get-code "13690754024" "d67e492ea08d6f72d15bf2487efc5db2" 9729))
