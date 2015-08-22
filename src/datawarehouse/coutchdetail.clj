(ns datawarehouse.coutchdetail
  (:require [com.ashafa.clutch :as clutch]))

(def html-db (clutch/get-database "html-db"))
(def domain-db (clutch/get-database "domain-db"))

;就是想做一个可以传入db选项的东西，但可以返回一个方法的集合。这个不懂写，算了。
;倒不如直接在这里写操作clutch的函数。其他不管。


