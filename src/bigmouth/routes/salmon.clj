(ns bigmouth.routes.salmon
  (:require [bigmouth.interaction :as interaction]
            [bigmouth.models.account :as account]
            [bigmouth.models.keystore :as keystore]
            [bigmouth.salmon :as salmon]
            [bigmouth.webfinger :as webfinger]
            [clj-xpath.core :as xpath]
            [clojure.string :as str])
  (:import [java.net URL]))

(def ATOM_NS "http://www.w3.org/2005/Atom")
(def ACTIVITY_STREAM_NS "http://activitystrea.ms/spec/1.0/")

(def URL->VERB
  (zipmap (vals salmon/VERBS) (keys salmon/VERBS)))

(defn- with-ns [f]
  (xpath/with-namespace-context {"" ATOM_NS
                                 "activity" ACTIVITY_STREAM_NS}
    (f)))

(defn- account-from-xml [xml]
  (with-ns
    (fn []
      (let [account-id (xpath/$x:text "./:author/:name" xml)
            uri (xpath/$x:text "./:author/:uri" xml)]
        (when (and account-id uri)
          (str account-id "@" (.getAuthority (URL. uri))))))))

(defn- ensure-public-key! [keystore account-id]
  (or (keystore/find-public-key keystore account-id)
      (let [res (webfinger/fetch-remote-account-resource account-id)
            [_ magic-key] (-> (webfinger/link res "magic-public-key")
                              :href
                              (str/split #","))
            public-key (account/magic-key->public-key magic-key)]
        (keystore/save-key! keystore account-id public-key)
        public-key)))

(defn- verb [xml]
  (try
    (with-ns #(URL->VERB (xpath/$x:text "./activity:verb" xml)))
    (catch Exception _
      :post)))

(defn salmon [context target-account envelop]
  (let [body (salmon/unpack envelop)
        xml (with-ns #(xpath/$x:node "/:entry" (xpath/xml->doc body)))
        account-id (account-from-xml xml)
        public-key (ensure-public-key! (:keystore context) account-id)]
    (when (salmon/verify envelop public-key)
      (let [handler (:interaction-handler context)
            target-id (account/->username target-account)
            verb (verb xml)]
        (case verb
          :post (interaction/post handler account-id)
          :share (interaction/share handler account-id)
          :delete (interaction/delete handler account-id)
          :follow (interaction/follow handler account-id target-id)
          :unfollow (interaction/unfollow handler account-id target-id)
          :request_friend (interaction/request-friend handler account-id target-id)
          :authorize (interaction/authorize handler account-id target-id)
          :reject (interaction/reject handler account-id target-id)
          :favorite (interaction/favorite handler account-id)
          :unfavorite (interaction/unfavorite handler account-id)
          :block (interaction/block handler account-id target-id)
          :unblock (interaction/unblock handler account-id target-id)
          nil)))))
