(ns bigmouth.webfinger
  (:require [bigmouth.models.account :as account]
            [bigmouth.models.keystore :as keystore]
            [bigmouth.utils :as utils]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]))

(defn account-resource [{:keys [configs] :as context} account]
  (let [profile-url (account/profile-url account configs)
        public-key (keystore/find-public-key (:keystore context) account)]
    {:subject (str "acct:" (:username account) "@" (:local-domain configs))
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
                         (account/public-key->magic-key public-key))}
             {:rel "http://ostatus.org/schema/1.0/subscribe"
              :template (str (utils/base-url configs)
                             "/authorize_follow?acct={uri}")}]}))

(defn fetch-remote-account-resource [account-id]
  (let [[_ username domain] (re-matches #"([^@]+?)@([^@]+)" account-id)
        ;; TODO: it seems more appropriate to use HTTPS instead of HTTP
        res @(http/get (str "http://" domain "/.well-known/webfinger")
                       {:query-params {:resource (str "acct:" account-id)}})]
    (if (= (:status res) 200)
      (json/read-str (:body res) :key-fn keyword)
      (throw (ex-info "failed to fetch remote account resource"
                      {:status (:status res)})))))

(defn link [resource rel]
  (->> (:links resource)
       (filter #(= (:rel %) rel))
       first))
