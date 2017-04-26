(ns bigmouth.protocols)

(defprotocol SubscriptionRepository
  (subscribe! [this account callback secret lease-seconds])
  (unsubscribe! [this account callback])
  (find-subscriptions [this account]))
