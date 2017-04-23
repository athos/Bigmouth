(ns bigmouth.utils)

(defn base-url [{:keys [use-https? local-domain]}]
  (str "http" (if use-https? "s" "") "://" local-domain))

(defn feed-url [account configs]
  (format "%s/users/%s.atom" (base-url configs) account))

(defn account-url [account configs]
  (format "%s/users/%s" (base-url configs) account))

(defn profile-url [account configs]
  (format "%s/@%s" (base-url configs) account))

(defn hub-url [configs]
  (format "%s/api/push" (base-url configs)))

(defn salmon-url [account-id configs]
  (format "%s/salmon/%s" (base-url configs) account-id))
