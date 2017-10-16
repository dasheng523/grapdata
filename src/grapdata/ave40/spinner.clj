(ns grapdata.ave40.spinner
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [grapdata.ave40.db :refer :all]))

(defn- split-content-len [content maxlen]
  (reduce (fn [v sen]
            (let [last-sen (last v)
                  add-sen (str last-sen "\n" sen)]
              (if (< (count add-sen) maxlen) (conj (vec (drop-last v)) add-sen) (conj v sen))))
          []
          (str/split content #"\n")))


(defn- spinner-login []
  (let [resp (-> (http/post "http://thebestspinner.com/api.php"
                            {:form-params {:action "authenticate"
                                           :format "xml"
                                           :username "515462418@qq.com"
                                           :password "40U30600U0034383W"}})
                 :body)
        session (re-find #"<session>([\S\s]*?)</session>" resp)]
    (if session (second session) (throw (Exception. "session is null")))))


(defn- spinner-post [params]
  (let [resp (-> (http/post "http://thebestspinner.com/api.php"
                            {:form-params params})
                 :body)
        output (re-find #"<output>([\S\s]*?)</output>" resp)]
    (if output (second output)
               (throw (Exception. (str resp))))))

(defn- spinner-synonyms [text session]
  (spinner-post {:action "identifySynonyms"
                 :session session
                 :format "xml"
                 :text text}))

(defn- spinner-sentences [text session]
  (spinner-post {:action "rewriteSentences"
                 :session session
                 :format "xml"
                 :text text}))

(defn- spinner-randomSpin [text session]
  (spinner-post {:action "randomSpin"
                 :session session
                 :format "xml"
                 :text text}))

(defn- spinner-parse [session text]
  (->
    text
    (spinner-synonyms session)
    (spinner-randomSpin session)
    (spinner-sentences session)
    (spinner-randomSpin session)))

(defn create-spinner []
  (let [session (spinner-login)]
    (partial spinner-parse session)))

(defn- handle-and-save [article spinner]
  (let [title (spinner (:title article))
        pies (str/split (:article article) #"\n")
        content (str/join "\n" (for [p pies] (if p (spinner p))))]
    (println (:title article))
    (update-data
      article-db
      {:table "articles"
       :updates {:spinner_title title :spinner_article content}
       :where (str "id=" (:id article))})))

(defn- handle-list-and-save [list spinner]
  (doseq [info list]
    (handle-and-save info spinner)))

(defn muti-run-spinner [n]
  (let [articles (select-all article-db {:table "articles" :where "isnull(spinner_title)"})
        pies (partition (quot (count articles) n) articles)
        spinner (create-spinner)]
    (doseq [pie pies]
      (future (handle-list-and-save pie spinner)))))

(defn simple-run-spinner []
  (let [articles (select-all article-db {:table "articles" :where "isnull(spinner_title)"})
        spinner (create-spinner)]
    (doseq [article articles]
      (try
        (let [title (spinner (:title article))
              pies (str/split (:article article) #"\n")
              content (str/join "\n" (for [p pies] (if p (spinner p))))]
          (println (:title article))
          (update-data
            article-db
            {:table "articles"
             :updates {:spinner_title title :spinner_article content}
             :where (str "id=" (:id article))}))
        (catch Exception e
          (log/error e))))))

