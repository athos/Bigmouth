(ns bigmouth.utils)

(defn base-url [{:keys [use-https? local-domain]}]
  (str "http" (if use-https? "s" "") "://" local-domain))

(defn hub-url [configs]
  (format "%s/api/push" (base-url configs)))
