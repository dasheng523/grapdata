(ns learn.facebook
  (:require [learn.db :as ldb]
            [learn.name :as lname]))


(defn create-users
  "创建N个邮箱账号,前缀"
  [n prefix]
  (let [range-num (shuffle (range 1 (+ n 1)))]
    (dotimes [i n]
      (let [name-generate (lname/gen-name-data-as-map)
            email (str prefix (format "%05d" (get range-num i)) "@hyesheng.com")
            password (str "PW,p" (format "%05d" (get range-num i)))]
        (ldb/grap-insert
          "users"
          {"email" email
           "password" password
           "firstname" (:first-name name-generate)
           "lastname" (-> name-generate :surnames first)
           "sex" (if (= :female (:gender name-generate)) 0 1)
           "enable" 1
           "birthday" (str (+ (rand-int 15) 1985) "-" (format "%02d" (+ (rand-int 11) 1)) "-" (format "%02d" (+ (rand-int 27) 1)))})))))


(defn create-mail-users
  []
  (let [users (ldb/get-all-users)]
    (for [n users]
      (ldb/insert-mail-user (:email n) (:password n)))))

(defn process
  []
  (ldb/clear-grap-users)
  (create-users 1000 "aa")
  (ldb/clear-mail-users)
  (create-mail-users))
