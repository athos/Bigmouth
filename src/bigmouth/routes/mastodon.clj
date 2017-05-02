(ns bigmouth.routes.mastodon
  (:require [bigmouth.atom :as atom]
            [bigmouth.models.account :as account]
            [bigmouth.routes.subscription :refer [subscribe unsubscribe]]
            [compojure.core :refer :all]
            [ring.util.response :as res]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defn- user-feed [account-repo username configs]
  (let [account (account/find-account account-repo username)
        entry {:id "001", :message "hogehoge"}]
    (-> (res/response (atom/atom-feed account [entry] configs))
        (res/content-type "application/atom+xml; charset=utf-8"))))

(defn- subscribe [subscription-repo params configs]
  (if (= (get params "hub.mode") "subscribe")
    (subscribe subscription-repo params configs)
    (unsubscribe subscription-repo params configs)))

(defn make-mastodon-routes [account-repo subscription-repo configs]
  (-> (routes
        (GET "/users/:username.atom" [username]
          (user-feed account-repo username configs))
        (POST "/api/push" {:keys [params]}
          (subscribe subscription-repo params configs)))
      (wrap-keyword-params)
      (wrap-params)))
