(ns grapdata.toutiao.driver
  (:require [clj-webdriver.taxi :refer :all]
            [cheshire.core :as json]
            [grapdata.toutiao.driver :as tdriver]
            [grapdata.toutiao.grap :as grap]
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


(defn add-item [{:keys [pic link title desc]} index]
  (Thread/sleep sleep-time)
  (if (= 1 index)
    (click driver "div.upload-btn button.pgc-button")
    (click driver "div.figure-state button.figure-add-btn"))
  (println pic)
  (send-keys driver "div.upload-handler input" pic)
  (wait-until driver #(exists? % "div.button-group button.confirm-btn") (* 3600 1000) 1000)
  (wait-until driver #(.startsWith (text % "div.image-footer div.drag-tip") "上传完成") (* 3600 1000) 1000)
  (click driver "div.button-group button.confirm-btn")
  (Thread/sleep sleep-time)
  (input-text driver (str "div.content-wrapper div.pagelet-figure-gallery-item:nth-child(" index ") div.gallery-txt textarea") desc)
  (click driver (str "div.content-wrapper div.pagelet-figure-gallery-item:nth-child(" index ") div.gallery-sub-sale span.slink"))
  (input-text driver "input[name=product_url]" link)
  (click driver "span.product-info-fetch")
  (wait-until driver #(not= "" (value % "div.info-wrap div.product-info-item:nth-child(1) input")) (* 3600 1000) 1000)
  (clear driver "div.info-wrap div.product-info-item:nth-child(1) input")
  (input-text driver "div.info-wrap div.product-info-item:nth-child(1) input" title)
  (input-text driver ".tui-input-wrapper textarea[name=recommend_reason]" desc)
  (Thread/sleep sleep-time)
  (click driver "div.gallery-footer button.confirm-btn"))


(defn add-pic [{:keys [pic desc]} index]
  (Thread/sleep sleep-time)
  (if (= 1 index)
    (click driver "div.upload-btn button.pgc-button")
    (click driver "div.figure-state button.figure-add-btn"))
  (println pic)
  (send-keys driver "div.upload-handler input" pic)
  (wait-until driver #(exists? % "div.button-group button.confirm-btn") (* 3600 1000) 1000)
  (wait-until driver #(.startsWith (text % "div.image-footer div.drag-tip") "上传完成") (* 3600 1000) 1000)
  (click driver "div.button-group button.confirm-btn")
  (Thread/sleep sleep-time)
  (input-text driver (str "div.content-wrapper div.pagelet-figure-gallery-item:nth-child(" index ") div.gallery-txt textarea") desc))

#_(add-item {:pic "/Users/huangyesheng/Documents/pics/20171102/1509628939329.png" :desc "ddddd" :link "https://detail.tmall.com/item.htm?id=536273268186" :title "wwww"} 6)

(defn auto-fill-article [{:keys [atitle goods]}]
  (dotimes [n (count goods)]
    (let [info (get goods n)]
      (if (= "" (:link info))
        (add-pic info (+ 1 n))
        (add-item info (+ 1 n)))))
  (input-text driver "div.article-title-wrap input" atitle)
  (click driver "div.pgc-radio label.tui-radio-wrapper:nth-child(3) input.tui-radio-input")
  #_(click driver "div.figure-footer div.pgc-btn div.tui-btn"))

(defn auto-do []
  (to driver "https://mp.toutiao.com/profile_v2/")
  (wait-until driver #(= (title %) "主页 - 头条号") (* 3600 1000) 1000)
  (when (exists? driver "div.btn-wrap span.got-it")
    (click driver "div.btn-wrap span.got-it"))
  (click driver "div.shead_right div.shead-post")
  (Thread/sleep sleep-time)
  (when (exists? driver "div.dialog-footer .tui-btn-negative")
    (click driver "div.dialog-footer .tui-btn-negative")
    (Thread/sleep sleep-time)
    (click driver "div.shead_right div.shead-post"))
  (Thread/sleep (* 2 sleep-time))
  (click driver "ul.pgc-title li:nth-child(3) a")
  (Thread/sleep sleep-time))


(auto-do)
(-> (grap/product-item-info "http://www.51taojinge.com/jinri/temai_content_article.php?id=4066141")
    (auto-fill-article))
