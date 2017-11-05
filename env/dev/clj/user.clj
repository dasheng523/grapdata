(ns user
  (:require [luminus-migrations.core :as migrations]
            [grapdata.config :refer [env]]
            [mount.core :as mount]
            [grapdata.figwheel :refer [start-fw stop-fw cljs]]
            grapdata.core))

(defn start []
  (mount/start-without #'grapdata.core/repl-server))

(defn stop []
  (mount/stop-except #'grapdata.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))


