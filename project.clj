(defproject rmaze "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :repositories {"sonatype-oss-public"
                 "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.priority-map "0.0.2-SNAPSHOT"]
                 [ring/ring-jetty-adapter "1.0.1"]
                 [compojure "0.6.5"]
                 [enlive "1.0.0"]
                 [org.clojure/data.json "0.1.2"]]
  :plugins [[lein-ring "0.6.2"]]
  :ring {:handler rmaze.server/app})
