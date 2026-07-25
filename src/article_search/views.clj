(ns article-search.views
  "Hiccup-based HTML rendering.  All user-supplied strings are auto-escaped
   by Hiccup; raw HTML is only emitted for the search-highlight <mark> tags
   whose content is escaped keyword text."
  (:require [hiccup.core :as hiccup]
            [hiccup.util :as hu]
            [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(defn- format-date
  "Format an ISO instant string into a human-readable date."
  [s]
  (try
    (let [date-str (str/replace (subs s 0 10) #"-" " ")]
      date-str)
    (catch Exception _ s)))

(defn- truncate
  "Truncate string to n chars, appending ellipsis if truncated."
  [s n]
  (if (<= (count s) n)
    s
    (str (subs s 0 n) "…")))

;; ---------------------------------------------------------------------------
;; Search highlighting — plan spec:
;;   Extract the first 200 characters of the article content.
;;   If the search keyword appears in the content, highlight all
;;   occurrences by wrapping with <mark class=\"search-highlight\">.
;;   If the keyword only matches the title, provide an unhighlighted
;;   snippet of the first 200 characters.
;; ---------------------------------------------------------------------------

(defn highlight-snippet
  "Extract the first 200 chars of `content`.  If `query` appears anywhere
   in the full content, highlight all its occurrences within those first
   200 chars.  Otherwise return a plain escaped snippet."
  [content query]
  (let [window-size 200]
    (if (or (nil? content) (nil? query) (str/blank? query))
      ;; No query — plain escaped first 200 chars
      (hu/escape-html (truncate (or content "") window-size))
      (let [content-lower (str/lower-case content)
            query-lower   (str/lower-case query)
            ;; Does the keyword appear ANYWHERE in the full content?
            match-in-full (str/includes? content-lower query-lower)]
        (if (not match-in-full)
          ;; Keyword not in content (matched title only) — plain first 200 chars
          (hu/escape-html (truncate content window-size))
          ;; Keyword IS in content — take first 200 chars and highlight within
          (let [window       (subs content 0 (min window-size (count content)))
                win-lower    (str/lower-case window)
                qlen         (count query)]
            ;; Walk the first-200-char window finding all match positions
            (loop [pos   0
                   parts []]
              (if-let [m (str/index-of win-lower query-lower pos)]
                (let [before (subs window pos m)
                      match  (subs window m (+ m qlen))]
                  (recur (+ m qlen)
                         (conj parts
                               (hu/escape-html before)
                               [:mark.search-highlight (hu/escape-html match)])))
                (let [remaining (subs window pos)]
                  (conj parts (hu/escape-html remaining)))))))))))

;; ---------------------------------------------------------------------------
;; Layout shell
;; ---------------------------------------------------------------------------

(defn layout
  "Wrap content in the base HTML shell with mobile-first bottom-tab frame."
  [request & content]
  (let [uri (:uri request)]
    (str "<!DOCTYPE html>\n" (hiccup/html
      [:html {:lang "en"}
       [:head
        [:meta {:charset "utf-8"}]
        [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
        [:meta {:name "description" :content "Search and browse articles about Clojure and web development."}]
        [:title "Article Search"]
        [:link {:rel "stylesheet" :href "/css/style.css"}]]
       [:body
        [:div.app-shell
         ;; Main scrollable area
         [:main.app-main
          content]
         ;; Bottom tab bar
         [:nav.bottom-tabs
          [:a.tab {:href "/"       :class (if (#{"/" "/search"} uri) "active" "")}
           [:span.tab-icon "🔍"]
           [:span.tab-label "Search"]]
          [:a.tab {:href "/articles" :class (if (str/starts-with? uri "/articles") "active" "")}
           [:span.tab-icon "📚"]
           [:span.tab-label "Browse"]]]]]]))))

;; ---------------------------------------------------------------------------
;; Home page — search form + featured articles
;; ---------------------------------------------------------------------------

(defn home-page
  "Render the home/search page with search form and recent articles."
  [request recent-articles]
  (let [q (or (get-in request [:params :q]) "")]
    (layout request
      ;; Hero section
      [:header.hero
       [:h1.hero-title "Find the article you need"]
       [:p.hero-subtitle "Search across our collection of Clojure and web development articles."]
       ;; Search form
       [:form.search-form {:action "/search" :method "GET"}
        [:div.search-input-wrap
         [:input.search-input
          {:type "text" :name "q" :placeholder "Try \"Clojure\" or \"testing\"…"
           :value q :autofocus true :aria-label "Search articles"}]
         [:button.search-submit {:type "submit" :aria-label "Submit search"}
          "→"]]
        [:p.search-hint "e.g. \"Ring\", \"database\", \"functional programming\""]]]

      ;; Featured articles section
      [:section.featured
       [:div.section-head
        [:h2 "Recent articles"]
        [:a.text-link {:href "/articles"} "View all →"]]
       [:div.article-cards
        (for [a recent-articles]
          [:a.article-card {:href (str "/articles/" (:id a)) :key (:id a)}
           [:h3.card-title (:title a)]
           [:p.card-snippet (truncate (:content a) 140)]
           [:time.card-date (format-date (:created_at a))]])]]

      ;; Signature quote
      [:aside.testimonial
       [:span.quote-mark "“"]
       [:blockquote
        [:p "Code is read much more often than it is written, so plan accordingly."]
        [:cite "— Rich Hickey, creator of Clojure"]]])))

;; ---------------------------------------------------------------------------
;; Search results page
;; ---------------------------------------------------------------------------

(defn search-results-page
  "Render search results with highlighted snippets."
  [request query results]
  (layout request
    [:header.search-header
     [:a.search-back {:href "/"} "← Back"]
     [:h1.search-query (str "Results for \"" (hu/escape-html query) "\"")]
     [:p.search-count
      (let [n (count results)]
        (str n " article" (when (not= n 1) "s") " found"))]]

    ;; Search form (compact, for refinement)
    [:form.search-form.search-form-compact {:action "/search" :method "GET"}
     [:div.search-input-wrap
      [:input.search-input {:type "text" :name "q" :value query
                            :aria-label "Refine search"}]
      [:button.search-submit {:type "submit" :aria-label "Search"} "→"]]]

    [:section.search-results-list
     (if (empty? results)
       [:div.empty-state
        [:p.empty-icon "📭"]
        [:h2 "No articles found"]
        [:p (str "We couldn't find any articles matching \"" (hu/escape-html query) "\". "
                 "Try a different search term or browse all articles.")]
        [:a.btn {:href "/articles"} "Browse all articles"]]
       (for [a results]
         [:a.result-item {:href (str "/articles/" (:id a)) :key (:id a)}
          [:h3.result-title (:title a)]
          [:p.result-snippet (highlight-snippet (:content a) query)]
          [:time.result-date (format-date (:created_at a))]]))]))

;; ---------------------------------------------------------------------------
;; Article detail page
;; ---------------------------------------------------------------------------

(defn article-detail-page
  "Render a single article in full."
  [request article]
  (layout request
    (if (nil? article)
      [:div.empty-state
       [:p.empty-icon "📄"]
       [:h2 "Article not found"]
       [:p "The article you're looking for doesn't exist or may have been removed."]
       [:a.btn {:href "/articles"} "Browse articles"]]
      [:article.article-detail
       [:a.back-link {:href "/articles"} "← All articles"]
       [:header.article-header
        [:h1.article-title (:title article)]
        [:time.article-date (format-date (:created_at article))]]
       [:div.article-body
        ;; Render paragraphs, auto-escaped by Hiccup
        (for [para (str/split (:content article) #"\n\n+")]
          [:p para])]

       ;; Signature quote at article footer
       [:aside.testimonial.testimonial-inline
        [:span.quote-mark "“"]
        [:blockquote
         [:p "Simplicity is hard work. But there's a huge payoff: programs that are simple are easier to understand, change, and maintain."]
         [:cite "— Rich Hickey"]]]])))

;; ---------------------------------------------------------------------------
;; Browse all articles page
;; ---------------------------------------------------------------------------

(defn article-list-page
  "Render a browsable list of all articles."
  [request articles]
  (layout request
    [:header.browse-header
     [:h1 "All articles"]
     [:p.browse-count (str (count articles) " article" (when (not= (count articles) 1) "s"))]]

    [:section.article-list
     (for [a articles]
       [:a.list-item {:href (str "/articles/" (:id a)) :key (:id a)}
        [:div.list-item-content
         [:h3.list-item-title (:title a)]
         [:p.list-item-snippet (truncate (:content a) 160)]
         [:time.list-item-date (format-date (:created_at a))]]
        [:span.list-item-arrow "→"]])]

    ;; Signature quote
    [:aside.testimonial
     [:span.quote-mark "“"]
     [:blockquote
      [:p "The key to performance is elegance, not battalions of special cases."]
      [:cite "— Jon Bentley"]]]))

;; ---------------------------------------------------------------------------
;; Not found
;; ---------------------------------------------------------------------------

(defn not-found-page
  [request]
  (layout request
    [:div.empty-state
     [:p.empty-icon "🔍"]
     [:h2 "Page not found"]
     [:p "The page you're looking for doesn't exist."]
     [:a.btn {:href "/"} "Go home"]]))
