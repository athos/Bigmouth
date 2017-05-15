(ns bigmouth.interaction
  (:require [bigmouth.models.account :as account]
            [clojure.spec.alpha :as s]))

(defprotocol InteractionHandler
  (post [this account])
  (share [this account])
  (delete [this account])
  (follow [this account target])
  (unfollow [this account target])
  (request-friend [this account target])
  (authorize [this account target])
  (reject [this account target])
  (favorite [this account])
  (unfavorite [this account])
  (block [this account target])
  (unblock [this account target]))

(s/def ::handler
  #(satisfies? InteractionHandler %))

(s/fdef post
  :args (s/cat :this ::handler
               :account ::account/username))

(s/fdef share
  :args (s/cat :this ::handler
               :account ::account/username))

(s/fdef delete
  :args (s/cat :this ::handler
               :account ::account/username))

(s/fdef follow
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(s/fdef unfollow
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(s/fdef request-friend
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(s/fdef authorize
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(s/fdef reject
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(s/fdef favorite
  :args (s/cat :this ::handler
               :account ::account/username))

(s/fdef unfavorite
  :args (s/cat :this ::handler
               :account ::account/username))

(s/fdef block
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(s/fdef unblock
  :args (s/cat :this ::handler
               :account ::account/username
               :target ::account/username))

(extend-protocol InteractionHandler
  nil
  (post [this account])
  (share [this account])
  (delete [this account])
  (follow [this account target])
  (unfollow [this account target])
  (request-friend [this account target])
  (authorize [this account target])
  (reject [this account target])
  (favorite [this account])
  (unfavorite [this account])
  (block [this account target])
  (unblock [this account target]))
