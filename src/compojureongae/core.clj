(ns compojureongae.core
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use compojure.core
        [ring.util.servlet   :only [defservice]]
        [ring.util.response  :only [redirect]]
        [hiccup.core         :only [h html]]
        [hiccup.page-helpers :only [doctype include-css link-to xhtml-tag]]
        [hiccup.form-helpers :only [form-to text-area text-field]])
  (:import (com.google.appengine.api.datastore Query))
  (:require [compojure.route          :as route]
            [appengine.datastore.core :as ds]
            [appengine.users          :as users]))

;; A static HTML side bar containing some internal and external links
(defn side-bar []
  (let [ui (users/user-info)]
    [:div#sidebar
     [:h3 "Current User"]
     (if-let [user (:user ui)]
       [:ul
        [:li "Logged in as " (.getEmail user)]
        [:li (link-to (.createLogoutURL (:user-service ui) "/") "Logout")]]
       [:ul
        [:li "Not logged in"]
        [:li (link-to (.createLoginURL (:user-service ui) "/") "Login")]]
       )
     [:h3 "Navigation"]
     [:ul
      [:li (link-to "/" "Main page")]
      (if (and (:user ui) (.isUserAdmin (:user-service ui)))
        [:li (link-to "/admin/new" "Create new post (Admin only)")])]
     [:h3 "External Links"]
     [:ul
      [:li (link-to "http://compojureongae.posterous.com/" "Blog")]
      [:li (link-to "http://github.com/christianberg/compojureongae" "Source Code")]]]))

(defn google-analytics [code]
  "Returns the script tag for injecting Google Analytics site visitor tracking."
  [:script {:type "text/javascript"} (str "
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '" code "']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();")])

(defn render-page [title & body]
  "Renders HTML around a given payload, acts as a template for all pages."
  (html
   (doctype :xhtml-strict)
   (xhtml-tag "en"
                   [:head
                    [:title title]
                    (include-css "/css/main.css")]
                   [:body
                    (google-analytics "UA-16545358-1")
                    [:h1 title]
                    [:div#main body]
                    (side-bar)])))

;;; A static HTML form for entering a new post.
(def new-form
     (form-to [:post "/admin/post"]
              [:fieldset
               [:legend "Create a new post"]
               [:ol
                [:li
                 [:label {:for :title} "Title"]
                 (text-field :title)]
                [:li
                 [:label {:for :body} "Body"]
                 (text-area :body)]]
               [:button {:type "submit"} "Post!"]]))

(defn create-post [title body]
  "Stores a new post in the datastore and issues an HTTP Redirect to the main page."
  (ds/create-entity {:kind "post" :title title :body body})
  (redirect "/"))

(defn render-post [post]
  "Renders a post to HTML."
  [:div
   [:h2 (h (:title post))]
   [:p (h (:body post))]])

(defn get-posts []
  "Returns all posts stored in the datastore."
  (ds/find-all (Query. "post")))

(defn main-page []
  "Renders the main page by displaying all posts."
  (render-page "Compojure on GAE"
    (map render-post (get-posts))))

(defroutes public-routes
  (GET "/" [] (main-page)))

(defroutes admin-routes
  (GET  "/admin/new"  [] (render-page "New Post" new-form))
  (POST "/admin/post" [title body] (create-post title body)))

(defn wrap-requiring-admin [application]
  (fn [request]
    (let [{:keys [user-service]} (users/user-info request)]
      (if (.isUserAdmin user-service)
        (application request)
        {:status 403 :body "Access denied. You must be logged in as admin user!"}))))

(wrap! admin-routes
       wrap-requiring-admin
       users/wrap-requiring-login
       users/wrap-with-user-info)

(defroutes example
  public-routes
  (ANY "/admin/*" [] admin-routes)
  (route/not-found "Page not found"))

(defservice example)