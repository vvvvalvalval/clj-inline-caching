(ns vvvvalvalval.clj-inline-caching.impl
  "The implementation strategy is to have a cache* function which has one arity per number of keys,
  (up to `max-arity`, after which the keys are wrapped in a vector).

  Each k-arity has its own Memo record, having k keys field and a v field.
  Comparison of the keys with the memo record is done inline for efficiency (no loop, no polymorphism).

  There's a special case for the arity where the set of keys is empty.
  In this case, the value is stored directly in the cell.
  The Cell class has a special method to know if it has never been set, which allows for storing nil has a value as well."
  (:import [vvvvalvalval.clj_inline_caching.impl Cell]))

(def max-arity 6)

(defmacro def-memo-records
  "generates forms like (defrecord Memo3 [k0 k1 k2 k3 v])"
  []
  `(do
     ~@(for [i (range max-arity)]
         `(defrecord ~(symbol (str "Memo" i))
            [~@(for [j (range (inc i))] (symbol (str "k" j))) ~'v]))))

(def-memo-records)

(defmacro write-cache*
  "Generates the code for the cache* function."
  []
  `(fn
     ;; 0-arity case: simpler as it needs not use a Memo record holding keys + value.
     ([^Cell cell#, get-v#]
      (let [v# (.get cell#)]
        (if (Cell/isOUnset v#)
          (let [v# (get-v#)]
            (.set cell# v#)
            v#)
          v#)))
     ~@(for [i (range max-arity)]
         (let [Memo-sym (symbol (str "Memo" i))
               ;->Memo-sym (symbol (str "->Memo" i))
               memo (vary-meta (gensym "memo") merge {} {:tag Memo-sym})
               kjs (for [j (range (inc i))] (gensym (str "k" j)))]
           `([^Cell cell#, ~@kjs, get-v#]
              (let [memo# (.get cell#)
                    v# (if (Cell/isOUnset memo#)
                         (let [v# (get-v#)]
                           (.set cell# (new ~Memo-sym ~@kjs v#))
                           v#)
                         (let [~memo memo#]
                           (if (and ~@(map (fn [j kj]
                                             `(= ~kj (~(symbol (str ".k" j)) ~memo)))
                                        (range (inc i)) kjs))
                             (.v ~memo)
                             (let [v# (get-v#)]
                               (.set cell# (new ~Memo-sym ~@kjs v#))
                               v#))))]
                v#))))
     ))

(def cache* (write-cache*))

(defn cache-var-miss*
  [cache-var]
  (if (bound? cache-var)
    (throw (IllegalStateException. "Cache Var bound with illegal value."))
    (let [cell (Cell.)]
      (alter-var-root
        cache-var
        (constantly cell))
      cell)))
