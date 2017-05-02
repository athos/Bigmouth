(ns bigmouth.status-updater
  (:require [bigmouth.atom :as atom]
            [bigmouth.models.account :as account]
            [bigmouth.models.subscription :as subs]
            [bigmouth.utils :as utils]
            [org.httpkit.client :as http]
            [pandect.algo.sha1 :as sha1]))

(defrecord StatusUpdater [context])

(defn make-status-updater [context]
  (->StatusUpdater context))

(defn update! [this username entry]
  (let [{:keys [accounts subscriptions configs]} (:context this)
        account (account/find-account accounts username)
        atom-feed (atom/atom-feed account [entry] configs)]
    (doseq [subs (subs/find-subscriptions subscriptions username)
            :let [sig (sha1/sha1-hmac atom-feed (:secret subs))]]
      (http/post (:callback subs)
                 {:headers {"X-Hub-Signature" (str "sha1=" sig)}
                  :body atom-feed}))))
