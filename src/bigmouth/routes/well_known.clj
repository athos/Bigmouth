(ns bigmouth.routes.well-known
  (:require [bigmouth.models.account :as account]
            [bigmouth.webfinger :as webfinger]
            [clojure.data.json :as json]
            [compojure.core :refer :all]
            [ring.util.response :as res]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [selmer.parser :as parser]))

(defn- host-meta [context]
  (let [{:keys [use-https? local-domain]} (:configs context)
        values {:use-https? use-https? :local-domain local-domain}]
    (-> (res/response (parser/render-file "host-meta" values))
        (res/content-type "application/xrd+xml; charset=utf-8"))))

(defn- webfinger [{:keys [accounts configs]} resource-uri]
  (let [[_ username] (re-find #"^acct:([^@]*?)@" resource-uri)]
    (when-let [account (account/find-account accounts username)]
      (let [resource (webfinger/account-resource account configs)]
        (-> (res/response (json/write-str resource))
            (res/content-type "application/jrd+json; charset=utf-8"))))))

(defn make-well-known-routes [context]
  (-> (routes
        (GET "/.well-known/host-meta" []
          (host-meta context))
        (GET "/.well-known/webfinger" [resource]
          (webfinger context resource)))
      (wrap-keyword-params)
      (wrap-params)))
