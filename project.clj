(defproject vvvvalvalval/clj-inline-caching "0.4.2-SNAPSHOT"
  :description "Helpers for inline caching."
  :url "https://github.com/vvvvalvalval/clj-inline-caching"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :java-source-paths ["java"]
  :profiles
  {:dev
   {:dependencies [[org.clojure/clojure "1.8.0"]
                   [criterium "0.4.3"]]
    :source-paths ["dev"]}
   :bench
   {:dependencies [[org.clojure/clojure "1.8.0"]
                   [criterium "0.4.3"]]
    :source-paths ["dev"]
    :main vvvvalvalval.clj-inline-caching.main}
   :uberjar
   {:omit-source true
    :env {:production true}
    :aot :all}
   :clojure6
   {:dependencies [[org.clojure/clojure "1.6.0"]]}})
