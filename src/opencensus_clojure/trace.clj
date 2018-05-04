(ns opencensus-clojure.trace
  (:require [opencensus-clojure.propagation :refer [b3-setter]]
            [clojure.tools.logging :as log])
  (:import (io.opencensus.trace Tracing AttributeValue)
           (io.opencensus.trace.samplers Samplers)))

(def tracer (Tracing/getTracer))

(def ^:dynamic current-span nil)

(defn configure-tracer [{:keys [probability max-annotations max-attributes max-links max-message-events]}]
  (let [trace-config (Tracing/getTraceConfig)
        new-params-builder (-> trace-config
                               (.getActiveTraceParams)
                               (.toBuilder))]
    (do
      (when (some? probability)
        (.setSampler new-params-builder (Samplers/probabilitySampler probability)))
      (when (some? max-annotations)
        (.setMaxNumberOfAnnotations new-params-builder max-annotations))
      (when (some? max-attributes)
        (.setMaxNumberOfAttributes new-params-builder max-attributes))
      (when (some? max-links)
        (.setMaxNumberOfLinks new-params-builder max-links))
      (when (some? max-message-events)
        (.setMaxNumberOfMessageEvents new-params-builder max-message-events))
      (.updateActiveTraceParams trace-config (.build new-params-builder)))))

(defn value->AttributeValue
  [v]
  (cond
    (int? v) (AttributeValue/longAttributeValue v)
    (boolean? v) (AttributeValue/booleanAttributeValue v)
    :else (AttributeValue/stringAttributeValue (str v))))

(defn add-tag
  [k v]
  (.putAttribute
    current-span
    k
    (value->AttributeValue v)))

(defn add-tags
  [tags]
  (.putAttributes
    current-span
    (->> tags
         (map (fn [[k v]] [k (value->AttributeValue v)]))
         (into {}))))

(defn make-downstream-headers
  []
  (let [b3-format (-> (Tracing/getPropagationComponent) (.getB3Format))
        builder (transient {})]
    (.inject b3-format (.getContext current-span) builder b3-setter)
    (persistent! builder)))

(defmacro span
  ([span-name code]
   `(let [span-builder# (.spanBuilder tracer ~span-name)]
      (log/debug "building span " ~span-name)
      (with-open [scope# (.startScopedSpan span-builder#)]
        (binding [current-span (.getCurrentSpan tracer)]
          ~code))))

  ([span-name code remote]
   `(let [span-builder# (.spanBuilderWithRemoteParent tracer ~span-name ~remote)]
      (log/debug "building span " ~span-name " with remote parent")
      (with-open [scope# (.startScopedSpan span-builder#)]
        (binding [current-span (.getCurrentSpan tracer)]
          ~code)))))
