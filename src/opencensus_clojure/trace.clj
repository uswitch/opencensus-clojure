(ns opencensus-clojure.trace
  (:require [opencensus-clojure.propagation :refer [b3-setter]]
            [clojure.tools.logging :as log])
  (:import (io.opencensus.trace Tracing AttributeValue)
           (io.opencensus.trace.samplers Samplers)))

; these have to be "public" because the macros use them. They're not actually "public".
(def ^:no-doc tracer
  "NOTE public because the macros use it; not to actually be consumed by clients"
  (Tracing/getTracer))

(def ^{:dynamic true :no-doc true} current-span
  "NOTE public because the macros use it; not to actually be consumed by clients"
  nil)

(defn configure-tracer
  "Configure the underlying tracer. Takes

    - `:probability`
    - `:max-annotations`
    - `:max-attributes`
    - `:max-links`
    - `:max-message-events`

  all of which are proxied through to the [TraceParams](https://static.javadoc.io/io.opencensus/opencensus-api/0.13.0/io/opencensus/trace/config/TraceParams.html) class."
  [{:keys [probability max-annotations max-attributes max-links max-message-events]}]
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

(defn- value->AttributeValue
  [v]
  (cond
    (int? v) (AttributeValue/longAttributeValue v)
    (boolean? v) (AttributeValue/booleanAttributeValue v)
    :else (AttributeValue/stringAttributeValue (str v))))

(defn add-tag
  "Adds a tag on the current span

    - `:k` tag name
    - `:v` tag value. Natively supported types are long, bool or string"
  [k v]
  (.putAttribute
    current-span
    k
    (value->AttributeValue v)))

(defn add-tags
  "See [[add-tag]]`.

    - `:tags` a hash of key-value pairs you'd pass to `add-tag`."
  [tags]
  (.putAttributes
    current-span
    (->> tags
         (map (fn [[k v]] [k (value->AttributeValue v)]))
         (into {}))))

(defn make-downstream-headers
  "Produces B3 style headers for the current span, to be passed down into further RPCs for distributed tracing."
  []
  (let [b3-format (-> (Tracing/getPropagationComponent) (.getB3Format))
        builder (transient {})]
    (.inject b3-format (.getContext current-span) builder b3-setter)
    (persistent! builder)))

(defmacro span
  "Creates a traced span. Takes a name and a form to be traced, or, additionally, a remote `SpanContext` that is a
  remote parent of the one you're starting.

  It is expected that clients should mostly use the 2-arg form and the 3-ard form is called from the Ring middleware
  handling the deserialization of the context from B3 headers, however, if you're not using Ring, this might be useful."
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
