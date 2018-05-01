(def base-version "0.1")

(defproject uswitch/opencensus-clojure (str base-version (if-let[number (System/getenv "DRONE_BUILD_NUMBER")] (str "." number) "-SNAPSHOT")))
  :description "wraps opencensus-java"
  :url "https://github.com/uswitch/opencensus-java"
  :scm  {:name "git"
         :url "https://github.com/uswitch/opencensus-java"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [io.opencensus/opencensus-api "0.13.0"]
                 [io.opencensus/opencensus-exporter-trace-logging "0.13.0"]
                 [io.opencensus/opencensus-impl "0.13.0" :scope "runtime"]
                 [io.opencensus/opencensus-exporter-trace-jaeger "0.13.0"]]

  :deploy-repositories [["clojars" {:sign-releases false
                                    :username [:gpg :env/clojars_username]
                                    :password [:gpg :env/clojars_password]}]])
