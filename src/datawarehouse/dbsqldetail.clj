(ns datawarehouse.dbsqldetail
  (:use [korma.db]
        [korma.core]))

(defdb stock-db (postgres {:db "stock"
                           :user "postgres"
                           :password "a5235013"}))



(defentity vegetablepics
           (database stock-db))

(defentity vegetables
           (database stock-db))

(defn insert-vegetables [vegetable]
  (-> (insert* vegetables)
      (values vegetable)
      (insert)))

(defn insert-vegetablepics [vegetablepic]
  (-> (insert* vegetablepics)
      (values vegetablepic)
      (insert)))

(defn insert-vegetable-data [vegetable picurls]
  (let [veg-id (:id (insert-vegetables vegetable))]
    (doseq [picurl picurls]
      (insert-vegetablepics
        {:veg_id veg-id
         :url picurl}))))