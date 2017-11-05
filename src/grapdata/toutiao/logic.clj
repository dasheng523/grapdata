(ns grapdata.toutiao.logic
  (:require [clj-webdriver.taxi :refer :all]
            [cheshire.core :as json]
            [dk.ative.docjure.spreadsheet :as sheet]
            [grapdata.toutiao.driver :as tdriver]
            [grapdata.toutiao.grap :as grap]
            [grapdata.utils :as utils]
            [clojure.string :as str]
            [grapdata.toutiao.config :as config])
  (:import (java.text SimpleDateFormat)))

(def sleep-time 1000)

(defn- read-excel [path cols]
  (->> path
       (sheet/load-workbook)
       (sheet/select-sheet "html")
       (sheet/select-columns cols)))

(defn recover-cookies [driver]
  (let [my-cookies (-> (slurp "d:/cookies.json") (json/parse-string true))]
    (doseq [my my-cookies]
      (add-cookie
        driver
        (if (:expiry my)
          (update my :expiry #(.parse (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") %))
          my)))))


(defn save-cookies [driver]
  (-> (cookies driver)
      (->> (map #(dissoc % :cookie)))
      (json/generate-string)
      (->> (spit "d:/cookies.json"))))


(defn- handle-no-money [driver]
  (click driver "div#pgc-add-product div.pgc-dialog div.dialog-footer button:nth-child(1)")
  (click driver "div.gallery-footer button.confirm-btn")
  (Thread/sleep sleep-time)
  (click driver (str "div.pagelet-figure-gallery-item:last-child div.gallery-action i.icon-delete")))


(defn add-item [driver {:keys [pic link title desc]}]
  (Thread/sleep sleep-time)
  (when pic
    (if (exists? driver "div.content-wrapper div.upload-bg")
      (click driver "div.upload-btn button.pgc-button")
      (click driver "div.figure-state button.figure-add-btn"))
    (send-keys driver "div.upload-handler input" pic)
    (wait-until driver #(exists? % "div.button-group button.confirm-btn") (* 3600 1000) 1000)
    (wait-until driver #(.startsWith (text % "div.image-footer div.drag-tip") "上传完成") (* 3600 1000) 1000)
    (click driver "div.button-group button.confirm-btn")
    (Thread/sleep sleep-time)
    (input-text driver (str "div.content-wrapper div.pagelet-figure-gallery-item:last-child div.gallery-txt textarea") desc)
    (click driver (str "div.content-wrapper div.pagelet-figure-gallery-item:last-child div.gallery-sub-sale span.slink"))
    (input-text driver "input[name=product_url]" link)
    (click driver "span.product-info-fetch")
    (wait-until driver #(not= "" (value % "div.info-wrap div.product-info-item:nth-child(1) input")) (* 3600 1000) 1000)
    (clear driver "div.info-wrap div.product-info-item:nth-child(1) input")
    (input-text driver "div.info-wrap div.product-info-item:nth-child(1) input" title)
    (input-text driver ".tui-input-wrapper textarea[name=recommend_reason]" desc)
    (Thread/sleep sleep-time)
    (if (and
         (exists? driver "div#pgc-add-product div.pgc-dialog div.title")
         (str/includes? (html driver "div#pgc-add-product div.pgc-dialog div.title") "佣金"))
      (handle-no-money driver)
      (click driver "div.gallery-footer button.confirm-btn"))))


(defn add-pic [driver {:keys [pic desc]}]
  (Thread/sleep sleep-time)
  (when pic
    (if (exists? driver "div.content-wrapper div.upload-bg")
      (click driver "div.upload-btn button.pgc-button")
      (click driver "div.figure-state button.figure-add-btn"))
    (send-keys driver "div.upload-handler input" pic)
    (wait-until driver #(exists? % "div.button-group button.confirm-btn") (* 3600 1000) 1000)
    (wait-until driver #(.startsWith (text % "div.image-footer div.drag-tip") "上传完成") (* 3600 1000) 1000)
    (click driver "div.button-group button.confirm-btn")
    (Thread/sleep sleep-time)
    (input-text driver (str "div.content-wrapper div.pagelet-figure-gallery-item:last-child div.gallery-txt textarea") desc)))

#_(add-item {:pic "/Users/huangyesheng/Documents/pics/20171102/1509628939329.png" :desc "ddddd" :link "https://detail.tmall.com/item.htm?id=536273268186" :title "wwww"} 6)

(defn auto-fill-article [driver {:keys [atitle goods]}]
  (doseq [info goods]
    (if (= "" (:link info))
      (add-pic driver info)
      (add-item driver info)))
  (input-text driver "div.article-title-wrap input" atitle)
  (click driver "div.pgc-radio label.tui-radio-wrapper:nth-child(2) span.tui-radio-text")
  #_(click driver "div.figure-footer div.pgc-btn div.tui-btn"))

(defn handle-reset-page [driver]
  (when (and
         (exists? driver "span.action-text")
         (str/includes? (html driver "span.action-text") "撤销"))
    (click driver "span.action-text")))

(defn enter-post-page [driver]
  (click driver "div.shead_right div.shead-post")
  (Thread/sleep sleep-time)
  (when (exists? driver "div.dialog-footer .tui-btn-negative")
    (click driver "div.dialog-footer .tui-btn-negative")
    (Thread/sleep (* 2 sleep-time))
    (click driver "div.shead_right div.shead-post"))
  (Thread/sleep (* 2 sleep-time))
  (click driver "ul.pgc-title li:nth-child(3) a")
  (Thread/sleep sleep-time)
  (handle-reset-page driver)
  (wait-until driver #(exists? % "div.content-wrapper div.upload-bg") (* 3600 1000) 1000))


(defn auto-do [driver]
  (to driver "https://mp.toutiao.com/profile_v2/")
  (wait-until driver #(= (title %) "主页 - 头条号") (* 3600 1000) 1000)
  (when (exists? driver "div.btn-wrap span.got-it")
    (click driver "div.btn-wrap span.got-it"))
  (enter-post-page driver))

(defn- read-data-from-txt [path]
  (-> path
      slurp
      (str/split #"\n")
      (->> (map str/trim))))

(defn- wail-for-ready-post [driver]
  (wait-until driver #(and
                       (exists? % "a.menu_item.selected")
                       (str/includes? (html % "a.menu_item.selected") "内容管理"))
              (* 3600 1000)
              1000))


(def mydriver (tdriver/create-chrome-driver))

(defn run[]
  (let [links (read-data-from-txt config/url-data)
        flist (for [link links] (future (grap/product-item-info link)))]
    (auto-do mydriver)
    (dotimes [n (count links)]
      (let [link (nth links n)
            fut (nth flist n)]
        (auto-fill-article mydriver @fut)
        (wail-for-ready-post mydriver)
        (enter-post-page mydriver)))))

