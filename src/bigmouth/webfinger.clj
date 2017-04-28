(ns bigmouth.webfinger
  (:require [bigmouth.models.account :as account]
            [bigmouth.utils :as utils]))

(defn account-resource [account {:keys [local-domain] :as configs}]
  (let [username (:username account)
        profile-url (utils/profile-url username configs)]
    {:subject (str "acct:" username "@" local-domain)
     :aliases [profile-url]
     :links [{:rel "http://webfinger.net/rel/profile-page"
              :type "text/html"
              :href profile-url}
             {:rel "http://schemas.google.com/g/2010#updates-from"
              :type "application/atom+xml"
              :href (utils/feed-url username configs)}
             {:rel "salmon"
              :href (utils/salmon-url (:id account) configs)}
             {:rel "magic-public-key"
              :href (str "data:application/magic-public-key,"
                         (account/public-key->magic-key (:public_key account)))}
             {:rel "http://ostatus.org/schema/1.0/subscribe"
              :template (str (utils/base-url configs)
                             "/authorize_follow?acct={uri}")}]}))
