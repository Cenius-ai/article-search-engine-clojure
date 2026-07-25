(ns article-search.seed
  "Idempotent database seeding: demo user + 12 demo articles.
   Run standalone:  clojure -M:seed
   Called from core on first boot when DB is empty."
  (:require [article-search.db :as db]
            [buddy.hashers :as hashers]))

;; ---------------------------------------------------------------------------
;; Demo user
;; ---------------------------------------------------------------------------

(def demo-user-id   "00000000-0000-0000-0000-000000000001")
(def demo-username  "cenius")
(def demo-email     "cenius@cenius.ai")
(def demo-password  "cenius")

;; ---------------------------------------------------------------------------
;; Demo articles — 12 real pieces with 300+ word bodies
;; ---------------------------------------------------------------------------

(def articles
  [{:id "a0000001-0000-0000-0000-000000000001"
    :title "Getting Started with Clojure: A Practical Guide"
    :content (str
      "Clojure is a modern, functional dialect of Lisp that runs on the Java Virtual Machine. "
      "It was designed by Rich Hickey and first released in 2007, with a focus on simplicity, "
      "immutability, and pragmatic concurrency support. Unlike many languages that bolt on "
      "functional features after the fact, Clojure was built from the ground up to encourage "
      "a functional programming style.\n\n"

      "The language's syntax is minimal — everything is either data or a function call. "
      "Clojure's parentheses-based S-expression syntax may look unfamiliar at first, but it "
      "provides a consistent, homoiconic structure where code is data and data can be code. "
      "This property enables powerful metaprogramming through macros, which allow you to extend "
      "the language itself.\n\n"

      "One of Clojure's standout features is its approach to state management. Rather than "
      "mutating variables in place, Clojure provides persistent, immutable data structures "
      "and reference types — atoms, refs, agents, and vars — that give you controlled, "
      "coordinated access to changing state. This makes concurrent programming dramatically "
      "simpler because you never have to worry about one thread corrupting another's data.\n\n"

      "Clojure also shines in its Java interop story. Because it runs on the JVM, you can "
      "call any Java library directly from Clojure code with minimal ceremony. This means "
      "you have access to the entire Java ecosystem — from database drivers to machine "
      "learning libraries — while writing idiomatic functional code. Getting started is as "
      "simple as installing the Clojure CLI tools and creating a deps.edn file to manage "
      "your dependencies. The REPL-driven development workflow, where you build your program "
      "incrementally by evaluating forms in a running environment, is one of the most "
      "productive ways to write software once you experience it.")
    :created_at "2025-01-15T10:00:00Z"}

   {:id "a0000002-0000-0000-0000-000000000002"
    :title "Understanding Ring: The Clojure Web Server Foundation"
    :content (str
      "Ring is the foundational HTTP abstraction library for Clojure web applications. "
      "It defines a simple, elegant protocol: an HTTP request is a Clojure map, and an "
      "HTTP response is also a Clojure map. This deceptively simple idea means that your "
      "entire application becomes a function from request map to response map.\n\n"

      "A Ring handler is just a function that takes one argument — the request map — and "
      "returns a response map. The request map contains keys like :uri, :request-method, "
      ":headers, :params, and :body. The response map must contain at minimum :status, "
      ":headers, and :body. This uniform interface means that middleware — functions that "
      "wrap handlers to add cross-cutting behavior — can be composed trivially.\n\n"

      "Ring middleware follows the decorator pattern. A middleware function takes a handler "
      "and returns a new handler that does something extra: logging requests, parsing "
      "parameters, managing sessions, handling cookies, or adding security headers. You "
      "compose these by nesting function calls, building up a pipeline that processes "
      "each request before it reaches your application logic.\n\n"

      "Ring adapters bridge the gap between the Ring specification and actual HTTP servers. "
      "The most common adapter is ring-jetty-adapter, which embeds a Jetty server inside "
      "your application. This means you can run your web app as a standalone process without "
      "needing an external servlet container. The adapter handles the messy details of "
      "socket management and HTTP parsing, letting you focus on your application's behavior. "
      "To start a Ring server, you typically call run-jetty with your handler and a port "
      "number, and Jetty takes care of the rest. Understanding Ring's request/response model "
      "is the key to building any Clojure web application.")
    :created_at "2025-01-20T14:30:00Z"}

   {:id "a0000003-0000-0000-0000-000000000003"
    :title "Compojure Routing: Clean URLs in Clojure Web Apps"
    :content (str
      "Compojure is a small, focused routing library for Ring-based Clojure web applications. "
      "It provides a concise DSL for mapping HTTP methods and URL patterns to handler "
      "functions. The core idea is that you define routes using macros like GET, POST, PUT, "
      "and DELETE, each of which takes a URL pattern and a handler body.\n\n"

      "Route patterns in Compojure can include named parameters using curly-brace syntax: "
      "\"/users/{id}\" captures the id segment and makes it available in the handler. You "
      "can also use regular expressions for more complex matching. Compojure compiles these "
      "patterns into efficient matching functions, so routing overhead is minimal even in "
      "applications with dozens of routes.\n\n"

      "Compojure's routing philosophy is about composition. The routes function combines "
      "multiple route definitions into a single Ring handler. The defroutes macro gives "
      "you a convenient way to define a set of routes as a named var. You can also use "
      "context to group routes under a common path prefix, which is especially useful for "
      "API versioning or admin sections.\n\n"

      "One of Compojure's strengths is that it doesn't try to be a full-stack framework. "
      "It handles routing and leaves concerns like templating, database access, and "
      "authentication to other libraries. This modularity means you can pick the best "
      "tools for each job. A typical Compojure application combines Ring for HTTP "
      "abstraction, Compojure for routing, Hiccup for HTML generation, and next.jdbc "
      "for database access — each library doing one thing well. Error handling is built "
      "in through the try-catch semantics of Clojure, and you can return appropriate "
      "HTTP status codes directly from your handlers.")
    :created_at "2025-02-01T09:15:00Z"}

   {:id "a0000004-0000-0000-0000-000000000004"
    :title "Hiccup Templating: HTML as Clojure Data Structures"
    :content (str
      "Hiccup is a Clojure library that lets you represent HTML as Clojure data structures. "
      "Instead of mixing code into template files with special syntax, you write HTML as "
      "nested vectors and maps — regular Clojure data that your editor already understands. "
      "This approach, sometimes called 'code-as-template,' eliminates the cognitive overhead "
      "of switching between languages and gives you the full power of Clojure for composition "
      "and abstraction.\n\n"

      "The basic Hiccup form is a vector where the first element is a keyword representing "
      "the HTML tag name, the second element is an optional map of attributes, and the "
      "remaining elements are child content. For example, [:div {:class \"container\"} "
      "[:h1 \"Hello\"]] renders as <div class=\"container\"><h1>Hello</h1></div>. This "
      "syntax is both compact and readable, and since it's just Clojure data, you can "
      "use all of Clojure's sequence manipulation functions to build your pages.\n\n"

      "Hiccup automatically escapes HTML in string content, which prevents cross-site "
      "scripting attacks by default. If you need to insert raw HTML — for example, "
      "rendering sanitized markdown — you use the hiccup.util/raw function explicitly. "
      "This opt-in approach to unescaped content means that security is the default and "
      "unsafe behavior requires a deliberate choice.\n\n"

      "For larger applications, you can define reusable component functions that return "
      "Hiccup vectors. A navigation bar, a footer, a card component — each can be a "
      "plain Clojure function that takes data and returns markup. Because these are "
      "functions, not templates, you get parameter validation, default values, and "
      "composability for free. Hiccup is particularly well-suited to server-rendered "
      "applications where you want to keep the full rendering pipeline in Clojure "
      "without introducing a separate template language.")
    :created_at "2025-02-10T11:45:00Z"}

   {:id "a0000005-0000-0000-0000-000000000005"
    :title "Database Access in Clojure with next.jdbc"
    :content (str
      "next.jdbc is the modern Clojure library for database access, succeeding the older "
      "clojure.java.jdbc. It provides a clean, consistent API for executing SQL queries "
      "and processing results. Unlike ORMs that try to hide SQL behind object abstractions, "
      "next.jdbc embraces SQL as a first-class concern while making the Clojure-side "
      "ergonomics excellent.\n\n"

      "The library is built around a few core concepts. A datasource represents a connection "
      "to your database. The execute! function runs a SQL statement and returns the results "
      "as Clojure data structures. Queries use parameterized SQL vectors — for example, "
      "[\"SELECT * FROM users WHERE id = ?\" user-id] — which completely prevent SQL "
      "injection attacks because the parameters are never interpolated into the SQL string.\n\n"

      "next.jdbc supports both plan-based and result-set-based processing. Plan mode gives "
      "you a reducible sequence of rows, which is memory-efficient for large result sets. "
      "The default mode returns results as vectors of maps, where each map represents a row "
      "with keys as lowercase keywords. This integrates naturally with Clojure's data "
      "manipulation functions, so you can filter, map, reduce, and transform query results "
      "using the standard library.\n\n"

      "For SQLite users, next.jdbc works seamlessly with the org.xerial/sqlite-jdbc driver. "
      "The setup is minimal: add the dependency, create a datasource pointing at your .db "
      "file, and you're ready to query. SQLite's embedded nature makes it perfect for "
      "development and small-to-medium deployments. For production with PostgreSQL, you "
      "simply swap the JDBC URL and driver — the next.jdbc API stays the same. This "
      "portability means you can develop locally with SQLite and deploy to PostgreSQL "
      "without changing your database access code.")
    :created_at "2025-02-18T08:00:00Z"}

   {:id "a0000006-0000-0000-0000-000000000006"
    :title "Building RESTful APIs with Clojure and Ring"
    :content (str
      "RESTful APIs are the backbone of modern web applications, and Clojure's Ring/Compojure "
      "stack provides an exceptionally clean foundation for building them. The request/response "
      "map model maps naturally to HTTP semantics: you receive a request map containing the "
      "method, URI, headers, and body, and you return a response map with a status code, "
      "headers, and body.\n\n"

      "JSON serialization is handled by libraries like Cheshire, which can encode and decode "
      "Clojure data structures to and from JSON efficiently. A typical API handler fetches "
      "data from the database, transforms it into the desired shape, and returns it as a "
      "JSON response with the appropriate content type header. The separation between data "
      "fetching, transformation, and serialization keeps each concern testable in isolation.\n\n"

      "Content negotiation is straightforward with Ring middleware. You can inspect the "
      "Accept header from the request and choose between HTML, JSON, or other formats. "
      "For APIs that need to support multiple versions, Compojure's context macro lets "
      "you prefix routes with version identifiers like \"/v1\" or \"/v2\". Error responses "
      "should follow a consistent format — a map with :error and :message keys — so that "
      "API consumers can handle failures predictably.\n\n"

      "Authentication for APIs typically uses token-based approaches rather than sessions. "
      "You can implement a middleware that extracts a bearer token from the Authorization "
      "header, validates it, and attaches user information to the request map before it "
      "reaches your handlers. Since Ring middleware composes cleanly, you can apply "
      "authentication to some routes and leave others public. Rate limiting, request "
      "logging, and CORS handling are all middleware concerns that can be added without "
      "touching your business logic.")
    :created_at "2025-03-01T16:20:00Z"}

   {:id "a0000007-0000-0000-0000-000000000007"
    :title "Testing Strategies for Clojure Web Applications"
    :content (str
      "Testing is a first-class concern in Clojure development, and the language's emphasis "
      "on pure functions makes unit testing straightforward. When your business logic is "
      "expressed as functions that take data and return data, without side effects, you can "
      "test them by calling them with known inputs and asserting on the outputs. The built-in "
      "clojure.test library provides everything you need for basic assertions and test "
      "organization.\n\n"

      "For integration testing of Ring handlers, you can call your handler function directly "
      "with constructed request maps. This avoids the overhead of starting an actual HTTP "
      "server in tests. Create a request map with the appropriate :uri, :request-method, "
      "and :params keys, pass it to your handler, and examine the response map. This approach "
      "gives you fast, deterministic tests that exercise your full routing and middleware "
      "stack.\n\n"

      "Database testing requires a bit more care. The standard approach is to use a separate "
      "test database — for SQLite, you can point at a different file or use an in-memory "
      "database. Before each test, you create the schema and seed any required reference "
      "data. After each test, you clean up. Fixtures in clojure.test help manage this "
      "setup and teardown. For more complex scenarios, you can use transactions that roll "
      "back after each test, ensuring tests don't interfere with each other.\n\n"

      "End-to-end testing can be done by starting your application on a random port and "
      "making actual HTTP requests. Libraries like clj-http make this simple from Clojure. "
      "You can also use the Ring mock library to test handlers without a running server. "
      "The key insight is that Clojure's functional nature means fewer moving parts to mock "
      "or stub — you test real functions with real data, and the tests run fast.")
    :created_at "2025-03-10T13:00:00Z"}

   {:id "a0000008-0000-0000-0000-000000000008"
    :title "Functional Programming Patterns Every Developer Should Know"
    :content (str
      "Functional programming is more than just using map and filter — it's a fundamentally "
      "different way of thinking about computation. At its core, functional programming "
      "treats computation as the evaluation of mathematical functions and avoids changing "
      "state and mutable data. This leads to programs that are easier to reason about, "
      "test, and parallelize.\n\n"

      "Immutability is the cornerstone pattern. Instead of modifying data in place, you "
      "create new versions of data structures. Clojure's persistent data structures make "
      "this efficient by sharing structure between versions. When you 'add' an element to "
      "a vector, you get a new vector that shares most of its internal structure with the "
      "original. This gives you the safety of immutability without the performance penalty "
      "of copying everything.\n\n"

      "Higher-order functions — functions that take other functions as arguments or return "
      "them — are another essential pattern. Map, filter, and reduce are the classic examples, "
      "but the pattern extends to middleware, handlers, and strategy selection. In Clojure, "
      "you'll often see functions like comp (composition) and partial (partial application) "
      "used to build new functions from existing ones, creating pipelines of data "
      "transformation.\n\n"

      "Recursion replaces loops in functional programming. Clojure provides loop/recur for "
      "efficient tail-recursive iteration without consuming stack space. The sequence "
      "abstraction unifies how you work with lists, vectors, maps, and sets — you can use "
      "the same functions (first, rest, cons) across all of them. Combined with lazy "
      "sequences, which compute elements only when needed, you can process datasets that "
      "wouldn't fit in memory while writing code that looks like it's working with a simple "
      "list.")
    :created_at "2025-03-15T07:30:00Z"}

   {:id "a0000009-0000-0000-0000-000000000009"
    :title "Data Transformation with Clojure's Sequence Library"
    :content (str
      "Clojure's sequence library is one of its most powerful features, providing a rich "
      "set of functions for transforming, filtering, and aggregating data. Because the "
      "library is built around a common sequence abstraction, you can chain operations "
      "together without worrying about the underlying data structure — lists, vectors, "
      "maps, and sets all participate in the sequence protocol.\n\n"

      "The core transformation functions — map, filter, reduce, take, drop, partition — "
      "form the foundation. Map applies a function to every element and returns a sequence "
      "of results. Filter selects elements that satisfy a predicate. Reduce combines "
      "elements into a single value. What makes Clojure's versions special is that they "
      "return lazy sequences by default, so chaining map-filter-map doesn't create "
      "intermediate collections.\n\n"

      "Threading macros — the arrow (->) and the arrow-last (->>) — make data transformation "
      "pipelines readable by eliminating deep nesting. Instead of writing (c (b (a x))), "
      "you write (-> x a b c), which reads left-to-right and top-to-bottom. The ->> variant "
      "threads the expression as the last argument, which is how most sequence functions "
      "expect their collection argument. These macros transform how you think about data "
      "flow.\n\n"

      "For complex transformations, functions like group-by, sort-by, juxt, and update-in "
      "give you precise control. Group-by partitions a collection by a key function, sort-by "
      "orders elements, juxt applies multiple functions and returns a vector of results, "
      "and update-in modifies a nested value in an associative structure. Together, these "
      "tools let you express data transformations declaratively — you describe what you "
      "want, not how to compute it step by step.")
    :created_at "2025-03-22T10:10:00Z"}

   {:id "a0000010-0000-0000-0000-000000000010"
    :title "Deploying Clojure Applications: From REPL to Production"
    :content (str
      "Getting a Clojure application running on your machine is one thing — deploying it "
      "to production is another. The good news is that Clojure's JVM heritage gives you "
      "access to decades of deployment tooling and infrastructure. The most common deployment "
      "pattern is to build an uberjar — a single JAR file containing your application and "
      "all its dependencies — and run it with java -jar.\n\n"

      "Building an uberjar is straightforward with the Clojure CLI tools. The :uberjar "
      "alias in deps.edn specifies your main namespace, and a single command compiles "
      "everything into a self-contained artifact. This uberjar can be deployed anywhere "
      "Java runs: a VPS, a container, a Platform-as-a-Service provider, or even a bare-metal "
      "server. The same artifact that passed testing is the one that goes to production, "
      "eliminating a whole class of deployment surprises.\n\n"

      "Configuration management follows the 12-factor app philosophy: environment variables "
      "for deployment-specific settings, with sensible defaults for development. Database "
      "URLs, API keys, and feature flags all come from the environment, not from files "
      "that might accidentally be committed. Clojure's System/getenv and System/getProperty "
      "make reading configuration trivial, and libraries like environ provide convenience "
      "wrappers.\n\n"

      "For containerized deployments, Clojure applications work well with Docker. The "
      "Dockerfile typically starts from a slim JRE base image, copies the uberjar, and "
      "sets the entrypoint. Since the uberjar is self-contained, the image stays small "
      "and the build is reproducible. Monitoring and observability can be added through "
      "JMX or by exposing metrics endpoints that Prometheus can scrape, giving you "
      "visibility into application health, request rates, and error counts in production.")
    :created_at "2025-04-01T09:00:00Z"}

   {:id "a0000011-0000-0000-0000-000000000011"
    :title "Concurrency in Clojure: Atoms, Refs, and Agents"
    :content (str
      "Concurrency is hard in most languages because shared mutable state requires careful "
      "coordination to avoid race conditions, deadlocks, and data corruption. Clojure takes "
      "a fundamentally different approach: it provides immutable data structures by default "
      "and a set of controlled reference types for managing the identity of values that "
      "change over time.\n\n"

      "Atoms are the simplest reference type. An atom holds a single value and provides "
      "atomic compare-and-swap semantics. You read an atom's value with deref (or the @ "
      "reader macro) and update it with swap! or reset!. Swap! takes a function that "
      "transforms the current value — if another thread modified the atom between your "
      "read and your write, swap! retries automatically. This guarantees consistency "
      "without locks.\n\n"

      "Refs and software transactional memory (STM) handle coordinated changes across "
      "multiple references. When you modify refs inside a dosync block, Clojure ensures "
      "that all the changes are atomic, consistent, and isolated — the ACI properties of "
      "ACID. If two transactions conflict, one retries. This is more sophisticated than "
      "atoms and is appropriate when you need to update multiple pieces of state together, "
      "like transferring money between two bank accounts.\n\n"

      "Agents provide asynchronous, independent state changes. You send a function to an "
      "agent, and it applies the function to the agent's value in a separate thread pool. "
      "Agents are useful for tasks that don't need immediate consistency, like logging, "
      "metrics collection, or sending notifications. Since each agent processes its actions "
      "sequentially, you don't need to worry about race conditions within a single agent. "
      "Together, these reference types give you a complete toolkit for managing state in "
      "concurrent programs without the complexity of manual locking.")
    :created_at "2025-04-08T11:30:00Z"}

   {:id "a0000012-0000-0000-0000-000000000012"
    :title "The REPL-Driven Development Workflow in Clojure"
    :content (str
      "REPL-driven development is the secret weapon of Clojure programmers. Unlike the "
      "traditional edit-compile-run cycle, where you make changes, rebuild, and restart "
      "your application to see the effects, REPL-driven development keeps a running "
      "environment alive while you modify it incrementally. You evaluate individual "
      "expressions, test functions with real data, and build your program piece by piece "
      "without ever restarting.\n\n"

      "The workflow starts by launching a REPL connected to your project. Most Clojure "
      "editors — Emacs with CIDER, VS Code with Calva, IntelliJ with Cursive — support "
      "this natively. You open your source file, place your cursor on a function "
      "definition, and send it to the REPL with a keystroke. The function is now live "
      "in the running environment, and you can call it immediately to see if it works "
      "as expected.\n\n"

      "This tight feedback loop changes how you approach problems. Instead of writing "
      "a large block of code and hoping it works, you build bottom-up: define a data "
      "structure, inspect it, write a transformation, test it, compose them, test again. "
      "The REPL becomes an interactive laboratory where you experiment with your data "
      "and algorithms in real time. When something doesn't work, you see it immediately "
      "and fix it before moving on.\n\n"

      "For web applications, you can start your Ring server from the REPL, make changes "
      "to handlers or middleware, re-evaluate the changed functions, and see the effects "
      "on the next HTTP request — all without restarting the server. Combined with "
      "Clojure's immutability, which means evaluating a function definition doesn't "
      "corrupt existing state, this creates a development experience where the feedback "
      "loop is measured in seconds rather than minutes. Once you've experienced it, "
      "going back to edit-compile-run feels like programming with one hand tied behind "
      "your back.")
    :created_at "2025-04-12T15:00:00Z"}])

;; ---------------------------------------------------------------------------
;; Seed logic
;; ---------------------------------------------------------------------------

(defn seed!
  "Idempotent: inserts demo user and articles only if they don't already exist."
  []
  (db/init-db!)

  ;; Demo user — check before insert
  (when (zero? (db/user-count))
    (println "   Creating demo user:" demo-username)
    (let [pw-hash (hashers/derive demo-password)]
      (db/insert-user! demo-user-id demo-username pw-hash)))

  ;; Articles — only seed when table is empty
  (when (zero? (db/article-count))
    (println (str "   Seeding " (count articles) " demo articles..."))
    (doseq [a articles]
      (db/insert-article!
        (:id a)
        (:title a)
        (:content a)
        demo-user-id
        (:created_at a)))
    (println "   Done."))

  (println (str "   DB: " (db/user-count) " users, " (db/article-count) " articles"))
  :ok)

(defn -main
  "Seed the database and exit."
  [& _args]
  (println "==> Seeding database (idempotent)...")
  (seed!)
  (println "==> Seed complete.")
  (System/exit 0))
