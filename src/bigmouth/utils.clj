(ns bigmouth.utils
  (:import [java.util Base64]))

(set! *warn-on-reflection* true)

(defn base-url [{:keys [use-https? local-domain]}]
  (str "http" (if use-https? "s" "") "://" local-domain))

(defn hub-url [configs]
  (format "%s/api/push" (base-url configs)))

(defn ^String base64-encode [datum]
  (let [^"[B" bytes (if (string? datum)
                      (.getBytes ^String datum)
                      datum)
        encoder (Base64/getUrlEncoder)]
    (.encodeToString encoder bytes)))

(defn ^"[B" base64-decode [^String base64]
  (let [decoder (Base64/getUrlDecoder)]
    (.decode decoder base64)))
