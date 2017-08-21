(ns learn.newdriver
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]]
            [clojure.core.async :as async]
            [learn.email :as email-util])
  (:import (org.openqa.selenium.phantomjs PhantomJSDriver)
           (org.openqa.selenium.remote DesiredCapabilities)))

(def PHANTOM_PATH "D:\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe")

(defn create-mydriver
  []
  (System/setProperty "phantomjs.binary.path" PHANTOM_PATH)
  (let [driver (init-driver {:webdriver
                             (PhantomJSDriver.
                              (doto (DesiredCapabilities.)
                                (.setCapability "phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0")
                                (.setCapability "phantomjs.page.customHeaders.Accept-Language" "zh-CN")
                                (.setCapability "phantomjs.page.customHeaders.Connection" "keep-alive")
                                (.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                         "--webdriver-loglevel=WARN"
                                                                                         "--proxy=127.0.0.1:55555"]))))})]
    #_(.executePhantomJS (:webdriver driver) (slurp "resources/PhantomJSDriver/withoutcss.js") (into-array []))
    (window-resize driver {:width 1920 :height 1080})
    driver))

(defn- get-pic-code [url]
  (spit "d:\\test.txt" url)
  (str (read)))

(defn register []
  (let [driver (create-mydriver)]
    (try
      (to driver "https://www.facebook.com")
      (input-text driver "input[name=lastname]" "han")
      (input-text driver "input[name=firstname]" "xixi")
      (input-text driver "input[name=reg_email__]" "test@hyesheng.com")
      (input-text driver "input[name=reg_email_confirmation__]" "test@hyesheng.com")
      (input-text driver "input[name=reg_passwd__]" "!3465634rgdG")
      (select-option driver "#year" {:text "1990"})
      (select-option driver "#month" {:value "1"})
      (select-option driver "#day" {:value "1"})
      (select driver "input[type='radio'][value='1']")
      (take-screenshot driver :file "d:\\register.png")
      (click driver "button[name=websubmit]")

      ; 等待加载
      (implicit-wait driver 5000)

      ; 验证码校验
      (when (exists? driver "#recaptcha_image img")
        (let [code (get-pic-code (attribute driver "#recaptcha_image img" "src"))]
          (click driver "input[name=captcha_response]")
          (input-text driver "input[name=captcha_response]" code)
          (click driver "button[type=submit]")))
      (take-screenshot driver :file "d:\\valid-code.png")

      (implicit-wait driver 3000)
      (take-screenshot driver :file "d:\\test.png")
      ; 读取邮件的验证地址
      (let [url (-> "test@hyesheng.com"
                    (email-util/get-email)
                    (email-util/get-facebook-confirm-email-url))]
        (to driver url)
        (take-screenshot driver :file "d:\\confirm-email.png"))

      (finally
        (println "finish")
        (quit driver)))))


(register)
