(ns learn.learndriverweb
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]])
  (:import (org.openqa.selenium.phantomjs PhantomJSDriver)
           (org.openqa.selenium.remote DesiredCapabilities)))

(defn run []
  (System/setProperty "webdriver.chrome.driver" "E:\\devkit\\chromedriver.exe")
  (set-driver! {:browser :firefox} "https://console.upyun.com/")
  (println (page-source))
  (wait-until (fn [] (if (find-element {:css "#username"}) true false)))
  (input-text "#username" "myusername")
  (input-text "#password" "123456")
  (to "http://www.baidu.com")
  (input-text "#kw" "test"))


(defn testrun []

  (System/setProperty "phantomjs.binary.path" "G:/dev_kit/phantomjs-1.9.8-windows/phantomjs.exe")

  (set-driver!
    (init-driver
      {:webdriver
       (PhantomJSDriver. (doto (DesiredCapabilities.)
                           (.setCapability "phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0")
                           (.setCapability "phantomjs.page.customHeaders.Accept-Language" "en-US")
                           (.setCapability "phantomjs.page.customHeaders.Connection" "keep-alive")
                           (.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                    "--webdriver-loglevel=WARN"]))))}))

  (to "http://www.baidu.com")
  (println (page-source))
  )

;(def driver (new-driver {:browser :chrome}))
;(set-driver! driver)

;(to "https://console.upyun.com/#/login")
;(to driver "https://console.upyun.com/#/login")