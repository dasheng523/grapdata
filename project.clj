(defproject grapdata "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.logging "0.4.0"]

                 [clj-time "0.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-http "3.7.0"]
                 [dk.ative/docjure "1.11.0"]
                 [clojure-opennlp "0.4.0"]                  ;处理自然语言
                 [com.ashafa/clutch "0.4.0"]
                 [clj-webdriver "0.7.2"]
                 [com.codeborne/phantomjsdriver "1.2.1"
                  :exclusion [org.seleniumhq.selenium/selenium-java
                              org.seleniumhq.selenium/selenium-server
                              org.seleniumhq.selenium/selenium-remote-driver]]
                 [enlive "1.1.6"]
                 [com.novemberain/monger "3.0.0-rc2"]
                 [com.taoensso/timbre "4.1.0"]
                 [io.forward/clojure-mail "1.0.7"]
                 [korma "0.4.3"]
                 [org.clojure/java.jdbc "0.7.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [com.layerware/hugsql "0.4.7"]
                 [mysql/mysql-connector-java "5.1.34"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.8.0"]]
  :main ^:skip-aot grapdata.core
  :target-path "target/%s"
  :jvm-opts ["-Xmx2G"]
  :profiles {:uberjar {:aot :all}})


;[korma "0.4.2"]
;[org.clojure/java.jdbc "0.3.7"]
;[org.postgresql/postgresql "9.2-1002-jdbc4"]
;[com.github.detro.phantomjsdriver "1.2.0"]
