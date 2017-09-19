(ns learn.newdriver
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]]
            [clj-webdriver.firefox :as ff]
            [clojure.core.async :as async]
            [learn.email :as email-util]
            [clojure.string :as str])
  (:import (org.openqa.selenium.phantomjs PhantomJSDriver)
           (org.openqa.selenium.firefox FirefoxDriver)
           (org.openqa.selenium.firefox FirefoxProfile)
           (org.openqa.selenium.chrome ChromeDriver)
           (org.openqa.selenium.remote DesiredCapabilities)
           (org.openqa.selenium Proxy)))

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
                                #_(.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                         "--webdriver-loglevel=WARN"
                                                                                         "--proxy=127.0.0.1:55555"]))))})]
    #_(.executePhantomJS (:webdriver driver) (slurp "resources/PhantomJSDriver/withoutcss.js") (into-array []))
    (window-resize driver {:width 1920 :height 1080})
    driver))

(defn create-firefoxDriver
  []
  (let [driver (init-driver {:webdriver
                             (FirefoxDriver.
                               (doto (FirefoxProfile.)
                                 (.setPreference "network.proxy.http", "183.135.249.198")
                                 (.setPreference "network.proxy.http_port" "35150")))})]
    driver))

(defn create-firefoxDriver2 []
  (new-driver {:browser :firefox
               :profile (doto (ff/new-profile)
                          (ff/set-preferences {:network.proxy.http "183.135.249.198"
                                               :network.proxy.http_port 35150
                                               :network.proxy.type 1}))}))


(System/setProperty "webdriver.chrome.driver" "d:\\chromedriver.exe")
#_(def cdriver (init-driver {:webdriver
                           (ChromeDriver.
                             (doto (DesiredCapabilities/chrome)
                               (.setCapability "proxy" (doto (Proxy.)
                                                         (.setHttpProxy "http://183.135.249.198:35150")))))}))
#_(to cdriver "https://www.baidu.com")

(defn- get-pic-code [url]
  (spit "d:\\test.txt" url)
  (str (read)))

(defn register [user]
  (let [driver (create-mydriver)
        bithday-path (str/split (:birthday user) #"-")]
    (try
      (delete-all-cookies driver)
      (to driver "https://www.facebook.com")
      (input-text driver "input[name=lastname]" (:lastname user))
      (input-text driver "input[name=firstname]" (:firstname user))
      (input-text driver "input[name=reg_email__]" (:email user))
      (input-text driver "input[name=reg_email_confirmation__]" (:email user))
      (input-text driver "input[name=reg_passwd__]" (:password user))
      (select-option driver "#year" {:text (first bithday-path)})
      (select-option driver "#month" {:value (str (Integer. (.toString (get bithday-path 1))))})
      (select-option driver "#day" {:value (str (Integer. (.toString (get bithday-path 2))))})
      (select driver (str "input[type='radio'][value='" (:sex user) "']"))
      (take-screenshot driver :file "d:\\register.png")
      (click driver "button[name=websubmit]")

      ; 等待加载
      (implicit-wait driver 5000)

      ; 验证码校验
      (when (exists? driver "#recaptcha_image img")
        (let [code (get-pic-code (attribute driver "#recaptcha_image img" "src"))]
          (click driver "input[name=captcha_response]")
          (input-text driver "input[name=captcha_response]" code)
          (click driver "button[type=submit]")
          (take-screenshot driver :file "d:\\valid-code.png")
          (implicit-wait driver 3000)))

      (take-screenshot driver :file "d:\\commit-form.png")

      ; 读取邮件的验证地址
      (let [url (-> (email-util/get-email (:email user) (:password user))
                    (email-util/get-facebook-confirm-email-url))]
        (to driver url)
        (take-screenshot driver :file "d:\\confirm-email.png"))

      (finally
        (println "finish")
        (quit driver)))))

(defn- upload-pic
  "有时候fackbook会要求上传面部图片"
  [driver user]
  (when (exists? driver ".mam input[name=upload_meta]")
    (take-screenshot driver :file "d:\\before.png")
    (send-keys driver ".mam input[name=upload_meta]" "d:\\mei.jpg")
    (wait-until #(exists? "img.uiLoadingIndicatorAsync") 10000)
    (click driver "#checkpointSubmitButton")
    (implicit-wait driver 3000)
    (click driver "#checkpointSubmitButton")
    (take-screenshot driver :file "d:\\uploadpic2.png")))

(defn login [driver username password]
  (to driver "https://www.facebook.com")
  ())
