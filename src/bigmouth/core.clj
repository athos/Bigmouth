(ns bigmouth.core
  (:require [bigmouth.routes :as routes]
            [bigmouth.specs :as specs]
            [bigmouth.status-updater :as updater]
            [clojure.spec.alpha :as s]))

(s/fdef bigmouth
  :args (s/cat :context ::specs/context))

(defn bigmouth [context]
  {:handler (routes/make-bigmouth-routes context)
   :updater (updater/make-status-updater context)})
