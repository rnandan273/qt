(ns qt.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[qt started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[qt has shut down successfully]=-"))
   :middleware identity})
