(ns bigmouth.webfinger
  (:require [bigmouth.models.account :as account]
            [bigmouth.utils :as utils]))

(defn account-resource [account {:keys [local-domain] :as configs}]
  (let [profile-url (account/profile-url account configs)]
    {:subject (str "acct:" (:username account) "@" local-domain)
     :aliases [profile-url]
     :links [{:rel "http://webfinger.net/rel/profile-page"
              :type "text/html"
              :href profile-url}
             {:rel "http://schemas.google.com/g/2010#updates-from"
              :type "application/atom+xml"
              :href (account/feed-url account configs)}
             {:rel "salmon"
              :href (account/salmon-url account configs)}
             {:rel "magic-public-key"
              :href (str "data:application/magic-public-key,"
                         (account/public-key->magic-key (:public_key account)))}
             {:rel "http://ostatus.org/schema/1.0/subscribe"
              :template (str (utils/base-url configs)
                             "/authorize_follow?acct={uri}")}]}))
