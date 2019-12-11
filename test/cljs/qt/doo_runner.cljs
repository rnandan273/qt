(ns qt.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [qt.core-test]))

(doo-tests 'qt.core-test)

