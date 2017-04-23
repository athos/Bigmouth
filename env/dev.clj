(ns dev
  (:require [bigmouth.routes :as bigmouth]
            [bigmouth.protocols :as proto]
            [clojure.pprint :refer [pp pprint]]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [org.httpkit.server :as server]
            [integrant.core :as ig]))

(def config
  {:configs/bigmouth {:use-https? false :local-domain "c2d7f0d9.ngrok.io"}
   :repository/subscription {}
   :handler/bigmouth {:configs (ig/ref :configs/bigmouth)
                      :subscription-repo (ig/ref :repository/subscription)}
   :adapter/http-kit {:port 8080 :handler (ig/ref :handler/bigmouth)}})

(defmethod ig/init-key :configs/bigmouth [_ configs]
  configs)

(defmethod ig/init-key :repository/subscription [_ _]
  (let [subscriptions (atom {})]
    (reify proto/SubscriptionRepository
      (subscribe! [this account callback]
        (swap! subscriptions update account (fnil conj #{}) callback))
      (unsubscribe! [this account callback]
        (swap! subscriptions update account (fnil disj #{}) callback))
      (find-subscriptions [this account]
        (get @subscriptions account))
      clojure.lang.IFn
      (invoke [this] @subscriptions))))

(defmethod ig/init-key :handler/bigmouth [_ opts]
  (bigmouth/make-bigmouth-routes (:subscription-repo opts)
                                 (:configs opts)))

(defmethod ig/init-key :adapter/http-kit [_ {:keys [handler] :as opts}]
  (server/run-server handler (dissoc opts :handler)))

(defmethod ig/halt-key! :adapter/http-kit [_ server]
  (server))

(def system nil)

(defn go []
  (alter-var-root #'system (constantly (ig/init config))))

(defn stop []
  (when system
    (ig/halt! system)
    (alter-var-root #'system (constantly nil))))

(defn reset []
  (stop)
  (refresh :after 'dev/go))
