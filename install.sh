#!/usr/bin/env bash
set -eu

echo "==> Article Search — install"
cd "$(dirname "$0")"

# Ensure data directory for SQLite
mkdir -p data

# Verify toolchain
command -v clojure >/dev/null 2>&1 || { echo "MISSING: clojure CLI not found"; exit 1; }
command -v java >/dev/null 2>&1 || { echo "MISSING: java not found"; exit 1; }

echo "    Java: $(java -version 2>&1 | head -1)"
echo "    Clojure: $(clojure --version 2>&1)"

# Download all dependencies (non-interactive, prepare only)
echo "==> Resolving dependencies..."
clojure -A:run:seed -P

# Seed the database (idempotent)
echo "==> Seeding database..."
clojure -M:seed

echo ""
echo "==> Setup complete."
echo "    Start the server:  clojure -M:run"
echo "    Then open:         http://localhost:3000"
