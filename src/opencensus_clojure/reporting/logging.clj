(ns opencensus-clojure.reporting.logging
  (:import (io.opencensus.exporter.trace.logging LoggingTraceExporter)))


(defn report []
  (LoggingTraceExporter/register))
