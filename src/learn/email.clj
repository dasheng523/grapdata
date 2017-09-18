(ns learn.email
  (:require [clojure-mail.core :refer :all]
            [clojure-mail.message :as message]
            [clojure.string :as str]
            [learn.utils :as utils]))

(def emails (ref {}))

(defn get-email
  [address password]
  (let [email (find @emails address)]
    (if (nil? email)
      (let [new-email (store "imap" "mail.hyesheng.com" address password)]
        (dosync (alter emails assoc address new-email))
        new-email)
      email)))

(defn close-all-email
  []
  (doseq [[k v] @emails]
    (dosync (alter emails dissoc emails k))
    (close-store v)))


(defn get-facebook-email-code
  [email-store]
  (when-let [latest-message (take 5 (all-messages email-store "inbox"))]
    (-> (utils/find-first #(str/index-of (message/subject %) "是你的 Facebook 验证码") latest-message)
        (message/subject)
        (str/split #" ")
        (first))))


(defn get-facebook-confirm-email-url
  [email-store]
  (-> (first (all-messages email-store "inbox"))
      (message/message-body)
      first
      :body
      (#(re-find #"(http.*confirmemail.*)\s" %))
      ((fn [n]
         (if (> (count n) 1)
           (last n)
           nil)))))

