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


要再写一次sikedaodi，不然总觉得有什么问题在这。把他写成一个控件，用来启动，关闭，重启，持久化。
  现在已经有了一个actuator控件了，但是下一步不清楚干什么。要用一个别人的做法来做吧。
    那么就应该首先查看一下有没有类似的控件可以参考。
      要搞清楚为什么不能暂停， save不会操作，以及如何进行传参。
      recover函数该返回什么？
      save和recover都不在那边做。
    如何加入真实的逻辑。
      写一个接口再说
      实现这个接口
        写出各种引用的依赖关系
          actuator -> ActuatorRecord -> executor -> engine
        那么想要的使用方式是什么呢？
        这个executor里面的go如何处理。

禁止进行优化！！！