(ns learn.clutch
  (:use [com.ashafa.clutch :only (create-database put-document) :as clutch]))

(def db (create-database "repl-crud"))

(put-document db {:_id "foo" :some-data "bar"})
