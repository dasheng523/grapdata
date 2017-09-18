(ns learn.twitter
  (:require [clj-webdriver.taxi :refer :all]
            [learn.email :as email-util]))

(defn register
  [driver user]
  (try
    (delete-all-cookies driver)
    (to driver "https://twitter.com/signup")
    (input-text driver "#full-name" (str (:firstname user) (:lastname user)))
    (input-text driver "#email" (:email user))
    (input-text driver "#password" (:password user))

    (wait-until #())
    (click driver "#submit_button")


    (finally
      (println "finish")
      (quit driver))))