(defproject bigmouth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.2"]
                 [ring/ring-core "1.5.1"]
                 [selmer "1.10.7"]]
  :profiles {:dev {:source-paths ["env"]
                   :dependencies
                   [[integrant "0.4.0"]
                    [org.clojure/tools.namespace "0.2.11"]
                    [ring/ring-jetty-adapter "1.5.1"]]}})
