## v0.2.x

- change the `span` interface
 - the `debug` option is gone. Use `opencensus-clojure.trace/configure-tracer` and
 set `p=1.0` to guarantee sampling
- span now takes an optional remote parent `SpanContext`
- Ring middleware automatically extracts this using the B3 format used by istio etc.
- added function to generate headers for downstream requests in B3 format
