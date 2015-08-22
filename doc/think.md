engine:
用来放各种抓取的主要逻辑
类似graper-generator
可存放多个。

最终的接口是：
(defprotocol Grapable
  ;开始抓取任务
  (start-grap [grap-task])
  ;暂停抓取任务
  (stop-grap [grap-task])
  ;结束抓取任务
  (end-grap [grap-task])
  ;重新开始抓取任务
  (restart-grap [grap-task]))

grap-task，随便怎么字段吧，都一样的，看实现需要什么。

CommonImpl：
grap-task就是一个id,一个start-url即可。
但如果这样的话，我每次调用其中的方法，都只能根据id去查找共享资源的内容，我不想这么做。我希望每个任务都是相互独立的。
不，如果将任务放入一个容器中，我可以对容器进行操作和管理，这样就更灵活了。嗯，就这么来吧。
但是这样的话，如果我需要保存更多的东西呢？

闭包是不是被我用烂了？

