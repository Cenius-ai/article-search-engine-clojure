(ns article-search.core
  "Application entry point.  Starts the Ring/Jetty server on 0.0.0.0:$PORT."
  (:require [article-search.db :as db]
            [article-search.seed :as seed]
            [article-search.handler :as handler]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main
  "Boot: init DB, seed if empty, start Jetty."
  [& _args]
  ;; Initialize database (idempotent CREATE TABLE IF NOT EXISTS)
  (println "==> Initializing database...")
  (db/init-db!)

  ;; Seed demo data if tables are empty (idempotent)
  (println "==> Checking seed data...")
  (seed/seed!)

  ;; Determine port
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println (str "==> Starting server on 0.0.0.0:" port))
    (jetty/run-jetty handler/app
                     {:host  "0.0.0.0"
                      :port  port
                      :join? true})))
