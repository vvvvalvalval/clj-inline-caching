(ns vvvvalvalval.clj-inline-caching
  (:require [vvvvalvalval.clj-inline-caching.impl :as impl :refer [cache*]])
  (:import [vvvvalvalval.clj_inline_caching.impl Cell]))

(def ^:dynamic ^:redef *check-freshness*
  "Whether or not to perform a cache freshness check.
  Outside of interactive development, the cache should never become stale,
  in which case you may want to set this Var to false for performance.

  Note that this value will be accounted for at macroexpansion-time, not run-time."
  true)

(defmacro cached
  "Given:
  * `deps`, a vector of expressions which are used for cache invalidation during interactive development (i.e when *check-freshness* is set to true)
  * `get-v`, a 0-arity function which computes a value
  Expands to code which lazily caches the `(get-v)` call in a cell attached to the call site.
  In production (i.e outside of interactive development), the `(get-v)` call should always return the same value:
  the presumption is that the call to `get-v` is expansive, but its value is a constant (typically derived from other constants).

  If *check-freshness* is true at macroexpansion-time (which is the recommended setting for interactive development),
  the cache will be invalidated if any of the expressions in `deps` evaluates to a new value (in the sense of `clojure.core/=`).

  If *check-freshness* is false at macroexpansion-time (which is the recommened setting in production, for performance),
  the expressions in `deps` will never get evaluated, and no cache-invalidation will occur."
  [deps get-v]
  (let [n (count deps)
        dep-syms (for [i (range n)] (gensym (str "dep" i)))
        cache-sym (vary-meta (gensym "cache") merge {:no-doc true})
        read-cell `(let [cell# ~cache-sym]
                     (if (instance? Cell cell#)             ;; at this point, cell# may be clojure.lang.Var&Unbound when using AOT compilation.
                       cell#
                       (impl/cache-var-miss* (var ~cache-sym))))
        _interned (intern *ns* cache-sym (Cell.))]
    (if *check-freshness*
      `(let [~@(interleave dep-syms deps)
             getv# ~get-v
             cell# ~read-cell]
         (impl/cache* cell#
           ~@(cond
               (= n 0) ()
               (<= 1 n impl/max-arity) dep-syms
               (< impl/max-arity n) [(vec dep-syms)])
           getv#))
      `(let [cell# ~read-cell]
         (impl/cache* cell# ~get-v)))
    ))

(defmacro call
  "Caches inline the invocation of f on the provided arguments.

  Except during interactive development, the values of f and its arguments for a given call site should never change.

  If *check-freshness* is false at macroexpansion-time, the expressions for f and its arguments will only be evaluated on the first execution
  (i.e when the cache misses.)

  See the doc of `cached` for usage considerations."
  [f & args]
  (if *check-freshness*
    (let [f-sym (gensym "f")
          arg-syms (repeatedly (count args) gensym)]
      `(let [~f-sym ~f
             ~@(interleave arg-syms args)]
         (cached [~f-sym ~@arg-syms] (fn [] (~f-sym ~@arg-syms)))))
    `(cached [] (fn [] (~f ~@args)))))
