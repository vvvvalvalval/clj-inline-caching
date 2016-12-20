(ns vvvvalvalval.clj-inline-caching.impl
  (:import [vvvvalvalval.clj_inline_caching.impl Cell]))

(def max-arity 6)

(defmacro def-memo-records []
  `(do
     ~@(for [i (range max-arity)]
         `(defrecord ~(symbol (str "Memo" i))
            [~@(for [j (range (inc i))] (symbol (str "k" j))) ~'v]))))

(def-memo-records)

(defmacro write-cache* []
  `(fn
     ~@(for [i (range max-arity)]
         (let [Memo-sym (symbol (str "Memo" i))
               ->Memo-sym (symbol (str "->Memo" i))
               ckv (vary-meta (gensym "ckv") merge {} {:tag Memo-sym})
               kjs (for [j (range (inc i))] (gensym (str "k" j)))]
           `([^Cell cell#, ~@kjs, get-v#]
              (let [~ckv (.get cell#)
                    v# (if (nil? ~ckv)
                         (let [v# (get-v#)]
                           (.set cell# (~->Memo-sym ~@kjs v#))
                           v#)
                         (if (and ~@(map (fn [j kj]
                                           `(= ~kj (~(symbol (str ".k" j)) ~ckv)))
                                      (range (inc i)) kjs))
                           (.v ~ckv)
                           (let [v# (get-v#)]
                             (.set cell# (~->Memo-sym ~@kjs v#))
                             v#)))]
                v#))))
     ))

(def cache* (write-cache*))

(defn cache-var-miss
  [cache-var]
  (if (bound? cache-var)
    (throw (IllegalStateException. "Cache Var bound with illegal value."))
    (let [cell (Cell. nil)]
      (alter-var-root
        cache-var
        (constantly cell))
      cell)))
