(ns dev
  (:require [bigmouth.core :as bigmouth]
            [bigmouth.interaction :as interaction]
            [bigmouth.models.account :as account]
            [bigmouth.models.keystore :as keystore]
            [bigmouth.models.subscription :as subs]
            [clojure.pprint :refer [pp pprint]]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [org.httpkit.server :as server]
            [integrant.core :as ig]))

(def config
  {:configs/bigmouth {:use-https? false :local-domain "localhost:8080"}
   :repository/account {}
   :repository/keystore {}
   :repository/subscription {}
   :handler/interaction {}
   :app/bigmouth {:configs (ig/ref :configs/bigmouth)
                  :accounts (ig/ref :repository/account)
                  :keystore (ig/ref :repository/keystore)
                  :subscriptions (ig/ref :repository/subscription)
                  :interaction-handler (ig/ref :handler/interaction)}
   :adapter/http-kit {:port 8080 :app (ig/ref :app/bigmouth)}})

(defmethod ig/init-key :configs/bigmouth [_ configs]
  configs)

(defmethod ig/init-key :repository/account [_ _]
  (account/simple-in-memory-account-repository))

(defmethod ig/init-key :repository/keystore [_ _]
  (keystore/simple-in-memory-keystore))

(defmethod ig/init-key :repository/subscription [_ _]
  (subs/simple-in-memory-subscription-repository))

(defmethod ig/init-key :handler/interaction [_ _]
  (reify interaction/InteractionHandler
    (follow [this account target]
      (println account "just followed" target))
    (unfollow [this account target]
      (println account "just unfollowed" target))))

(defmethod ig/init-key :app/bigmouth [_ context]
  (bigmouth/bigmouth context))

(defmethod ig/init-key :adapter/http-kit [_ {:keys [app] :as opts}]
  (server/run-server (:handler app) (dissoc opts :app)))

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
