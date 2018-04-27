(ns opencensus-clojure.reporting.jaeger
  (:import (io.opencensus.exporter.trace.jaeger JaegerTraceExporter)))

(defn report
  ([service-name] (report "http://localhost:14268/api/traces" service-name))
  ([endpoint service-name] (JaegerTraceExporter/createAndRegister endpoint service-name)))
