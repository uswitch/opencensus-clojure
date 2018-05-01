(def base-version "0.1")

(defproject opencensus-clojure (str base-version (if-let[number (System/getenv "DRONE_BUILD_NUMBER")] (str "." number) "-SNAPSHOT"))
  :description "nope"
  :url "http://example.com/fridayfriday"
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [io.opencensus/opencensus-api "0.13.0"]
                 [io.opencensus/opencensus-exporter-trace-logging "0.13.0"]
                 [io.opencensus/opencensus-impl "0.13.0" :scope "runtime"]
                 [io.opencensus/opencensus-exporter-trace-jaeger "0.13.0"]]

  :repositories {"releases" {:url "http://internaljars.uswitchinternal.com:8080/nexus/content/repositories/releases"
                             :sign-releases false
                             :username "energy-platform-team"
                             :password "p6wP2Xu*lG*^bPo9" }
                 "snapshots" {:url "http://internaljars.uswitchinternal.com:8080/nexus/content/repositories/snapshots"
                              :sign-releases false
                              :username "energy-platform-team"
                              :password "p6wP2Xu*lG*^bPo9" }}
  :lein-release {:deploy-via "releases"})
