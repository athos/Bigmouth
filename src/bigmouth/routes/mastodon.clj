(ns bigmouth.routes.mastodon
  (:require [bigmouth.atom :as atom]
            [bigmouth.models.account :as account]
            [bigmouth.routes.subscription :refer [subscribe unsubscribe]]
            [bigmouth.routes.salmon :as salmon]
            [compojure.core :refer :all]
            [ring.util.response :as res]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defn- user-feed [context username]
  (let [account (account/find-account (:accounts context) username)
        entry {:id "001", :message "hogehoge"}]
    (-> (res/response (atom/atom-feed account [entry] (:configs context)))
        (res/content-type "application/atom+xml; charset=utf-8"))))

(defn- subscription [context params]
  (if (= (get params "hub.mode") "subscribe")
    (subscribe context params)
    (unsubscribe context params)))

(defn- salmon [context account-id body]
  (if (empty? body)
    (res/status {} 200)
    (do (salmon/salmon context account-id body)
        (res/status {} 201))))

(defn make-mastodon-routes [context]
  (-> (routes
        (GET "/users/:username.atom" [username]
          (user-feed context username))
        (POST "/api/push" {:keys [params]}
          (subscription context params))
        (POST "/salmon/:id" {{:keys [id]} :params :keys [body]}
          (salmon context id (slurp body))))
      (wrap-keyword-params)
      (wrap-params)))
