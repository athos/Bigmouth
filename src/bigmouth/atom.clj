(ns bigmouth.atom
  (:require [bigmouth.models.account :as account]
            [bigmouth.utils :as utils]
            [selmer.parser :as parser])
  (:import [java.util Date]))

(defn- normalize-entry [entry]
  (let [now (Date.)]
    (cond-> entry
      (nil? (:created_at entry)) (assoc :created_at now)
      (nil? (:updated_at entry)) (assoc :updated_at now)
      (nil? (:scope entry)) (assoc :scope "public")
      (not (string? (:scope entry))) (update :scope name))))

(defn atom-feed [account entries {:keys [local-domain] :as configs}]
  (let [username (:username account)
        context {:username username
                 :description (:description account)
                 :email (format "%s@%s" username local-domain)
                 :feed-url (account/feed-url account configs)
                 :account-url (account/account-url account configs)
                 :profile-url (account/profile-url account configs)
                 :hub-url (utils/hub-url configs)
                 :salmon-url (account/salmon-url account configs)
                 :entries (into [] (map normalize-entry) entries)}]
    (parser/render-file "atom.xml" context)))
