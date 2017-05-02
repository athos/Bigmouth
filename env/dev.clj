(ns dev
  (:require [bigmouth.routes :as bigmouth]
            [bigmouth.models.subscription :as subs]
            [bigmouth.models.account :as account]
            [clojure.pprint :refer [pp pprint]]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [org.httpkit.server :as server]
            [integrant.core :as ig]))

(def config
  {:configs/bigmouth {:use-https? false :local-domain "localhost:8080"}
   :repository/subscription {}
   :repository/account {}
   :handler/bigmouth {:configs (ig/ref :configs/bigmouth)
                      :accounts (ig/ref :repository/account)
                      :subscriptions (ig/ref :repository/subscription)}
   :adapter/http-kit {:port 8080 :handler (ig/ref :handler/bigmouth)}})

(defmethod ig/init-key :configs/bigmouth [_ configs]
  configs)

(defmethod ig/init-key :repository/account [_ _]
  (account/simple-in-memory-account-repository))

(defmethod ig/init-key :repository/subscription [_ _]
  (subs/simple-in-memory-subscription-repository))

(defmethod ig/init-key :handler/bigmouth [_ context]
  (bigmouth/make-bigmouth-routes context))

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
