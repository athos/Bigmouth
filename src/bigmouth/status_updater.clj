(ns bigmouth.status-updater
  (:require [bigmouth.atom :as atom]
            [bigmouth.models.account :as account]
            [bigmouth.models.subscription :as subs]
            [bigmouth.utils :as utils]
            [org.httpkit.client :as http]
            [pandect.algo.sha1 :as sha1]))

(defrecord StatusUpdater [account-repo subscription-repo configs])

(defn make-status-updater [account-repo subscription-repo configs]
  (->StatusUpdater account-repo subscription-repo configs))

(defn update! [this username entry]
  (let [account-repo (:account-repo this)
        subscription-repo (:subscription-repo this)
        account (account/find-account account-repo username)
        atom-feed (atom/atom-feed account [entry] (:configs this))]
    (doseq [subs (subs/find-subscriptions subscription-repo username)
            :let [sig (sha1/sha1-hmac atom-feed (:secret subs))]]
      (http/post (:callback subs)
                 {:headers {"X-Hub-Signature" (str "sha1=" sig)}
                  :body atom-feed}))))
