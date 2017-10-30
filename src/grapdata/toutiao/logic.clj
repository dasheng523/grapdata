(ns grapdata.toutiao.driver
  (:require [clj-webdriver.taxi :refer :all]
            [cheshire.core :as json]
            [grapdata.toutiao.driver :as tdriver])
  (:import (java.text SimpleDateFormat)))

(def driver (tdriver/create-chrome-driver))


(defn recover-cookies []
  (let [my-cookies (-> (slurp "d:/cookies.json") (json/parse-string true))]
    (doseq [my my-cookies]
      (add-cookie
        driver
        (if (:expiry my)
          (update my :expiry #(.parse (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") %))
          my)))))


(defn save-cookies []
  (-> (cookies driver)
      (->> (map #(dissoc % :cookie)))
      (json/generate-string)
      (->> (spit "d:/cookies.json"))))

(recover-cookies)


(defn auto-do []
  (to driver "https://sso.toutiao.com/login/?service=https://mp.toutiao.com/sso_confirm/?redirect_url=/")
  (wait-until driver #(= (title %) "主页 - 头条号") (* 3600 1000) 1000))


(auto-do)




(to driver "https://mp.toutiao.com/profile_v2/")
(click driver "div.btn-wrap span.got-it")
(click driver "div.shead_right div.shead-post")
(click driver "div.dialog-footer .tui-btn-negative")
(click driver "a[href~=figure]")

(click driver "div.edit-input div.pgc-button")

(switch-to-other-window driver)





(send-keys driver "div.upload-handler input" "d:\\mei.jpg")



(let [my-cookies (-> (slurp "d:/cookies.json") (json/parse-string true))]
  (for [my my-cookies]
    (if (::expiry my)
      (update my :expiry #(.parse (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") %))
      my)))

(update {:name "UM_distinctid",
         :value "15f580c8d7d3af-0ed51f390bb6af-5d1b3316-1fa400-15f580c8d7ea6d",
         :path "/",
         :expiry "2018-04-26T09:38:58Z",
         :domain ".toutiao.com",
         :secure? false} :expiry #(.parse (SimpleDateFormat. "yyyy-MM-dd'T'hh:mm:ss'Z'") %))

