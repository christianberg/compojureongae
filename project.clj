(defproject compojureongae "0.2.0-SNAPSHOT"
  :description "Example app for deployoing Compojure on Google App Engine"
  :namespaces [compojureongae.core]
  :dependencies [[compojure "0.4.0-SNAPSHOT"]
                 [ring/ring-servlet "0.2.1"]]
  :dev-dependencies [[swank-clojure "1.2.0"]]
  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib")