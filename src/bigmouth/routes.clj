(ns bigmouth.routes
  (:require [bigmouth.routes.well-known :refer [make-well-known-routes]]
            [bigmouth.routes.mastodon :refer [make-mastodon-routes]]
            [clojure.string :as str]
            [compojure.core :refer :all]))

(defn- if-matches [path route]
  (fn [{:keys [uri] :as req}]
    (when (str/starts-with? uri path)
      (route req))))

(defn make-bigmouth-routes [account-repo subscription-repo configs]
  (let [well-known-routes (make-well-known-routes account-repo configs)
        mastodon-routes (make-mastodon-routes account-repo
                                              subscription-repo
                                              configs)]
    (routes
      (if-matches "/.well-known"
        well-known-routes)
      mastodon-routes)))
