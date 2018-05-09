(ns ^:no-doc opencensus-clojure.propagation
  "Contains implementations to [de]serialize `SpanContext`s to / from B3 headers. These are almost certainly not the
  droids you're looking for."
  (:refer-clojure :rename {get core-get
                           set core-set})
  (:require [clojure.string :as str])
  (:import (io.opencensus.trace.propagation TextFormat$Getter TextFormat$Setter)))

(def ring-b3-getter
  (proxy [TextFormat$Getter] []
    (get [request key]
      (let [headers (:headers request)]
        (core-get headers (str/lower-case key))))))

(def b3-setter
  (proxy [TextFormat$Setter] []
    (put [transient-hash key value]
      (assoc! transient-hash (str/lower-case key) value))))
