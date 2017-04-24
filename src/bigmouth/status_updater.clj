(ns bigmouth.status-updater
  (:require [bigmouth.protocols :as proto]
            [bigmouth.utils :as utils]
            [org.httpkit.client :as http]
            [pandect.algo.sha1 :as sha1]))

(defrecord StatusUpdater [subscription-repo configs])

(defn make-status-updater [subscription-repo configs]
  (->StatusUpdater subscription-repo configs))

(defn update! [this account content]
  (let [subscription-repo (:subscription-repo this)]
    (doseq [subs (proto/find-subscriptions subscription-repo account)
            :let [sig (sha1/sha1-hmac content (:secret subs))]]
      (http/post (:callback subs)
                 {:headers {"X-Hub-Signature" (str "sha1=" sig)}
                  :body content}))))
