(ns vvvvalvalval.clj-inline-caching-test
  (:require [clojure.test :refer :all]
            [vvvvalvalval.clj-inline-caching :as ic :refer :all]))

(defn ho0 []
  (let [v (->> (range 10000) shuffle sort vec)]
    (fn [i] (get v i))))

(def ho0-manual (ho0))

(defn f0-manual [i]
  (ho0-manual i))

(defn f0-ic [i]
  (ic/call ho0 [] i))

(deftest arity-0
  (testing "arity 0"
    (is
      (= (f0-manual 1299)
        (f0-ic 1299) (f0-ic 1299)
        @(future (f0-ic 1299))))))

(defn ho [& colls]
  (let [v (->> colls (apply interleave) vec)]
    (fn [i] (get v i))))

(defn random-vec []
  (vec (shuffle (range 1000))))

(def c1 (random-vec))
(def a-c2 (atom (random-vec)))

(deftest arity-3
  (testing "arity 3"
    (let [c3 (random-vec)

          f3-ic (fn [i]
                  (ic/call ho [c1 @a-c2 c3] i))]

      (let [ho3-manual (ho c1 @a-c2 c3)
            f3-manual (fn [i]
                        (ho3-manual i))]
        (is (= (f3-manual 1299) (f3-ic 1299) (f3-ic 1299) @(future (f3-ic 1299)))))

      (reset! a-c2 (random-vec))

      (let [ho3-manual (ho c1 @a-c2 c3)
            f3-manual (fn [i]
                        (ho3-manual i))]
        (is (= (f3-manual 1299) (f3-ic 1299) (f3-ic 1299) @(future (f3-ic 1299)))))
      )))


(defn hobig
  [& xs]
  (let [prefix (apply str xs)]
    (fn [y]
      (str prefix y))))

(def x1 "a")
(def x2 (atom 3))

(deftest arity-big
  (testing "arity 3"
    (let [x3 :x3

          f-ic (fn [x]
                 (ic/call hobig
                   [x1 @x2 x3 4 "x5" true \7 'x8 nil [] {}] x))]

      (let [hobig-manual (hobig x1 @x2 x3 4 "x5" true \7 'x8 nil [] {})
            f-manual (fn [x]
                        (hobig-manual x))]
        (is (= (f-manual 1299) (f-ic 1299) (f-ic 1299) @(future (f-ic 1299)))))

      (reset! x2 -3)

      (let [hobig-manual (hobig x1 @x2 x3 4 "x5" true \7 'x8 nil [] {})
            f-manual (fn [x]
                       (hobig-manual x))]
        (is (= (f-manual 1299) (f-ic 1299) (f-ic 1299) @(future (f-ic 1299)))))
      )))
