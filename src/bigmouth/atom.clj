(ns bigmouth.atom
  (:require [bigmouth.utils :as utils]
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
  (let [context {:username account
                 :email (format "%s@%s" account local-domain)
                 :feed-url (utils/feed-url account configs)
                 :account-url (utils/account-url account configs)
                 :profile-url (utils/profile-url account configs)
                 :hub-url (utils/hub-url configs)
                 :salmon-url (utils/salmon-url "FIXME" configs)
                 :entries (into [] (map normalize-entry) entries)}]
    (parser/render-file "atom.xml" context)))
