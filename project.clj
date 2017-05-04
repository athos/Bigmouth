(defproject bigmouth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.github.kyleburton/clj-xpath "1.4.11"]
                 [compojure "1.5.2"]
                 [http-kit "2.2.0"]
                 [org.clojure/data.json "0.2.6"]
                 [pandect "0.6.1"]
                 [ring/ring-core "1.5.1"]
                 [selmer "1.10.7"]]
  :profiles {:dev {:source-paths ["env"]
                   :dependencies
                   [[integrant "0.4.0"]
                    [org.clojure/tools.namespace "0.2.11"]]}})
