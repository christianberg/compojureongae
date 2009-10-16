(ns blog
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.http.routes)
  (:use compojure.http.servlet)
  (:use compojure.html))

(def site-title "Compojure on GAE - Example Blog")

(def posts (sorted-map 
            1 {:id 1
               :title "Hello World!"
               :body  "This is my first post. Welcome to my example blog."}
            2 {:id 2
               :title "Second post"
               :body  "Already I'm out of things to say."}))

(defn page-template [title body &]
  (html [:html
         [:head [:title (str site-title ": " title)]]
         [:body
          [:div.header [:h1 site-title]]
          [:div.content body]
          [:div.footer "&copy; 2009 Christian Berg"]]]))

(defn display-post [post]
  [:div [:h2.post-header (post :title)]
   [:p.post-body (post :body)]])

(defn show-main-page []
  (page-template "Home"
                 (map (comp display-post val) posts)))

(defn page-not-found []
  [404 "The page you requested does not exist."])

(defroutes main-routes
  (GET "/" (show-main-page))
  (ANY "/*" (page-not-found)))

(defservice main-routes)

