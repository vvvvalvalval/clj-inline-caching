(ns vvvvalvalval.clj-inline-caching
  (:require [vvvvalvalval.clj-inline-caching.impl :as impl :refer [cache*]])
  (:import [vvvvalvalval.clj_inline_caching.impl Cell]))

(defmacro cached
  [keys-vec get-v]
  (let [n (count keys-vec)
        ks (for [i (range n)] (gensym (str "k" i)))
        cache-sym (vary-meta (gensym "cache") merge {:no-doc true})
        _interned (intern *ns* cache-sym (Cell.))]
    `(let [~@(interleave ks keys-vec)
           getv# ~get-v
           cell# (let [cell# ~cache-sym]
                   (if (instance? Cell cell#) ;; at this point, cell# may be clojure.lang.Var&Unbound when using AOT compilation.
                     cell#
                     (impl/cache-var-miss* (var ~cache-sym))))]
       (impl/cache* cell#
         ~@(cond
             (= n 0) ()
             (<= 1 n impl/max-arity) ks
             (< impl/max-arity n) [(vec ks)])
         getv#))
    ))

(defmacro call
  "Caches inline the invocation of f on the provided arguments.
  Except during interactive development, the values of f and its arguments for a given call site should never change."
  [f & args]
  (let [f-sym (gensym "f")
        arg-syms (repeatedly (count args) gensym)]
    `(let [~f-sym ~f
           ~@(interleave arg-syms args)]
       (cached [~f-sym ~@arg-syms] (fn [] (~f-sym ~@arg-syms))))))
