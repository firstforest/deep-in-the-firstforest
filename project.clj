(defproject ditff "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.twitter4j/twitter4j-core "4.0.1"]
                 [seesaw "1.4.4"]
                 [clj-http "0.9.1"]]
  :main ^:skip-aot ditff.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
