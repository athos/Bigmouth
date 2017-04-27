(ns bigmouth.models.account
  (:import [java.security KeyPair KeyPairGenerator]))

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
