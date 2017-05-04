(ns bigmouth.salmon
  (:require [clj-xpath.core :as xpath]
            [clojure.string :as str]
            [bigmouth.utils :as utils])
  (:import [java.security Signature PublicKey]))

(set! *warn-on-reflection* true)

(def XMLNS "http://salmon-protocol.org/ns/magic-env")

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
  (let [[body _ _] (parse raw-body)]
    body))

(defn verify [raw-body ^PublicKey public-key]
  (try
    (let [[_, ^String plain-text, ^"[B" signature] (parse raw-body)
          sig (doto (Signature/getInstance "SHA256withRSA")
                (.initVerify public-key)
                (.update (.getBytes plain-text)))]
      (.verify sig signature))
    (catch Exception _
      false)))
