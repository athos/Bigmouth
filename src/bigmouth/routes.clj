(ns bigmouth.routes
  (:require [compojure.core :refer :all]
            [ring.util.response :as res]
            [selmer.parser :as parser]))

(defn host-meta [{:keys [use-https? local-domain]}]
  (let [context {:use-https? use-https? :local-domain local-domain}]
    (-> (res/response (parser/render-file "host-meta" context))
        (res/content-type "application/xrd+xml; charset=utf-8"))))

(defn make-well-known-routes [configs]
  (routes
    (GET "/.well-known/host-meta" []
      (host-meta configs))))
