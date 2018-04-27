(ns opencensus-clojure.ring.middleware
  (:require [opencensus-clojure.trace :refer [span add-tag]]))



(defn wrap-tracing
  ([handler] (wrap-tracing handler (constantly "ring-request") false))
  ([handler name-foo] (wrap-tracing handler name-foo false))
  ([handler name-foo debug?]
   (fn [req]
     (span (name-foo req)
           (let [response (handler req)]
             (add-tag "http_status" (:status response))
             response)
           debug?))))
