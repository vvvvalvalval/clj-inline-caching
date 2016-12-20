(ns vvvvalvalval.clj-inline-caching.main
  (:require [vvvvalvalval.clj-inline-caching :as ic]
            [vvvvalvalval.clj-inline-caching.definition-site :as ds]
            [criterium.core :as bench])
  (:gen-class))

(defn test-correctness []
  (let [ax (atom 0)
        y 1
        icf (fn []
              (ic/call ds/f [@ax y] 2))]
    (assert (= (icf) ((ds/f @ax y) 2)))
    (assert (= (icf) ((ds/f @ax y) 2)))

    (swap! ax inc)
    (assert (= (icf) ((ds/f @ax y) 2)))
    (assert (= (icf) ((ds/f @ax y) 2)))
    ))

(defn g [& colls] (->> colls (apply interleave) sort reverse vec))

(def c1 (range 1000))
(def c2 (range 1000))
(def c3 (range 1000))

(def gd (g c1 c2 c3))

(defn wo-inline-caching [i]
  (get gd i))

(defn w-inline-caching [i]
  (ic/call g [c1 c2 c3] i))

(defn -main [& args]
  (test-correctness)
  #_(println (wo-inline-caching 134) (w-inline-caching 134))
  (println "benchmarking...")
  (println "control:")
  (bench/bench (wo-inline-caching 134))
  (println "with inline caching:")
  (bench/bench (w-inline-caching 134))
  (println "done")
  )

