(ns opencensus-clojure.ring.middleware
  (:require [opencensus-clojure.trace :refer [span add-tag]]
            [clojure.tools.logging :as logging]
            [opencensus-clojure.propagation :refer [ring-b3-getter b3-setter]])
  (:import (io.opencensus.trace Tracing)
           (io.opencensus.trace.propagation TextFormat)))

(defn- extract-remote-span [{:keys [headers] :as request}]
  (when (get headers "x-b3-traceid")
    (logging/debug "found x-b3-spanid, extracting remote context")
    (let [^TextFormat b3-format (-> (Tracing/getPropagationComponent) (.getB3Format))]
      (.extract b3-format request ring-b3-getter))))

(defn wrap-tracing
  "Ring middleware that wraps span tracing around a ring handler.

    - `:handler` the ring handler
    - `:name-foo` a 1-arg function that takes the request and returns a string, used to figure out the operation name for
    the span. A common example might be `:uri`, if your paths don't have variables in them."
  ([handler]
   (fn [req]
     (span "ring-request"
           (let [response (handler req)]
             (add-tag "http_status" (:status response))
             response)
           (extract-remote-span req))))

  ([handler name-foo]
   (fn [req]
     (span (name-foo req)
           (let [response (handler req)]
             (add-tag "http_status" (:status response))
             response)
           (extract-remote-span req)))))
