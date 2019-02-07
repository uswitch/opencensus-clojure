(def base-version "0.2")

(defproject uswitch/opencensus-clojure (str base-version (if-let [number (System/getenv "DRONE_BUILD_NUMBER")] (str "." number) "-SNAPSHOT"))
  :description "wraps opencensus-java"
  :url "https://github.com/uswitch/opencensus-java"
  :scm {:name "git"
        :url  "https://github.com/uswitch/opencensus-java"}
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-codox "0.10.3"]]
  :codox {:metadata {:doc/format :markdown}}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/tools.logging "0.4.0"]
                 [io.opencensus/opencensus-api "0.19.2"]
                 [io.opencensus/opencensus-impl "0.19.2" :scope "runtime"]
                 [io.opencensus/opencensus-exporter-trace-logging "0.19.2" :scope "provided"]
                 [io.opencensus/opencensus-exporter-trace-jaeger "0.19.2" :scope "provided"]
                 [io.opencensus/opencensus-exporter-trace-zipkin "0.19.2" :scope "provided"]]

  :repositories [["clojars" {:sign-releases false
                             :username      [:gpg :env/clojars_username]
                             :password      [:gpg :env/clojars_password]}]]
  :deploy-repositories [["clojars" {:sign-releases false
                                    :username      [:gpg :env/clojars_username]
                                    :password      [:gpg :env/clojars_password]}]])
