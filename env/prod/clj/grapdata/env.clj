(ns grapdata.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[grapdata started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[grapdata has shut down successfully]=-"))
   :middleware identity})
