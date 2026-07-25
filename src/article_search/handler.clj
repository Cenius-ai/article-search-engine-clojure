(ns article-search.handler
  "Ring handler definition using Compojure routing."
  (:require [article-search.db :as db]
            [article-search.views :as views]
            [cheshire.core :as json]
            [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :as response]
            [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(defn- query-param
  "Extract a keyword param from the request, defaulting to empty string."
  [request k]
  (or (get-in request [:params k]) ""))

;; ---------------------------------------------------------------------------
;; Route handlers
;; ---------------------------------------------------------------------------

(defn- home-handler
  "GET / — search form + recent articles."
  [request]
  (let [recent (db/get-recent-articles 6)]
    {:status  200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body    (views/home-page request recent)}))

(defn- search-page-handler
  "GET /search?q=... — search results page."
  [request]
  (let [q       (query-param request :q)
        results (if (str/blank? q) [] (db/search-articles q))]
    {:status  200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body    (views/search-results-page request q results)}))

(defn- api-search-handler
  "GET /api/search?q=... — JSON search results."
  [request]
  (let [q       (query-param request :q)
        results (if (str/blank? q)
                  []
                  (map (fn [a]
                         {:id         (:id a)
                          :title      (:title a)
                          :snippet    (let [s (:content a)]
                                        (subs s 0 (min 200 (count s))))
                          :created_at (:created_at a)})
                       (db/search-articles q)))]
    {:status  200
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body    (json/generate-string {:query q :results results})}))

(defn- article-detail-handler
  "GET /articles/:id — full article view."
  [id request]
  (let [article (db/get-article-by-id id)]
    {:status  200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body    (views/article-detail-page request article)}))

(defn- article-list-handler
  "GET /articles — browse all articles."
  [request]
  (let [articles (db/get-all-articles)]
    {:status  200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body    (views/article-list-page request articles)}))

(defn- health-handler
  "GET /health — simple health check."
  [_request]
  {:status  200
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body    "{\"status\":\"ok\"}"})


(defn- hello-handler
  "GET /hello — simple Hello World check."
  [_request]
  {:status  200
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body    "Hello World"})

(defn- db-check-handler
  "GET /db-check — database connectivity check returning [{:?column? 1}]."
  [_request]
  (let [result (db/db-check)]
    {:status  200
     :headers {"Content-Type" "application/json; charset=utf-8"}
     :body    (json/generate-string (vec result))}))

;; ---------------------------------------------------------------------------
;; Security headers middleware
;; ---------------------------------------------------------------------------

(defn wrap-security-headers
  "Add security-related HTTP headers to every response."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (update :headers merge
                  {"X-Content-Type-Options"  "nosniff"
                   "X-Frame-Options"         "DENY"
                   "Referrer-Policy"         "strict-origin-when-cross-origin"
                   "Content-Security-Policy" "default-src 'self'; style-src 'self' 'unsafe-inline'"})))))

;; ---------------------------------------------------------------------------
;; Routes
;; ---------------------------------------------------------------------------

(defroutes app-routes
  (GET "/" [] home-handler)
  (GET "/search" [] search-page-handler)
  (GET "/api/search" [] api-search-handler)
  (GET "/articles" [] article-list-handler)
  (GET "/articles/:id" [id] (fn [req] (article-detail-handler id req)))
  (GET "/health" [] health-handler)
  (GET "/hello" [] hello-handler)
  (GET "/db-check" [] db-check-handler)
  (route/resources "/")
  (ANY "*" [] (fn [req] {:status 404
                         :headers {"Content-Type" "text/html; charset=utf-8"}
                         :body (views/not-found-page req)})))

;; ---------------------------------------------------------------------------
;; Application handler stack
;;   Request flows outer→inner: security-headers → content-type → resource
;;     → wrap-params (parses query string → :params with string keys)
;;     → wrap-keyword-params (converts string keys → keywords)
;;     → app-routes
;; ---------------------------------------------------------------------------

(def app
  "The full Ring handler with middleware."
  (-> app-routes
      wrap-keyword-params
      wrap-params
      (wrap-resource "public")
      wrap-content-type
      wrap-security-headers))
