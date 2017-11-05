(ns grapdata.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [grapdata.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[grapdata started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[grapdata has shut down successfully]=-"))
   :middleware wrap-dev})
