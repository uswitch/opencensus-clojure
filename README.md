# opencensus-clojure

A Clojure library designed to pass butter.

## Usage

### Ring
There's a ringleware that you can use like this
```clojure
(ns another-web-app
  (:require [opencensus-clojure.ring.middleware :refer [wrap-tracing]]
            [clojure.string :as str]
            [compojure.api.sweet :refer :all]))

(defroutes handlers
  (GET "/foo" (ok "bar")))

; will assume all your requests are the same and use "ring-request" for the operation name
(-> handlers
  (wrap-tracing))

; will take a function to figure out the name of the operation from the request. For a ring app, it
; _probably_ makes sense to pass :uri, meaning the path would be the operation name
(-> handlers
  (wrap-tracing (fn [req] (-> req :uri (str/replace #"/" "🦄")))))

; The traces are probabilistic by default. This will force a sample, so you can actually see things in dev.
(-> handlers
  (wrap-tracing (constantly "i-dont-care") (debug-this-request?)))
```

It also adds a tag with the response status by default.

### Generic

The ringleware essentially just wraps the more generic macro, `span`, which takes an op name 
and an arbitrary form.

```clojure
(ns my-deep-layered-namespace
  (:require [opencensus-clojure.trace :refer [span add-tag]]))

(defn fetch-sticks
  "fetches sticks and is a good boi" 
  [stick-filters]
  (span "my-operation-name"
    (let [db-response (do-a-db-thing stick-filters)]
      (add-tag "did-the-db-thing-succeed" (:success db-response)))))
```
As in the example, you can also add tags anywhere inside a `span`. Because the ringleware wraps a request,
you can in theory do this anywhere within a request, but the tag will end up on whatever span is currently
being measured.

The nesting is handled by magic under the bonnet, so you **can** literally just wrap ring and then do these
`span`s in your DB layer, and get nice nested timings out of this.

### Reporting

There are two reporters available, the `logging` one,
```clojure
(opencensus-clojure.reporting.logging/report)
```

and the Jaeger one.
```clojure
(opencensus-clojure.reporting.jaeger/report "my-service-name")

; allows you to specify the Thrift HTTP endpoint manually. You almost certainly don't need this.
(opencensus-clojure.reporting.jaeger/report "http://localhost:14268/api/traces" "my-service-name")
```

The logging one is nice to figure out if your stuff is working in the first place if your Jaeger thing seems
to not be. Jaeger is, however, what we have on Kube and you can launch a batteries included version via
```shell
$ docker run --rm -i \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp \
  -p5778:5778 -p16686:16686 -p14268:14268 -p9411:9411 \
  jaegertracing/all-in-one:latest
```
It should come up with a UI on `localhost:16686`, and you should see all your traces there.

## Disclaimer

This relies on 
- [opencensus-java](https://www.javadoc.io/doc/io.opencensus/opencensus-api/0.12.3)
  - [github](https://github.com/census-instrumentation/opencensus-java)
- single-threadedness for the duration of a span. I.e., your usual Ring + Jetty should work just fine.
 If you're doing futures inside your requests and try doing spans in said futures, it will probably
 get funny. Have done no testing there, so that way be dragons.
 Async Ring and other green thread stuff will probably get funny.
 OpenCensus supports directly passing in parent spans etc., and we can extend it to be 
 async/threading-compatible if there's actual demand.
