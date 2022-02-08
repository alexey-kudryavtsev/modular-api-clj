(defproject com.gitlab.alexey-kudryavtsev.modular-api-clj/server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service "0.5.7"]
                 [io.pedestal/pedestal.route "0.5.7"]
                 [io.pedestal/pedestal.jetty "0.5.7"]
                 [org.reflections/reflections "0.10.2"]]
  :main server.core
  :target-path "target/%s"
  :profiles {:uberjar    {:aot :all}
             :demo-local {:resource-paths ["../plugin_a/target/uberjar/plugin_a-0.1.0-SNAPSHOT.jar"
                                           "../plugin_b/target/uberjar/plugin_b-0.1.0-SNAPSHOT.jar"]}})
