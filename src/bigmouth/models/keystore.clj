(ns bigmouth.models.keystore
  (:require [bigmouth.models.account :as account]
            [clojure.spec.alpha :as s])
  (:import [java.security PublicKey PrivateKey KeyPair KeyPairGenerator]))

(set! *warn-on-reflection* true)

(defprotocol KeyStore
  (find-public-key* [this username domain])
  (find-private-key* [this username])
  (save-key!* [this username domain public-key]))

(s/def ::domain string?)

(s/def ::public-key
  #(instance? PublicKey %))

(s/def ::private-key
  #(instance? PrivateKey %))

(s/def ::keystore
  #(satisfies? KeyStore %))

(s/fdef find-public-key*
  :args (s/cat :this ::keystore
               :username ::account/username
               :domain ::domain)
  :ret ::public-key)

(s/fdef find-private-key*
  :args (s/cat :this ::keystore
               :username ::account/username)
  :ret ::private-key)

(s/fdef save-key!*
  :args (s/cat :this ::keystore
               :username ::account/username
               :domain ::domain
               :public-key ::public-key))

(defn- destruct-name [name]
  (let [[_ username domain] (re-matches #"([^@]+?)(?:@([^@]+))?" name)]
    [username domain]))

(defn find-public-key [this account]
  (let [[username domain] (destruct-name (account/->username account))]
    (find-public-key* this username domain)))

(defn find-private-key [this account]
  (find-private-key* this (account/->username account)))

(defn save-key! [this account public-key]
  (let [[username domain] (destruct-name (account/->username account))]
    (save-key!* this username domain public-key)))

(defn ^KeyPair fresh-keypair
  ([] (fresh-keypair 2048))
  ([key-size]
   (let [gen (doto (KeyPairGenerator/getInstance "RSA")
               (.initialize (int key-size)))]
     (.generateKeyPair gen))))

;; The implementation below isn't intended to be used in production.
;; It's here mainly for development purpose.

(declare ensure-keypair!)

(defrecord SimpleInMemoryKeyStore [keys]
  KeyStore
  (find-private-key* [this username]
    (:private (ensure-keypair! keys username)))
  (find-public-key* [this username domain]
    (if domain
      (get-in @keys [[username domain] :public])
      (:public (ensure-keypair! keys username))))
  (save-key!* [this username domain public-key]
    (swap! keys assoc-in [[username domain] :public] public-key)))

(defn simple-in-memory-keystore []
  (->SimpleInMemoryKeyStore (atom {})))

(defn- ^KeyPair ensure-keypair! [keys username]
  (swap! keys update [username nil]
         (fn [keypair]
           (or keypair
               (let [key (fresh-keypair)]
                 {:public (.getPublic key)
                  :private (.getPrivate key)}))))
  (get @keys [username nil]))
