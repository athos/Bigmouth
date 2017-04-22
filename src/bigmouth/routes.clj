(ns bigmouth.routes
  (:require [clojure.data.json :as json]
            [compojure.core :refer :all]
            [ring.util.response :as res]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [selmer.parser :as parser]))

(defn host-meta [{:keys [use-https? local-domain]}]
  (let [context {:use-https? use-https? :local-domain local-domain}]
    (-> (res/response (parser/render-file "host-meta" context))
        (res/content-type "application/xrd+xml; charset=utf-8"))))

(defn webfinger [resource {:keys [use-https? local-domain]}]
  (let [[_ user] (re-find #"^acct:([^@]*?)@" resource)
        base-url (str "http" (if use-https? "s" "") "://" local-domain)
        profile-url (str base-url "/@" user)
        user-info {:subject (str "acct:" user "@" local-domain)
                   :aliases [profile-url]
                   :links [{:rel "http://webfinger.net/rel/profile-page"
                            :type "text/html"
                            :href profile-url}
                           {:rel "http://schemas.google.com/g/2010#updates-from"
                            :type "application/atom+xml"
                            :href (str base-url "/users/" user ".atom")}
                           {:rel "salmon"
                            :href (str base-url "/api/salmon/FIXME")}
                           {:rel "magic-public-key"
                            :href "data:application/magic-public-key,RSA.rrOhHpSf_Sypkx14xjSQEgDXGxQvpdmOvyUWgUh8DsPYeACnDN0_qztYWHXcRf73lcNWIy9t3hBQ37lVdTyHZxOZ1X8ryunsWHVyJ6u-JEUTk7tDK_lxFz9HAsATH714T2IauawlJoNyucztP0nQxwOnsKWeNzvXKFjtVPuXNrtOapuqcPeAt8BzFzJt-0QBUQ1POPIQGeMVqdzG-wnxINF-yZvnRlgWfvujJwUDwRwseMukgGRrYDgbuye1bYJAlK6dlrZtHcd8IwC7uguk11gVpyacYHnAc9FTmq1C2T2-JeZwK2yehDuZjGqxy1AktqMqkKlTnOGbQ5WA_ErXhQ==.AQAB"}
                           {:rel "http://ostatus.org/schema/1.0/subscribe"
                            :template (str base-url "/authorize_follow?acct={uri}")}]}]
    (-> (res/response (json/write-str user-info))
        (res/content-type "application/jrd+json; charset=utf-8"))))

(defn make-well-known-routes [configs]
  (-> (routes
        (GET "/.well-known/host-meta" []
          (host-meta configs))
        (GET "/.well-known/webfinger" [resource]
          (webfinger resource configs)))
      (wrap-keyword-params)
      (wrap-params)))
