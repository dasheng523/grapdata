(ns grapdata.grap-protocols)



;抓取最外层接口
(defprotocol GrapInterface
  (start [])
  (stop [])
  (save [])
  (recover []))

