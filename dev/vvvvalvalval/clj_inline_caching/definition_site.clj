(ns vvvvalvalval.clj-inline-caching.definition-site)

(defn f [x y]
  (fn [z] (+ x y z)))
