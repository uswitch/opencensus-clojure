(ns opencensus-clojure.propagation
  (:refer-clojure :rename {get core-get
                           set core-set})
  (:import (io.opencensus.trace.propagation TextFormat$Getter TextFormat$Setter)))

(def ring-b3-getter
  (proxy [TextFormat$Getter] []
    (get [request key]
      (let [headers (:headers request)]
        (core-get headers key)))))

(def b3-setter
  (proxy [TextFormat$Setter] []
    (put [transient-hash key value]
      (assoc! transient-hash key value))))
