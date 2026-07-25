(ns article-search.db
  "Database connection, schema creation, and query helpers.
   Defaults to embedded SQLite at data/app.db.
   Set DATABASE_URL for PostgreSQL."
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (java.io File)))

;; ---------------------------------------------------------------------------
;; Datasource (read-write, for schema + inserts)
;; ---------------------------------------------------------------------------

(defn- build-datasource
  "Create a next.jdbc datasource from env or SQLite default."
  []
  (let [db-url (System/getenv "DATABASE_URL")]
    (if db-url
      (jdbc/get-datasource db-url)
      (do
        (.mkdirs (File. "data"))
        (jdbc/get-datasource
          {:dbtype   "sqlite"
           :dbname   "data/app.db"})))))

(defonce datasource
  (delay (build-datasource)))

(defn get-ds [] @datasource)

;; ---------------------------------------------------------------------------
;; Read-only datasource — for all search / read queries.
;;   SQLite needs read-only set at connection-open time via the JDBC URL
;;   parameter `open_mode=1`.  PostgreSQL uses jdbc/with-options.
;; ---------------------------------------------------------------------------

(defn- build-ro-datasource
  "Create a read-only datasource."
  []
  (let [db-url (System/getenv "DATABASE_URL")]
    (if db-url
      (jdbc/with-options (jdbc/get-datasource db-url) {:read-only true})
      (do
        (.mkdirs (File. "data"))
        (jdbc/get-datasource
          {:dbtype  "sqlite"
           :dbname  "data/app.db?open_mode=1"})))))

(defonce ro-datasource
  (delay (build-ro-datasource)))

(defn get-ro-ds
  "Return the read-only datasource for search / read queries."
  []
  @ro-datasource)

;; ---------------------------------------------------------------------------
;; Builder opts — unqualified, lowercase column names
;; ---------------------------------------------------------------------------

(def builder-opts
  "Return rows as maps with unqualified lowercase keywords as keys."
  {:builder-fn rs/as-unqualified-lower-maps})

;; ---------------------------------------------------------------------------
;; Schema
;; ---------------------------------------------------------------------------

(def schema-ddl
  ["CREATE TABLE IF NOT EXISTS users (
     id         TEXT PRIMARY KEY,
     username   TEXT NOT NULL UNIQUE,
     password_hash TEXT NOT NULL,
     created_at TEXT NOT NULL DEFAULT (datetime('now'))
   )"
   "CREATE TABLE IF NOT EXISTS articles (
     id         TEXT PRIMARY KEY,
     title      TEXT NOT NULL,
     content    TEXT NOT NULL,
     author_id  TEXT NOT NULL REFERENCES users(id),
     created_at TEXT NOT NULL DEFAULT (datetime('now'))
   )"
   "CREATE INDEX IF NOT EXISTS idx_articles_title ON articles(title)"
   "CREATE INDEX IF NOT EXISTS idx_articles_created ON articles(created_at)"])

(defn init-db!
  "Create tables if they don't exist.  Idempotent.  Uses read-write DS."
  []
  (let [ds (get-ds)]
    (doseq [ddl schema-ddl]
      (jdbc/execute! ds [ddl]))
    :ok))

;; ---------------------------------------------------------------------------
;; Read queries — all use the read-only datasource
;; ---------------------------------------------------------------------------

(defn search-articles
  "Return articles whose title or content matches query (case-insensitive LIKE)."
  [query]
  (let [ds  (get-ro-ds)
        q   (str "%" query "%")
        sql "SELECT id, title, content, author_id, created_at
             FROM articles
             WHERE title LIKE ? OR content LIKE ?
             ORDER BY created_at DESC"]
    (jdbc/execute! ds (into [sql q q]) builder-opts)))

(defn get-article-by-id
  "Return a single article by id, or nil."
  [id]
  (let [ds  (get-ro-ds)
        sql "SELECT id, title, content, author_id, created_at
             FROM articles WHERE id = ?"]
    (first (jdbc/execute! ds [sql id] builder-opts))))

(defn get-all-articles
  "Return all articles ordered by newest first."
  []
  (let [ds  (get-ro-ds)
        sql "SELECT id, title, content, author_id, created_at
             FROM articles ORDER BY created_at DESC"]
    (jdbc/execute! ds [sql] builder-opts)))

(defn get-recent-articles
  "Return the n most recent articles."
  [n]
  (let [ds  (get-ro-ds)
        sql "SELECT id, title, content, author_id, created_at
             FROM articles ORDER BY created_at DESC LIMIT ?"]
    (jdbc/execute! ds (into [sql n]) builder-opts)))

(defn count-articles
  "Return total article count."
  []
  (let [ds  (get-ro-ds)
        sql "SELECT count(*) AS cnt FROM articles"]
    (:cnt (first (jdbc/execute! ds [sql] builder-opts)))))

(defn get-user-by-username
  "Return user row by username, or nil."
  [username]
  (let [ds  (get-ro-ds)
        sql "SELECT id, username, password_hash, created_at
             FROM users WHERE username = ?"]
    (first (jdbc/execute! ds [sql username] builder-opts))))

(defn db-check
  "Database connectivity check — returns [{:?column? 1}] when DB is reachable."
  []
  (let [ds (get-ro-ds)]
    (jdbc/execute! ds ["SELECT 1 AS \"?column?\""] builder-opts)))

;; ---------------------------------------------------------------------------
;; Write operations — use the read-write datasource
;; ---------------------------------------------------------------------------

(defn insert-user!
  "Insert a user row.  Returns nil."
  [id username password-hash]
  (let [ds  (get-ds)
        sql "INSERT OR IGNORE INTO users (id, username, password_hash)
             VALUES (?, ?, ?)"]
    (jdbc/execute! ds [sql id username password-hash])
    nil))

(defn insert-article!
  "Insert an article row."
  [id title content author-id created-at]
  (let [ds  (get-ds)
        sql "INSERT OR IGNORE INTO articles (id, title, content, author_id, created_at)
             VALUES (?, ?, ?, ?, ?)"]
    (jdbc/execute! ds [sql id title content author-id created-at])
    nil))

(defn article-count
  "How many articles exist?"
  []
  (:cnt (first (jdbc/execute! (get-ro-ds) ["SELECT count(*) AS cnt FROM articles"] builder-opts))))

(defn user-count
  "How many users exist?"
  []
  (:cnt (first (jdbc/execute! (get-ro-ds) ["SELECT count(*) AS cnt FROM users"] builder-opts))))
