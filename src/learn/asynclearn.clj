(ns learn.asynclearn
  (:gen-class))

(require '[clojure.core.async :as async :refer [<! >! <!! >!! close! alts! alt! alts!! timeout chan alt! go]])

(defn fake-search [kind]
  (fn [c query]
    (go
      (<! (timeout (rand-int 100)))
      (>! c [kind query]))))

(def web1 (fake-search :web1))
(def web2 (fake-search :web2))
(def image1 (fake-search :image1))
(def image2 (fake-search :image2))
(def video1 (fake-search :video1))
(def video2 (fake-search :video2))

(defn fastest [query & replicas]
  (let [c (chan)]
    (doseq [replica replicas]
      (replica c query))
    c))

(defn google [query]
  (let [c (chan)
        t (timeout 80)]
    (go (>! c (<! (fastest query web1 web2))))
    (go (>! c (<! (fastest query image1 image2))))
    (go (>! c (<! (fastest query video1 video2))))
    (go (loop [i 0 ret []]
          (if (= i 3)
            ret
            (recur (inc i) (conj ret (alt! [c t] ([v] v)))))))))

(<!! (google "clojure"))



(let [c (chan 10)]
  (>!! c "hello")
  (assert (= "hello" (<!! c)))
  (close! c))


(let [c1 (chan)
      c2 (chan)]
  (thread (while true
            (let [[v ch] (alts!! [c1 c2])]
              (println "Read" v "from" ch))))
  (>!! c1 "hi")
  (>!! c2 "there"))


(let [ch1 (chan)
      ch2 (chan)]
  (go (dotimes [i 2]
        (prn (str "CH1" (<! ch1)))))
  (go (dotimes [i 2]
        (prn (str "CH2" (<! ch2)))))
  (go (dotimes [i 4]
        (alts! [[ch1 i] [ch2 i]]))))