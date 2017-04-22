(ns dev
  (:require [bigmouth.routes :as bigmouth]
            [clojure.tools.namespace.repl :refer [refresh]]
            [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]))

(def config
  {:handler/bigmouth {:use-https? false :local-domain "example.com"}
   :adapter/jetty {:port 8080 :handler (ig/ref :handler/bigmouth)}})

(defmethod ig/init-key :handler/bigmouth [_ configs]
  (bigmouth/make-well-known-routes configs))

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (let [opts (-> opts (dissoc :handler) (assoc :join? false))]
    (jetty/run-jetty handler opts)))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (.stop server))

(def system nil)

(defn go []
  (alter-var-root #'system (constantly (ig/init config))))

(defn stop []
  (ig/halt! system)
  (alter-var-root #'system (constantly nil)))

(defn reset []
  (stop)
  (refresh :after 'dev/go))
