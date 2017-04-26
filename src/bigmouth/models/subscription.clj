(ns bigmouth.models.subscription
  (:import [java.util Date]))

(defprotocol SubscriptionRepository
  (subscribe! [this account callback secret lease-seconds])
  (unsubscribe! [this account callback])
  (find-subscriptions [this account]))

;; The implementation below isn't intended to be used in production.
;; It's here mainly for development purpose.

(defrecord SimpleInMemorySubscriptionRepository [subscriptions]
  SubscriptionRepository
  (subscribe! [this account callback secret lease-seconds]
    (let [subsription {:callback callback :secret secret
                       :expires_at (+ (.getTime (Date.))
                                      (* 1000 lease-seconds))}]
      (swap! subscriptions assoc-in [account callback] subsription)))
  (unsubscribe! [this account callback]
    (swap! subscriptions update account dissoc callback))
  (find-subscriptions [this account]
    (let [now (.getTime (Date.))]
      (doseq [subs (vals (get @subscriptions account))
              :when (<= (:expires_at subs) now)]
        (swap! subscriptions update account dissoc (:callback subs)))
      (vals (get @subscriptions account))))
  ;; for debugging
  clojure.lang.IFn
  (invoke [this] @subscriptions))

(defn simple-in-memory-subscription-repository []
  (->SimpleInMemorySubscriptionRepository (atom {})))
