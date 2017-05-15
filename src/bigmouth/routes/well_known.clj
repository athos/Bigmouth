(ns bigmouth.routes.well-known
  (:require [bigmouth.models.account :as account]
            [bigmouth.webfinger :as webfinger]
            [bigmouth.specs :as specs]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]
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

(defn- webfinger [context resource-uri]
  (let [[_ username] (re-find #"^acct:([^@]*?)@" resource-uri)]
    (when-let [account (account/find-account (:accounts context) username)]
      (let [resource (webfinger/account-resource context account)]
        (-> (res/response (json/write-str resource))
            (res/content-type "application/jrd+json; charset=utf-8"))))))

(s/fdef make-well-known-routes
  :args (s/cat :context ::specs/context))

(defn make-well-known-routes [context]
  (-> (routes
        (GET "/.well-known/host-meta" []
          (host-meta context))
        (GET "/.well-known/webfinger" [resource]
          (webfinger context resource)))
      (wrap-keyword-params)
      (wrap-params)))
