(ns ^:figwheel-no-load grapdata.app
  (:require [grapdata.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
