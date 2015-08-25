(ns tools.phantom-graper
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]])
  (:import (org.openqa.selenium.phantomjs PhantomJSDriver)
           (org.openqa.selenium.remote DesiredCapabilities)))


(defonce mydriver (atom nil))

(defn get-driver []
  (when-not @mydriver
    (System/setProperty "phantomjs.binary.path" "E:/devkit/phantomjs-1.9.8-windows/phantomjs.exe")
    (let [phantom-ins (PhantomJSDriver. (doto (DesiredCapabilities.)
                                          (.setCapability "phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0")
                                          (.setCapability "phantomjs.page.customHeaders.Accept-Language" "zh-CN")
                                          (.setCapability "phantomjs.page.customHeaders.Connection" "keep-alive")
                                          (.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                                   "--webdriver-loglevel=WARN"]))))]
      (.executePhantomJS phantom-ins (slurp "resources/PhantomJSDriver/withoutcss.js") (into-array []))
      (reset! mydriver (init-driver {:webdriver phantom-ins}))))
  @mydriver)

