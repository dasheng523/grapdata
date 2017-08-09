(ns learn.newdriver
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]]
            [clojure.core.async :as async]
            )
  (:import (org.openqa.selenium.phantomjs PhantomJSDriver)
           (org.openqa.selenium.remote DesiredCapabilities)))

(def PHANTOM_PATH "/usr/local/phantomjs-2.1.1-linux-x86_64/bin/phantomjs")


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
                                                                                         "--webdriver-loglevel=WARN"]))))})]
    #_(.executePhantomJS (:webdriver driver) (slurp "resources/PhantomJSDriver/withoutcss.js") (into-array []))
    (window-resize driver {:width 1920 :height 1080})
    driver))



(defn register []
  (let [driver (create-mydriver)]
    (to driver "https://www.facebook.com")
    (input-text driver "input[name=lastname]" "han")
    (input-text driver "input[name=firstname]" "xixi")
    (input-text driver "input[name=reg_email__]" "test@hyesheng.com")
    (input-text driver "#u_0_a" "testest@hyesheng.com")
    (input-text driver "input[name=reg_passwd__]" "!3465634rgdG")
    (select-option driver "#year" {:text "1990"})
    (select-option driver "#month" {:value "1"})
    (select-option driver "#day" {:value "1"})
    (select driver "#u_0_h")
    (click driver "#u_0_m")
    (async/<!! (async/timeout 3000))
    (take-screenshot driver :file "/home/yesheng/www/1.png")
    (close driver)
    (quit driver)))

(register)
