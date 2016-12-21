# clj-inline-caching

A Clojure library providing helpers for implementing custom inline caching,
which is a more REPL-friendly way of caching than defining Vars derived from other Vars.

Inspired by the implementation of [Specter 0.13.1](https://github.com/nathanmarz/specter/blob/fdd74ea2243bc089eecd984edd8953e4321adcd4/src/clj/com/rpl/specter.cljc#L226).
Works by interning an anonymous Var holding a mutable cell to store the intermediate result of a computation.

Project status: alpha, subject to breaking change.

## Usage

FIXME

### Caveats

In production, the cache keys passed to the macros should simply never change.

Note that the cache store is attached to the call site, *in the code*.
Be careful when you refactor into something more generic.

Currently only for Clojure JVM.

## Running benchmarks

```
$ lein with-profile +bench uberjar && java -jar target/clj-inline-caching-0.1.0-SNAPSHOT-standalone.jar
```

According to the benchmarks I've run so far, the overhead of dereferencing the generated Var is on the order of 10ns.

## TODO

* tests
* documentation
* faster 0-arity case?

## License

Copyright © 2016 Valentin Waeselynck and contributors

Distributed under the MIT license.
