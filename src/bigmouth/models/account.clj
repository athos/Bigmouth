(ns bigmouth.models.account
  (:require [bigmouth.utils :as utils]
            [clojure.string :as str])
  (:import [java.security KeyPair KeyPairGenerator KeyFactory]
           [java.security.interfaces RSAPublicKey]
           [java.security.spec RSAPublicKeySpec]))

(set! *warn-on-reflection* true)

(defprotocol AccountRepository
  (find-account [this username]))

(defn fresh-keypair
  ([] (fresh-keypair 2048))
  ([key-size]
   (let [gen (doto (KeyPairGenerator/getInstance "RSA")
               (.initialize (int key-size)))]
     (.generateKeyPair gen))))

(defn- ensure-account! [accounts username]
  (swap! accounts
         (fn [accounts]
           (if (get accounts username)
             accounts
             (let [id (inc (count accounts))
                   ^KeyPair keypair (fresh-keypair)]
               (assoc accounts username
                      {:id id
                       :username username
                       :description "no description"
                       :locked false
                       :public_key (.getPublic keypair)
                       :private_key (.getPrivate keypair)}))))))

;; The implementation below isn't intended to be used in production.
;; It's here mainly for development purpose.

(defrecord SimpleInMemoryAccountRepository [accounts]
  AccountRepository
  (find-account [this username]
    (ensure-account! accounts username)
    (get @accounts username)))

(defn simple-in-memory-account-repository []
  (->SimpleInMemoryAccountRepository (atom {})))

;; utils

(defn ->username [account]
  (if (string? account)
    account
    (:username account)))

(defn ->account [account-repo account]
  (if (string? account)
    (find-account account-repo account)
    account))

(defn feed-url [account configs]
  (format "%s/users/%s.atom" (utils/base-url configs) (->username account)))

(defn account-url [account configs]
  (format "%s/users/%s" (utils/base-url configs) (->username account)))

(defn profile-url [account configs]
  (format "%s/@%s" (utils/base-url configs) (->username account)))

(defn hub-url [configs]
  (format "%s/api/push" (utils/base-url configs)))

(defn salmon-url [account configs]
  (format "%s/salmon/%s" (utils/base-url configs) (:id account)))

(defn public-key->magic-key [^RSAPublicKey key]
  (let [conv #(utils/base64-encode (.toByteArray ^BigInteger %))
        modulus (conv (.getModulus key))
        exponent (conv (.getPublicExponent key))]
    (str "RSA." modulus "." exponent)))

(defn magic-key->public-key [magic-key]
  (let [conv (fn [x]
               (let [bytes (utils/base64-decode x)]
                 (if (neg? (aget bytes 0))
                   (let [len (count bytes)
                         arr (byte-array (inc len))]
                     (aset arr 0 (byte 0))
                     (System/arraycopy bytes 0 arr 1 len)
                     (BigInteger. arr))
                   (BigInteger. bytes))))
        [_ modulus exponent] (str/split magic-key #"\.")
        spec (RSAPublicKeySpec. (conv modulus) (conv exponent))]
    (.. (KeyFactory/getInstance "RSA")
        (generatePublic spec))))
