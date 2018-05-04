(ns opencensus-clojure.reporting.jaeger
  (:require [clojure.tools.logging :as log])
  (:import (io.opencensus.exporter.trace.jaeger JaegerTraceExporter)))

(defn report
  ([service-name] (report "http://localhost:14268/api/traces" service-name))
  ([endpoint service-name] (do
                             (log/info "starting Jaeger reporter")
                             (JaegerTraceExporter/createAndRegister endpoint service-name))))
