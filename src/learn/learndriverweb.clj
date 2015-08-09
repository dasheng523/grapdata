(ns learn.learndriverweb)
(use 'clj-webdriver.taxi)

(defn run []
  (System/setProperty "webdriver.chrome.driver" "E:\\devkit\\chromedriver.exe")
  (set-driver! {:browser :firefox} "https://console.upyun.com/")
  (println (page-source))
  (wait-until (fn [] (if (find-element {:css "#username"}) true false)))
  (input-text "#username" "myusername")
  (input-text "#password" "123456")
  (to "http://www.baidu.com")
  (input-text "#kw" "test"))


;(def driver (new-driver {:browser :chrome}))
;(set-driver! driver)

;(to "https://console.upyun.com/#/login")
;(to driver "https://console.upyun.com/#/login")