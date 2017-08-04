(ns learn.newdriver
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]])
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
    driver))


(def driver (create-mydriver))

(window-resize driver {:width 1920 :height 1080})

(to driver "https://www.baidu.com")


(input-text driver "#kw" "web")


(click driver (find-element driver {:css ".s_btn"}))

(map (fn [n] (text n)) (find-elements driver {:css "h3.c-gap-bottom-small a"}))

(execute-script driver "scroll(0, 250);")


(take-screenshot driver :file "/home/yesheng/www/1.png")

(close driver)


(defn run-driver []
  (let [driver (create-mydriver)]
    (to driver "http://www.facebook.com")
    (quick-fill driver
                {{:name "email"}, "ming"})
    (take-screenshot driver :file "/home/yesheng/www/1.png")
    (close driver)))


