(ns blog
  (:use compojure.http.routes)
  (:use compojure.http.servlet)
  (:use compojure.server.jetty)
  (:use compojure.html))

(defn show-main-page []
  "Hello World!")

(defn page-not-found []
  [404 "The page you requested does not exist."])

(defroutes main-routes
  (GET "/" (show-main-page))
  (ANY "/*" (page-not-found)))

(def server
     (run-server {:port 8080}
                 "/*" (servlet main-routes)))