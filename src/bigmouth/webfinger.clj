(ns bigmouth.webfinger)

(defn account-resource [account {:keys [use-https? local-domain]}]
  (let [base-url (str "http" (if use-https? "s" "") "://" local-domain)
        profile-url (str base-url "/@" account)]
    {:subject (str "acct:" account "@" local-domain)
     :aliases [profile-url]
     :links [{:rel "http://webfinger.net/rel/profile-page"
              :type "text/html"
              :href profile-url}
             {:rel "http://schemas.google.com/g/2010#updates-from"
              :type "application/atom+xml"
              :href (str base-url "/users/" account ".atom")}
             {:rel "salmon"
              :href (str base-url "/api/salmon/FIXME")}
             {:rel "magic-public-key"
              :href "data:application/magic-public-key,RSA.rrOhHpSf_Sypkx14xjSQEgDXGxQvpdmOvyUWgUh8DsPYeACnDN0_qztYWHXcRf73lcNWIy9t3hBQ37lVdTyHZxOZ1X8ryunsWHVyJ6u-JEUTk7tDK_lxFz9HAsATH714T2IauawlJoNyucztP0nQxwOnsKWeNzvXKFjtVPuXNrtOapuqcPeAt8BzFzJt-0QBUQ1POPIQGeMVqdzG-wnxINF-yZvnRlgWfvujJwUDwRwseMukgGRrYDgbuye1bYJAlK6dlrZtHcd8IwC7uguk11gVpyacYHnAc9FTmq1C2T2-JeZwK2yehDuZjGqxy1AktqMqkKlTnOGbQ5WA_ErXhQ==.AQAB"}
             {:rel "http://ostatus.org/schema/1.0/subscribe"
              :template (str base-url "/authorize_follow?acct={uri}")}]}))
