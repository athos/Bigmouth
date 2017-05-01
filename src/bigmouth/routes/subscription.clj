(ns bigmouth.routes.subscription
  (:require [bigmouth.models.account :as account]
            [bigmouth.models.subscription :as subs]
            [org.httpkit.client :as http]
            [pandect.utils.convert :as conv]
            [ring.util.response :as res])
  (:import [java.security SecureRandom]))

(defn- secure-random
  ([] (secure-random 16))
  ([size]
   (let [r (SecureRandom/getInstanceStrong)
         bs (byte-array size)]
     (.nextBytes r bs)
     (conv/bytes->hex bs))))

(defn subscribe [subscription-repo params configs]
  (let [topic (get params "hub.topic")
        secret (get params "hub.secret")
        callback (get params "hub.callback")
        lease-seconds (get params "hub.lease_seconds")
        lease-seconds (if (empty? lease-seconds)
                        (* 86400 7)
                        (-> (Long/parseLong lease-seconds)
                            (min (* 86400 30))
                            (max (* 86400 7))))
        [_ account] (re-find #"/users/([^.]+?).atom$" topic)
        challenge (secure-random)]
    (http/get callback
              {:query-params {:hub.topic (account/feed-url account configs)
                              :hub.mode "subscribe"
                              :hub.challenge challenge
                              :hub.lease_seconds lease-seconds}}
              (fn [{:keys [status error body]}]
                (when (and (= status 200) (not error) (= body challenge))
                  (subs/subscribe! subscription-repo account callback secret lease-seconds))))
    (res/status {} 202)))

(defn unsubscribe [subscription-repo params]
  (prn :params params))
