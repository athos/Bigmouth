(ns bigmouth.models.subscription
  (:require [clojure.spec.alpha :as s]
            [bigmouth.models.account :as account])
  (:import [java.util Date]))

(defprotocol SubscriptionRepository
  (subscribe! [this account callback secret lease-seconds])
  (unsubscribe! [this account callback])
  (find-subscriptions [this account]))

(s/def ::callback string?)
(s/def ::secret string?)
(s/def ::expires_at #(instance? Date %))

(s/def ::subscription
  (s/keys :req-un [::callback ::secret ::expires_at]))

(s/def ::repository
  #(satisfies? SubscriptionRepository %))

(s/fdef subscribe!
  :args (s/cat :this ::repository
               :account ::account/username
               :callback ::callback
               :secret ::secret
               :lease-seconds integer?))

(s/fdef unsubscribe!
  :args (s/cat :this ::repository
               :account ::account/username
               :callback ::callback))

(s/fdef find-subscriptions
  :args (s/cat :this ::repository
               :account ::account/username)
  :ret (s/coll-of ::subscription))

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
      (vals (get @subscriptions account)))))

(defn simple-in-memory-subscription-repository []
  (->SimpleInMemorySubscriptionRepository (atom {})))
