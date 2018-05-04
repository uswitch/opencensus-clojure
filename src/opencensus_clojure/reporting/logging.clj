(ns opencensus-clojure.reporting.logging
  (:require [clojure.tools.logging :as log])
  (:import (io.opencensus.exporter.trace.logging LoggingTraceExporter)))


(defn report []
  (do
    (log/info "starting logging reporter")
    (LoggingTraceExporter/register)))
