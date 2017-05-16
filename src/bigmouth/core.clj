(ns bigmouth.core
  (:require [bigmouth.routes :as routes]
            [bigmouth.specs :as specs]
            [bigmouth.status-updater :as updater]
            [clojure.spec.alpha :as s]))

(s/fdef status-updater
  :args (s/cat :context ::specs/context))

(defn status-updater [context]
  (updater/make-status-updater context))

(s/fdef bigmouth-handler
  :args (s/cat :context ::specs/context))

(defn bigmouth-routes [context]
  (routes/make-bigmouth-routes context))
