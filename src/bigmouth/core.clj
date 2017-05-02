(ns bigmouth.core
  (:require [bigmouth.routes :as routes]
            [bigmouth.status-updater :as updater]))

(defn bigmouth [context]
  {:handler (routes/make-bigmouth-routes context)
   :updater (updater/make-status-updater context)})
