(ns bigmouth.salmon
  (:require [clj-xpath.core :as xpath]
            [clojure.string :as str]
            [bigmouth.utils :as utils])
  (:import [java.security Signature PublicKey]))

(set! *warn-on-reflection* true)

(def XMLNS "http://salmon-protocol.org/ns/magic-env")

(def VERBS
  {:post           "http://activitystrea.ms/schema/1.0/post"
   :share          "http://activitystrea.ms/schema/1.0/share"
   :favorite       "http://activitystrea.ms/schema/1.0/favorite"
   :unfavorite     "http://activitystrea.ms/schema/1.0/unfavorite"
   :delete         "http://activitystrea.ms/schema/1.0/delete"
   :follow         "http://activitystrea.ms/schema/1.0/follow"
   :request_friend "http://activitystrea.ms/schema/1.0/request-friend"
   :authorize      "http://activitystrea.ms/schema/1.0/authorize"
   :reject         "http://activitystrea.ms/schema/1.0/reject"
   :unfollow       "http://ostatus.org/schema/1.0/unfollow"
   :block          "http://mastodon.social/schema/1.0/block"
   :unblock        "http://mastodon.social/schema/1.0/unblock"})

(defn parse [raw-body]
  (let [[data sig enc alg] (xpath/with-namespace-context {"me" XMLNS}
                             [(first (xpath/$x "//me:data" raw-body))
                              (xpath/$x:text "//me:sig" raw-body)
                              (xpath/$x:text "//me:encoding" raw-body)
                              (xpath/$x:text "//me:alg" raw-body)])
        body (utils/base64-decode (:text data))
        type (get-in data [:attrs :type])
        signature (utils/base64-decode sig)
        plain-text (->> [type enc alg]
                        (map utils/base64-encode)
                        (cons (:text data))
                        (str/join \.))]
    [body plain-text signature]))

(defn unpack [raw-body]
  (let [[^"[B" body _ _] (parse raw-body)]
    (String. body)))

(defn verify [raw-body ^PublicKey public-key]
  (try
    (let [[_, ^String plain-text, ^"[B" signature] (parse raw-body)
          sig (doto (Signature/getInstance "SHA256withRSA")
                (.initVerify public-key)
                (.update (.getBytes plain-text)))]
      (.verify sig signature))
    (catch Exception _
      false)))
