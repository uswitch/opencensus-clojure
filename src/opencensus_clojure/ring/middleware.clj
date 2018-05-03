(ns opencensus-clojure.ring.middleware
  (:require [opencensus-clojure.trace :refer [span add-tag]]
            [clojure.tools.logging :as logging]
            [opencensus-clojure.propagation :refer [ring-b3-getter b3-setter]])
  (:import (io.opencensus.trace Tracing)))

(defn extract-remote-span [{:keys [headers] :as request}]
  (when (get headers "x-b3-spanid")
    (logging/debug "found x-b3-spanid, extracting remote context")
    (let [b3-format (-> (Tracing/getPropagationComponent) (.getB3Format))]
      (.extract b3-format request ring-b3-getter))))

(defn wrap-tracing
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
