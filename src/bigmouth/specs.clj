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

(s/def ::use-https? (s/or :true true? :false false?))
(s/def ::local-domain string?)
(s/def ::configs
  (s/keys :req-un [::local-domain]
          :opt-un [::use-https?]))

(s/def ::context
  (s/keys :req-un [::accounts ::keystore ::subscriptions ::configs]
          :opt-un [::interaction-handler]))
