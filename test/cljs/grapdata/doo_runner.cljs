(ns grapdata.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [grapdata.core-test]))

(doo-tests 'grapdata.core-test)

