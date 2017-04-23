(ns bigmouth.webfinger
  (:require [bigmouth.utils :as utils]))

(defn account-resource [account {:keys [local-domain] :as configs}]
  (let [profile-url (utils/profile-url account configs)]
    {:subject (str "acct:" account "@" local-domain)
     :aliases [profile-url]
     :links [{:rel "http://webfinger.net/rel/profile-page"
              :type "text/html"
              :href profile-url}
             {:rel "http://schemas.google.com/g/2010#updates-from"
              :type "application/atom+xml"
              :href (utils/feed-url account configs)}
             {:rel "salmon"
              :href (utils/salmon-url "FIXME" configs)}
             {:rel "magic-public-key"
              :href "data:application/magic-public-key,RSA.rrOhHpSf_Sypkx14xjSQEgDXGxQvpdmOvyUWgUh8DsPYeACnDN0_qztYWHXcRf73lcNWIy9t3hBQ37lVdTyHZxOZ1X8ryunsWHVyJ6u-JEUTk7tDK_lxFz9HAsATH714T2IauawlJoNyucztP0nQxwOnsKWeNzvXKFjtVPuXNrtOapuqcPeAt8BzFzJt-0QBUQ1POPIQGeMVqdzG-wnxINF-yZvnRlgWfvujJwUDwRwseMukgGRrYDgbuye1bYJAlK6dlrZtHcd8IwC7uguk11gVpyacYHnAc9FTmq1C2T2-JeZwK2yehDuZjGqxy1AktqMqkKlTnOGbQ5WA_ErXhQ==.AQAB"}
             {:rel "http://ostatus.org/schema/1.0/subscribe"
              :template (str (utils/base-url configs)
                             "/authorize_follow?acct={uri}")}]}))
