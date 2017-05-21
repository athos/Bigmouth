(ns bigmouth.models.account
  (:require [bigmouth.utils :as utils]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defprotocol AccountRepository
  (find-account [this username])
  (find-account-by-id [this id]))

(s/def ::id string?)
(s/def ::username string?)
(s/def ::description string?)
(s/def ::locked (s/or :true true? :false false?))

(s/def ::account
  (s/keys :req-un [::id ::username]
          :opt-un [::description ::locked]))

(s/def ::account-or-username
  (s/or :account ::account
        :username ::username))

(s/def ::repository
  #(satisfies? AccountRepository %))

(s/fdef find-account
  :args (s/cat :this ::repository
               :username ::username)
  :ret ::account)

(s/fdef find-account-by-id
  :args (s/cat :this ::repository
               :id ::id)
  :ret ::account)

(defn- ensure-account! [accounts username]
  (swap! accounts
         (fn [{:keys [name->account] :as accounts}]
           (if (get name->account username)
             accounts
             (let [id (str (inc (count accounts)))
                   account {:id id
                            :username username
                            :description "no description"
                            :locked false}]
               (-> accounts
                   (assoc-in [:name->account username] account)
                   (assoc-in [:id->account id] account)))))))

;; The implementation below isn't intended to be used in production.
;; It's here mainly for development purpose.

(defrecord SimpleInMemoryAccountRepository [accounts]
  AccountRepository
  (find-account [this username]
    (ensure-account! accounts username)
    (get-in @accounts [:name->account username]))
  (find-account-by-id [this id]
    (get-in @accounts [:id->account id])))

(defn simple-in-memory-account-repository []
  (->SimpleInMemoryAccountRepository (atom {})))

;; utils

(s/fdef ->username
  :args (s/cat :account ::account-or-username)
  :ret ::username)

(defn ->username [account]
  (if (string? account)
    account
    (:username account)))

(s/fdef ->account
  :args (s/cat :account-repo ::repository
               :account ::account-or-username)
  :ret ::account)

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
