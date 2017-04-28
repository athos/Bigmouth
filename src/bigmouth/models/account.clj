(ns bigmouth.models.account
  (:require [clojure.string :as str])
  (:import [java.security KeyPair KeyPairGenerator KeyFactory]
           [java.security.interfaces RSAPublicKey]
           [java.security.spec RSAPublicKeySpec]
           [java.util Base64]))

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

(defn public-key->magic-key [^RSAPublicKey key]
  (let [encoder (Base64/getUrlEncoder)
        conv #(.encodeToString encoder (.toByteArray ^BigInteger %))
        modulus (conv (.getModulus key))
        exponent (conv (.getPublicExponent key))]
    (str "RSA." modulus "." exponent)))

(defn magic-key->public-key [magic-key]
  (let [decoder (Base64/getUrlDecoder)
        conv #(BigInteger. (.decode decoder ^String %))
        [_ modulus exponent] (str/split magic-key #"\.")
        spec (RSAPublicKeySpec. (conv modulus) (conv exponent))]
    (.. (KeyFactory/getInstance "RSA")
        (generatePublic spec))))
