(ns opencensus-clojure.trace
  (:import (io.opencensus.trace Tracing AttributeValue)
           (io.opencensus.trace.samplers Samplers)))

(def tracer (Tracing/getTracer))

(def ^:dynamic current-span nil)

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
   `(let [span-builder# (-> tracer
                            (.spanBuilder ~span-name)
                            (.setRecordEvents true)
                            (.setSampler (Samplers/alwaysSample)))]
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
