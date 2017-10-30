(ns grapdata.toutiao.driver
  (:require [clj-webdriver.taxi :refer :all]
            [cheshire.core :as json]
            [grapdata.toutiao.driver :as tdriver]
            [grapdata.utils :as utils])
  (:import (java.text SimpleDateFormat)))

(def driver (tdriver/create-chrome-driver))
(def sleep-time 1000)


(defn recover-cookies []
  (let [my-cookies (-> (slurp "d:/cookies.json") (json/parse-string true))]
    (doseq [my my-cookies]
      (add-cookie
        driver
        (if (:expiry my)
          (update my :expiry #(.parse (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") %))
          my)))))

#_(recover-cookies)



(defn save-cookies []
  (-> (cookies driver)
      (->> (map #(dissoc % :cookie)))
      (json/generate-string)
      (->> (spit "d:/cookies.json"))))

#_(save-cookies)


(defn add-item [{:keys [pic link desc]} index]
  (if (= 1 index)
    (click driver "div.upload-btn button.pgc-button")
    (click driver "div.figure-state button.figure-add-btn"))
  (send-keys driver "div.upload-handler input" pic)
  (wait-until driver #(exists? % "div.button-group button.confirm-btn") (* 3600 1000) 1000)
  (wait-until driver #(.startsWith (text % "div.image-footer div.drag-tip") "上传完成") (* 3600 1000) 1000)
  (click driver "div.button-group button.confirm-btn")
  (Thread/sleep sleep-time)
  (click driver (str "div.content-wrapper div.pagelet-figure-gallery-item:nth-child(" index ") div.gallery-sub-sale span.slink"))
  (input-text driver "input[name=product_url]" link)
  (click driver "span.product-info-fetch")
  (input-text driver ".tui-input-wrapper textarea[name=recommend_reason]" desc)
  (Thread/sleep sleep-time)
  (click driver "div.gallery-footer button.confirm-btn"))

#_(add-item {:pic "d:\\mei.jpg" :link "https://detail.tmall.com/item.htm?id=556980052128" :desc "11111"} 2)


(defn auto-do []
  (to driver "https://mp.toutiao.com/profile_v2/")
  (wait-until driver #(= (title %) "主页 - 头条号") (* 3600 1000) 1000)
  (when (exists? driver "div.dialog-footer .tui-btn-negative")
    (click driver "div.btn-wrap span.got-it"))
  (click driver "div.shead_right div.shead-post")
  (Thread/sleep sleep-time)
  (when (exists? driver "div.dialog-footer .tui-btn-negative")
    (click driver "div.dialog-footer .tui-btn-negative")
    (click driver "div.shead_right div.shead-post"))
  (click driver "ul.pgc-title li:nth-child(3) a")
  (add-item {:pic "d:\\mei.jpg" :link "https://detail.tmall.com/item.htm?id=556980052128" :desc "11111"} 1)
  (Thread/sleep sleep-time)
  (add-item {:pic "d:\\mei.jpg" :link "https://detail.tmall.com/item.htm?id=556980052128" :desc "11111"} 2)
  (Thread/sleep sleep-time)
  (add-item {:pic "d:\\mei.jpg" :link "https://detail.tmall.com/item.htm?id=556980052128" :desc "11111"} 3)
  (Thread/sleep sleep-time)
  (input-text driver "div.article-title-wrap input" "111111")
  (click driver "div.pgc-radio label.tui-radio-wrapper:nth-child(3) input.tui-radio-input")
  #_(click driver "div.figure-footer div.pgc-btn div.tui-btn"))


(auto-do)
