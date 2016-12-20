(ns vvvvalvalval.clj-inline-caching
  (:require [vvvvalvalval.clj-inline-caching.impl :as impl :refer [cache*]])
  (:import [vvvvalvalval.clj_inline_caching.impl Cell]))

(defmacro cached
  [keys-vec get-v]
  (let [n (count keys-vec)
        ks (for [i (range n)] (gensym (str "k" i)))
        cache-sym (vary-meta (gensym "cache") merge {:no-doc true})
        _interned (intern *ns* cache-sym (Cell. nil))]
    `(let [~@(interleave ks keys-vec)
           getv# ~get-v
           cell# (let [cell# ~cache-sym]
                   (if (instance? Cell cell#) ;; at this point, cell# may be clojure.lang.Var&Unbound when using AOT compilation.
                     cell#
                     (impl/cache-var-miss (var ~cache-sym))))]
       (impl/cache* cell# ~@ks getv#))
    ))

(defmacro call
  "Usage: (call f [x1 x2 ... xp] y1 y2 ... yq)
  Has the same effect as calling ((f x1 x2 ... xp) y1 y2 ... yq),
  except that the call to (f x1 x2 ...) is cached inline.

  The presumption is that, outside of interactive development, x1 ... xp will always evaluate to the same values for a given site;
  otherwise using this macro would be pointless (and unsafe wrt concurrency)."
  [f args1 & args2]
  (let [n (count args1)
        ksyms (for [i (range n)] (gensym (str "k" i)))]
    `(let [~@(interleave ksyms args1)]
       ((cached
          ~(cond
             (= n 0) [`nil]
             (<= 1 n impl/max-arity) (vec ksyms)
             (< impl/max-arity n) [(vec ksyms)])
          (fn [] (~f ~@ksyms))) ~@args2))))
