(ns local-dev
  "Tools for local development.
   Enables the use of the App Engine APIs on the REPL and in a local Jetty instance."
  (:use ring.adapter.jetty
        [ring.middleware file file-info])
  (:import [java.io File]
           [java.util HashMap]
           [com.google.apphosting.api ApiProxy ApiProxy$Environment]
           [com.google.appengine.tools.development
            ApiProxyLocalFactory
            LocalServerEnvironment]))

(defonce *server* (atom nil))
(def *port* 8181)

(defn- set-app-engine-environment []
  "Sets up the App Engine environment for the current thread."
  (let [att (HashMap. {"com.google.appengine.server_url_key"
                       (str "http://localhost:" *port*)})
        env-proxy (proxy [ApiProxy$Environment] []
                    (isLoggedIn [] false)
                    (getRequestNamespace [] "")
                    (getDefaultNamespace [] "")
                    (getAttributes [] att)
                    (getAppId [] "_local_"))]
    (ApiProxy/setEnvironmentForCurrentThread env-proxy)))

(defn- set-app-engine-delegate [dir]
  "Initializes the App Engine services. Needs to be run (at least) per JVM."
  (let [local-env (proxy [LocalServerEnvironment] []
                    (getAppDir [] (File. dir))
                    (getAddress [] "localhost")
                    (getPort [] *port*)
                    (waitForServerToStart [] nil))
        api-proxy (.create (ApiProxyLocalFactory.)
                           local-env)]
    (ApiProxy/setDelegate api-proxy)))

(defn init-app-engine
  "Initializes the App Engine services and sets up the environment. To be called from the REPL."
  ([] (init-app-engine "/tmp"))
  ([dir]
     (set-app-engine-delegate dir)
     (set-app-engine-environment)))

(defn wrap-local-app-engine [app]
  "Wraps a ring app to enable the use of App Engine Services."
  (fn [req]
    (set-app-engine-environment)
    (app req)))

(defn start-server [app]
  "Initializes the App Engine services and (re-)starts a Jetty server
   running the supplied ring app, wrapping it to enable App Engine API use
   and serving of static files."
  (set-app-engine-delegate "/tmp")
  (swap! *server* (fn [instance]
                   (when instance
                     (.stop instance))
                   (let [app (-> app
                                 (wrap-local-app-engine)
                                 (wrap-file "./war")
                                 (wrap-file-info))]
                     (run-jetty app {:port *port*
                                     :join? false})))))

(defn stop-server []
  "Stops the local Jetty server."
  (swap! *server* #(when % (.stop %))))

