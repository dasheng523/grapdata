(ns learn.email
  (:require [clojure-mail.core :refer :all]
            [clojure-mail.message :as message]))


(def wangyi-store (store "imap" "mail.hyesheng.com" "test@hyesheng.com" "a5235013"))


(def my-inbox-message (take 5 (all-messages wangyi-store "inbox")))


(def first-message (first my-inbox-message))
(message/subject first-message)
