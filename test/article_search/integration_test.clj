(ns article-search.integration-test
  "Integration tests for the search API and handlers.
   Run: clojure -M:test"
  (:require [article-search.db :as db]
            [article-search.seed :as seed]
            [article-search.handler :as handler]
            [clojure.test :refer :all]
            [cheshire.core :as json]
            [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Fixtures
;; ---------------------------------------------------------------------------

(defn setup-db
  "Initialize and seed a fresh test database."
  []
  (db/init-db!)
  (seed/seed!))

;; ---------------------------------------------------------------------------
;; Tests
;; ---------------------------------------------------------------------------

(deftest test-hello-world
  (setup-db)
  (let [resp (handler/app {:uri "/hello" :request-method :get})]
    (is (= 200 (:status resp)))
    (is (= "Hello World" (:body resp)))))

(deftest test-db-check
  (setup-db)
  (let [resp   (handler/app {:uri "/db-check" :request-method :get})
        parsed (json/parse-string (:body resp) true)]
    (is (= 200 (:status resp)))
    (is (vector? parsed))
    (is (pos? (count parsed)))
    (is (contains? (first parsed) :?column?))
    (is (= 1 (:?column? (first parsed))))))

(deftest test-root-returns-html
  (setup-db)
  (let [resp (handler/app {:uri "/" :request-method :get})]
    (is (= 200 (:status resp)))
    (is (str/includes? (:body resp) "<!DOCTYPE html>"))
    (is (str/includes? (:body resp) "Article Search"))
    (is (str/includes? (:body resp) "search-form"))))

(deftest test-search-page-returns-html
  (setup-db)
  (let [resp (handler/app {:uri "/search" :request-method :get :params {"q" "Clojure"}})]
    (is (= 200 (:status resp)))
    (is (str/includes? (:body resp) "<!DOCTYPE html>"))
    (is (str/includes? (:body resp) "Clojure"))))

(deftest test-api-search-returns-json
  (setup-db)
  (let [resp   (handler/app {:uri "/api/search" :request-method :get :params {"q" "Clojure"}})
        parsed (json/parse-string (:body resp) true)]
    (is (= 200 (:status resp)))
    (is (= "Clojure" (:query parsed)))
    (is (contains? parsed :results))
    (is (pos? (count (:results parsed))))
    (is (contains? (first (:results parsed)) :title))
    (is (contains? (first (:results parsed)) :snippet))))

(deftest test-api-search-empty-query
  (setup-db)
  (let [resp   (handler/app {:uri "/api/search" :request-method :get :params {"q" ""}})
        parsed (json/parse-string (:body resp) true)]
    (is (= 200 (:status resp)))
    (is (= 0 (count (:results parsed))))))

(deftest test-api-search-no-match
  (setup-db)
  (let [resp   (handler/app {:uri "/api/search" :request-method :get :params {"q" "xyznonexistent999"}})
        parsed (json/parse-string (:body resp) true)]
    (is (= 200 (:status resp)))
    (is (= 0 (count (:results parsed))))))

(deftest test-article-detail-page
  (setup-db)
  (let [articles (db/get-all-articles)
        first-id (:id (first articles))
        resp     (handler/app {:uri (str "/articles/" first-id) :request-method :get})]
    (is (= 200 (:status resp)))
    (is (str/includes? (:body resp) "<!DOCTYPE html>"))
    (is (str/includes? (:body resp) (:title (first articles))))))

(deftest test-article-list-page
  (setup-db)
  (let [resp (handler/app {:uri "/articles" :request-method :get})]
    (is (= 200 (:status resp)))
    (is (str/includes? (:body resp) "<!DOCTYPE html>"))
    (is (str/includes? (:body resp) "All articles"))))

(deftest test-health-endpoint
  (setup-db)
  (let [resp (handler/app {:uri "/health" :request-method :get})]
    (is (= 200 (:status resp)))
    (is (str/includes? (:body resp) "\"ok\""))))

(deftest test-not-found
  (setup-db)
  (let [resp (handler/app {:uri "/nonexistent" :request-method :get})]
    (is (= 404 (:status resp)))
    (is (str/includes? (:body resp) "Page not found"))))

(deftest test-security-headers
  (setup-db)
  (let [resp (handler/app {:uri "/" :request-method :get})]
    (is (= "nosniff" (get-in resp [:headers "X-Content-Type-Options"])))
    (is (= "DENY" (get-in resp [:headers "X-Frame-Options"])))))

(deftest test-search-highlight-present
  (setup-db)
  ;; Search for a term that appears in article content — should be highlighted
  (let [resp (handler/app {:uri "/search" :request-method :get :params {"q" "immutability"}})
        body (:body resp)]
    (is (= 200 (:status resp)))
    (is (str/includes? body "search-highlight"))
    (is (str/includes? body "immutability"))))

(deftest test-snippet-is-first-200-chars
  (setup-db)
  ;; Search for "Ring" — appears in some article contents.
  ;; The snippet must start from the beginning of the content (first 200 chars),
  ;; not from a window around the match.
  (let [resp   (handler/app {:uri "/search" :request-method :get :params {"q" "Ring"}})
        body   (:body resp)]
    (is (= 200 (:status resp)))
    ;; The first snippet on the page should contain the opening of the article,
    ;; e.g. "Ring is the foundational HTTP abstraction library..."
    ;; so the first result-item should have text from the article start.
    (is (str/includes? body "foundational HTTP abstraction"))))

(deftest test-demo-user-seeded
  (setup-db)
  (let [user (db/get-user-by-username "cenius")]
    (is (some? user))
    (is (= "cenius" (:username user)))))

(defn -main
  "Run all tests and exit."
  [& _args]
  (println "==> Running integration tests...")
  (setup-db)
  (let [results (run-tests 'article-search.integration-test)]
    (let [failures (+ (:fail results) (:error results))]
      (println (str "==> Tests: " (:pass results) " passed, " failures " failed"))
      (when (pos? failures)
        (System/exit 1))
      (System/exit 0))))
