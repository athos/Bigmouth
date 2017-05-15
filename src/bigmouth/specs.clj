(ns bigmouth.specs
  (:require [bigmouth.models.account :as account]
            [bigmouth.models.keystore :as keystore]
            [bigmouth.models.subscription :as subs]
            [bigmouth.interaction :as interaction]
            [clojure.spec.alpha :as s]))

(s/def ::accounts ::account/repository)
(s/def ::keystore ::keystore/keystore)
(s/def ::subscriptions ::subs/repository)
(s/def ::interaction-handler ::interaction/handler)

(s/def ::context
  (s/keys :req-un [::accounts ::keystore ::subscriptions]
          :opt-un [::interaction-handler]))
