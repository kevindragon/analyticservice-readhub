(ns asrh.core
  (:require [clojure.java.io :as io]
            [asrh.fetch :as fetch]
            [asrh.index :as index]
            [cheshire.core :refer [generate-string]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [GET POST DELETE] :as compojure]
            [compojure.route :as compojure-route])
  (:gen-class))

(defn perform-index []
  (doseq [doc (fetch/fetch-top-n-topic 1000)]
    (index/add doc))
  (index/commit)
  {:body {:status :ok}})

(defn perform-query [{:keys [query]}]
  {:body (index/query query 0 10)})

(compojure/defroutes http-routes
  (GET "/" [] (io/resource "public/index.html"))
  (GET "/query" {:keys [params]} (perform-query params))
  (GET "/index" [] (perform-index))
  (compojure-route/not-found "<h1>Page not found</h1>"))

(defn wrap-json-response [handler]
  (fn [request]
    (let [response (handler request)
          body (get response :body nil)]
      (if (some #(% body) [map? coll? vector? symbol?])
        (-> response
          (assoc-in [:headers "Content-Type"] "application/json; charset=utf-8")
          (update :body generate-string))
        response))))

(def http-app
  (-> #'http-routes
      (wrap-reload)
      (wrap-resource "public")
      (wrap-resource "")
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)))
      (wrap-json-params)
      (wrap-json-response)))

(defonce http-server (atom nil))

(defn stop-http-server []
  (when @http-server (.stop @http-server)))

(defn start-http-server [join port]
  (stop-http-server)
  (if join
    (run-jetty #'http-app {:join? join :port port})
    (reset! http-server (run-jetty #'http-app {:join? join :port port}))))

(defn str->int [s & [default]]
  (try (Integer/parseInt s)
       (catch Exception e (or default 0))))

(defn -main [& args]
  (let [port (str->int (first args) 8080)]
    (println "start http server on" port)
    (println "loading index files")
    (index/load-index)
    (println "load index files done")
    (start-http-server true port)))
