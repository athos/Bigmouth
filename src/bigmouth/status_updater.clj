(ns bigmouth.status-updater
  (:require [bigmouth.atom :as atom]
            [bigmouth.models.subscription :as subs]
            [bigmouth.utils :as utils]
            [org.httpkit.client :as http]
            [pandect.algo.sha1 :as sha1]))

(defrecord StatusUpdater [subscription-repo configs])

(defn make-status-updater [subscription-repo configs]
  (->StatusUpdater subscription-repo configs))

(defn update! [this account entry]
  (let [subscription-repo (:subscription-repo this)
        atom-feed (atom/atom-feed account [entry] (:configs this))]
    (doseq [subs (subs/find-subscriptions subscription-repo account)
            :let [sig (sha1/sha1-hmac atom-feed (:secret subs))]]
      (http/post (:callback subs)
                 {:headers {"X-Hub-Signature" (str "sha1=" sig)}
                  :body atom-feed}))))
