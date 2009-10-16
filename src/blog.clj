(ns blog
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.http.routes)
  (:use compojure.http.servlet)
  (:use compojure.html))

(defn show-main-page []
  "Hello World!")

(defn page-not-found []
  [404 "The page you requested does not exist."])

(defroutes main-routes
  (GET "/" (show-main-page))
  (ANY "/*" (page-not-found)))

(defservice main-routes)

