# Installation

## Prerequisites

- **Java 17+** (OpenJDK or Eclipse Temurin recommended)
- **Clojure CLI tools** (version 1.11.3+)

Check your toolchain:

```bash
java -version
clojure --version
```

## Steps

### 1. Clone and enter the project

```bash
cd article-search
```

### 2. Run the install script

```bash
bash install.sh
```

This script:
1. Creates the `data/` directory for the SQLite database
2. Downloads all Clojure dependencies via `clojure -A:run:seed -P`
3. Seeds the database with demo data (idempotent — safe to re-run)

The script **exits** when complete. It does **not** start the server.

### 3. Start the server

```bash
clojure -M:run
```

The server starts on `0.0.0.0:3000` (or `$PORT` if set).

### 4. Verify

Open **http://localhost:3000** in your browser. You should see the search page with recent articles listed.

## Re-seeding

The seed is idempotent. To re-run it independently:

```bash
clojure -M:seed
```

## Using PostgreSQL

By default the app uses embedded SQLite. To switch to PostgreSQL, set the `DATABASE_URL` environment variable:

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/article_search?user=myuser&password=mypass"
```

Then run the seed and start the server. The schema is created automatically on first boot.

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `clojure: command not found` | Install the Clojure CLI tools |
| `Could not locate ring/adapter/jetty` | Run `clojure -A:run:seed -P` to fetch deps |
| Port already in use | Set a different `PORT`, or kill the existing process |
| Empty database | Run `clojure -M:seed` to populate demo data |
