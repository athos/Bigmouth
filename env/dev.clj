(ns dev
  (:require [bigmouth.routes :as bigmouth]
            [clojure.tools.namespace.repl :refer [refresh]]
            [org.httpkit.server :as server]
            [integrant.core :as ig]))

(def config
  {:configs/bigmouth {:use-https? false :local-domain "c2d7f0d9.ngrok.io"}
   :handler/bigmouth {:configs (ig/ref :configs/bigmouth)}
   :adapter/http-kit {:port 8080 :handler (ig/ref :handler/bigmouth)}})

(defmethod ig/init-key :configs/bigmouth [_ configs]
  configs)

(defmethod ig/init-key :handler/bigmouth [_ {:keys [configs]}]
  (bigmouth/make-bigmouth-routes configs))

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
