(ns grapdata.toutiao.driver
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]]
            [clojure.java.io :as io])
  (:import (org.openqa.selenium.remote DesiredCapabilities)
           (org.openqa.selenium.chrome ChromeDriver)
           (org.openqa.selenium Proxy)))

(defn create-chrome-proxy-driver []
  (System/setProperty "webdriver.chrome.driver" (.getPath (io/resource "driver/chromedriver.exe")))
  (init-driver {:webdriver
                (ChromeDriver.
                  (doto (DesiredCapabilities/chrome)
                    (.setCapability "proxy" (doto (Proxy.)
                                              (.setHttpProxy "http://183.135.249.198:35150")))))}))

(defn create-chrome-driver []
  (System/setProperty "webdriver.chrome.driver" (.getPath (io/resource "driver/chromedriver.exe")))
  (let [driver (new-driver {:browser :chrome})]
    (window-resize driver {:width 1920 :height 1080})
    driver))
