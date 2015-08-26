(ns tools.phantom-graper
  (:require [clj-webdriver.taxi :refer :all]
            [clj-webdriver.driver :refer [init-driver]]
            [clojure.core.async :refer [chan go >! <! <!! >!! close! go-loop]])
  (:import (org.openqa.selenium.phantomjs PhantomJSDriver)
           (org.openqa.selenium.remote DesiredCapabilities)))


(defonce driver-list (chan 10))


(defn- init-single-driver []
  (let [phantom-ins (PhantomJSDriver. (doto (DesiredCapabilities.)
                                        (.setCapability "phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0")
                                        (.setCapability "phantomjs.page.customHeaders.Accept-Language" "zh-CN")
                                        (.setCapability "phantomjs.page.customHeaders.Connection" "keep-alive")
                                        (.setCapability "phantomjs.cli.args" (into-array String ["--ignore-ssl-errors=true"
                                                                                                 "--webdriver-loglevel=WARN"]))))]
    (.executePhantomJS phantom-ins (slurp "resources/PhantomJSDriver/withoutcss.js") (into-array []))
    (init-driver {:webdriver phantom-ins})))


(defn- init-driver-list [n]
  (System/setProperty "phantomjs.binary.path" "e:/devkit/phantomjs-1.9.8-windows/phantomjs.exe")
  (for [_ (range n)]
    (go (>! driver-list (init-single-driver)))))

(defn close-all-driver []
  (close! driver-list)
  (go-loop []
    (when-let [driver (<! driver-list)]
      (close driver)
      (recur))))


(defn get-page-content [url]
  (let [driver (<!! driver-list)]
    (to driver url)
    (let [page-content (page-source driver)]
      (>!! driver-list driver)
      page-content)))

