(ns bigmouth.routes
  (:require [bigmouth.webfinger :as webfinger]
            [clojure.data.json :as json]
            [compojure.core :refer :all]
            [ring.util.response :as res]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [selmer.parser :as parser]))

(defn host-meta [{:keys [use-https? local-domain]}]
  (let [context {:use-https? use-https? :local-domain local-domain}]
    (-> (res/response (parser/render-file "host-meta" context))
        (res/content-type "application/xrd+xml; charset=utf-8"))))

(defn webfinger [resource-uri configs]
  (let [[_ account] (re-find #"^acct:([^@]*?)@" resource-uri)
        account-resource (webfinger/account-resource account configs)]
    (-> (res/response (json/write-str account-resource))
        (res/content-type "application/jrd+json; charset=utf-8"))))

(defn user-feed [account {:keys [use-https? local-domain]}]
  (let [base-url (str "http" (if use-https? "s" "") "://" local-domain)
        context {:username account
                 :email (format "%s@%s" account local-domain)
                 :feed-url (format "%s/users/%s.atom" base-url account)
                 :account-uri (format "%s/users/%s" base-url account)
                 :profile-url (format "%s/@%s" base-url account)
                 :hub-url (format "%s/api/push" base-url)
                 :salmon-url (format "%s/salmon/FIXME" base-url)}]
    (-> (res/response (parser/render-file "atom.xml" context))
        (res/content-type "application/atom+xml; charset=utf-8"))))

(defn make-well-known-routes [configs]
  (-> (routes
        (GET "/.well-known/host-meta" []
          (host-meta configs))
        (GET "/.well-known/webfinger" [resource]
          (webfinger resource configs)))
      (wrap-keyword-params)
      (wrap-params)))

(defn make-mastodon-routes [configs]
  (routes
    (GET "/users/:account.atom" [account]
      (user-feed account configs))))

(defn make-bigmouth-routes [configs]
  (routes
    (make-well-known-routes configs)
    (make-mastodon-routes configs)))
