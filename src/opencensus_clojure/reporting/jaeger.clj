(ns opencensus-clojure.reporting.jaeger
  (:require [clojure.tools.logging :as log])
  (:import (io.opencensus.exporter.trace.jaeger JaegerTraceExporter)))

(defn report
  "Reports the spans to Jaeger.

    - `:endpoint` optional endpoint to report to
    - `:service-name` the name of your app. This is global and forever."
  ([^String service-name] (report "http://localhost:14268/api/traces" service-name))
  ([^String endpoint ^String service-name]
   (do
     (log/info "starting Jaeger reporter")
     (JaegerTraceExporter/createAndRegister endpoint service-name))))
