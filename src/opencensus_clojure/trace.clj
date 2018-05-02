(ns opencensus-clojure.trace
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

(defn add-tag
  [k v]
  (.putAttribute
    current-span
    k
    (cond
      (int? v) (AttributeValue/longAttributeValue v)
      (boolean? v) (AttributeValue/booleanAttributeValue v)
      :else (AttributeValue/stringAttributeValue (str v)))))

(defmacro span
  ([span-name code]
   `(let [span-builder# (.spanBuilder tracer ~span-name)]
      (with-open [scope# (.startScopedSpan span-builder#)]
        (binding [current-span (.getCurrentSpan tracer)]
          ~code))))

  ([span-name code debug?]
   `(let [span-builder# (.spanBuilder tracer ~span-name)
          scope-builder# (if ~debug?
                           (-> span-builder#
                               (.setRecordEvents true)
                               (.setSampler (Samplers/alwaysSample)))
                           span-builder#)]
      (with-open [scope# (.startScopedSpan scope-builder#)]
        (binding [current-span (.getCurrentSpan tracer)]
          ~code)))))
