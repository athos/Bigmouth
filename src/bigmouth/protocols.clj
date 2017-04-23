(ns bigmouth.protocols)

(defprotocol SubscriptionRepository
  (subscribe! [this account callback secret])
  (unsubscribe! [this account callback])
  (find-subscriptions [this account]))
