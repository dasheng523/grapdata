(ns learn.learnmongo
  (:require [monger.core :as mg]
            [monger.collection :as mc]))

(let [conn (mg/connect)
      db   (mg/get-db conn "monger-test")
      coll "documents"]
  (mc/insert db coll {:first-name "John" :last-name "Lennon"})
  (mc/insert db coll {:first-name "dasheng" :last-name "huang"})
  (println (mc/find db coll {:first-name "dasheng"}))
  (mg/disconnect conn))
