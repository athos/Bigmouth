(ns bigmouth.routes.well-known
  (:require [bigmouth.models.account :as account]
            [bigmouth.webfinger :as webfinger]
            [clojure.data.json :as json]
            [compojure.core :refer :all]
            [ring.util.response :as res]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [selmer.parser :as parser]))

(defn- host-meta [{:keys [use-https? local-domain]}]
  (let [context {:use-https? use-https? :local-domain local-domain}]
    (-> (res/response (parser/render-file "host-meta" context))
        (res/content-type "application/xrd+xml; charset=utf-8"))))

(defn- webfinger [account-repo resource-uri configs]
  (let [[_ username] (re-find #"^acct:([^@]*?)@" resource-uri)
        account (account/find-account account-repo username)
        account-resource (webfinger/account-resource account configs)]
    (-> (res/response (json/write-str account-resource))
        (res/content-type "application/jrd+json; charset=utf-8"))))

(defn make-well-known-routes [account-repo configs]
  (-> (routes
        (GET "/.well-known/host-meta" []
          (host-meta configs))
        (GET "/.well-known/webfinger" [resource]
          (webfinger account-repo resource configs)))
      (wrap-keyword-params)
      (wrap-params)))
