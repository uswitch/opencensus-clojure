(ns opencensus-clojure.reporting.logging
  (:require [clojure.tools.logging :as log])
  (:import (io.opencensus.exporter.trace.logging LoggingTraceExporter)))


(defn report
  "Reports your spans via logging. Useful for debugging, to see that your spans actually work."
  []
  (do
    (log/info "starting logging reporter")
    (LoggingTraceExporter/register)))

(defn shutdown []
  "Unregisters the trace exporter"
  (LoggingTraceExporter/unregister))
