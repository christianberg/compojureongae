(ns blog
  (:use compojure.http.routes)
  (:use compojure.http.servlet)
  (:use compojure.server.jetty)
  (:use compojure.html))

(defn show-main-page []
  "Hello World!")

(defroutes main-routes
  (GET "/" (show-main-page)))

(def server
     (run-server {:port 8080}
                 "/*" (servlet main-routes)))