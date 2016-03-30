(defproject grapdata "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [korma "0.4.2"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [clj-time "0.10.0"]
                 [clj-http "2.0.0"]
                 [com.ashafa/clutch "0.4.0"]
                 [clj-webdriver "0.7.1"]
                 [enlive "1.1.6"]
                 [org.seleniumhq.selenium/selenium-java "2.45.0"]]
  :main ^:skip-aot grapdata.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
