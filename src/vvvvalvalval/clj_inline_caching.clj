(ns vvvvalvalval.clj-inline-caching
  (:require [vvvvalvalval.clj-inline-caching.impl :as impl :refer [cache*]])
  (:import [vvvvalvalval.clj_inline_caching.impl Cell]))

(def ^:dynamic ^:redef *check-freshness*
  "Whether or not to perform a cache freshness check.
  Outside of interactive development, the cache should never become stale,
  in which case you may want to set this Var to false for performance.

  Note that this value will be accounted for at compile-time, not run-time."
  true)

(defmacro cached
  [keys-vec get-v]
  (let [n (count keys-vec)
        ks (for [i (range n)] (gensym (str "k" i)))
        cache-sym (vary-meta (gensym "cache") merge {:no-doc true})
        read-cell `(let [cell# ~cache-sym]
                     (if (instance? Cell cell#)             ;; at this point, cell# may be clojure.lang.Var&Unbound when using AOT compilation.
                       cell#
                       (impl/cache-var-miss* (var ~cache-sym))))
        _interned (intern *ns* cache-sym (Cell.))]
    (if *check-freshness*
      `(let [~@(interleave ks keys-vec)
             getv# ~get-v
             cell# ~read-cell]
         (impl/cache* cell#
           ~@(cond
               (= n 0) ()
               (<= 1 n impl/max-arity) ks
               (< impl/max-arity n) [(vec ks)])
           getv#))
      `(let [cell# ~read-cell]
         (impl/cache* cell# ~get-v)))
    ))

(defmacro call
  "Caches inline the invocation of f on the provided arguments.
  Except during interactive development, the values of f and its arguments for a given call site should never change."
  [f & args]
  (if *check-freshness*
    (let [f-sym (gensym "f")
          arg-syms (repeatedly (count args) gensym)]
      `(let [~f-sym ~f
             ~@(interleave arg-syms args)]
         (cached [~f-sym ~@arg-syms] (fn [] (~f-sym ~@arg-syms)))))
    `(cached [] (fn [] (~f ~@args)))))
