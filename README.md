# opencensus-clojure

A Clojure library designed to pass butter.

****
**NOTE**: this is __**alpha**__ software under active development; here be dragons.
****

## Usage

## Dependency

Add the latest 

[![Clojars Project](http://clojars.org/uswitch/opencensus-clojure/latest-version.svg)](http://clojars.org/uswitch/opencensus-clojure) 

to your build tool of choice, **AND** add your preferred exporter lib, e.g.,
`[io.opencensus/opencensus-exporter-trace-zipkin "0.13.1"]`. This is because there are at least 5 exporters and we don't
want to pull in a boatload of unneeded transitives.

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

### Distributed tracing

The ring middleware will automagically pick up Zipkin B3 format headers and assign the remote context if it finds one.

To pass trace IDs downstream, use `(opencensus-clojure.trace/make-downstream-headers)` anywhere within a traced context.

Example:
```clojure
(span "foobar"
  (http-kit/get "http://google.com/all-your-base"
                {:headers (trace/make-downstream-headers)}))
```

As above, keep in mind that Ring already wraps a span, so you can do this pretty much anywhere within your app.
The function returns a `{str str}`, which is what most HTTP clients understand

### Reporting

There are multiple reporters available.

The `logging` one
```clojure
(opencensus-clojure.reporting.logging/report)
```

The Jaeger one
```clojure
(opencensus-clojure.reporting.jaeger/report "my-service-name")

; allows you to specify the Thrift HTTP endpoint manually
(opencensus-clojure.reporting.jaeger/report "http://localhost:14268/api/traces" "my-service-name")
```

The Zipkin one
```clojure
(opencensus-clojure.reporting.zipkin/report "my-service-name")

; allows you to specify the HTTP endpoint manually
(opencensus-clojure.reporting.jaeger/report "http://localhost:9411/api/v2/spans" "my-service-name")
```

The logging one is nice to figure out if your stuff is working in the first place if your Jaeger or Zipkin thing seems
to not be. 

You can launch a batteries included Jaeger thing via
```shell
$ docker run --rm -i \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp \
  -p5778:5778 -p16686:16686 -p14268:14268 -p9411:9411 \
  jaegertracing/all-in-one:latest
```
It should come up with a UI on `localhost:16686`, and you should see all your traces there.

Launching a local Zipkin handler is up to you.

#### Configuration

The traces are probabilistic by default; you can configure the tracer to `p=1.0` via
```clojure
(opencensus-clojure.trace/configure-tracer {:probability 1.0})
```
to force sampling. The configuration function also takes
- `max-annotations`
- `max-attributes`
- `max-links`
- `max-message-events`

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

# License

Copyright © 2018 uSwitch

Distributed under the Eclipse Public License, the same as Clojure.
