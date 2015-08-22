(ns task.testtask
  (:require [grapdata.enginegenerator :as engine]
            [taoensso.timbre :as timbre]
            [grapdata.sikedaodi]))

(defn defalt-task []
  {:task-id "5566"
   :start-link "http://www.15fen.com/category.php?id=1"})

(defn- page-type [html]
  (let [redir (first (:trace-redirects html))]
    (cond
      (.contains redir "www.15fen.com/category.php")
      "list"
      (.contains redir "www.15fen.com/goods.php")
      "detail"
      :else (throw (Exception. "Òì³£ÄÚÈİ")))))

(defn defalt-engine []
  (engine/engine-generator page-type #{[:dt.pro_list_pic :> :a] [:a.page_next]} (defalt-task)))

