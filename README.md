# Article Search Engine — Clojure search engine app reference implementation

**Article Search Engine** is a free, open-source search engine app built with Clojure. A minimal Clojure web application using Ring and Compojure that allows visitors to search a list of articles by keyword, backed by PostgreSQL. Run it locally, deploy it as a self-hosted search engine app, or [remix it on cenius.ai](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure) to make it your own — the whole application (code, design, seeded demo data) ships in this repository under the Apache-2.0 license.

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE) ![Stack](https://img.shields.io/badge/Stack-Clojure-3b82f6) [![Built with cenius.ai](https://img.shields.io/badge/Built%20with-cenius.ai-8b5cf6)](https://cenius.ai)

[![Open in cenius.ai](https://img.shields.io/badge/▶%20Open%20%26%20edit%20in-cenius.ai-8b5cf6?style=for-the-badge)](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure)

> **▶ [Open & edit in cenius.ai](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure)** — one click to an editable workspace: describe changes in plain English, get an instant preview, one-click deploy and host. Modifications made on the platform come with full rebrand & relicense rights.

_Local clone? See [Quick start](#quick-start) below. cenius.ai is the zero-setup path._

## Demo

![Article Search Engine — search engine app](.github/media/poster.png)

![Article Search Engine demo — search engine app built with Clojure](.github/media/hero_flagship.gif)

▶ **[Watch the full demo video](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure)** — the complete walkthrough, playing on the project's cenius.ai page · [MP4 file](.github/media/demo.mp4)

## Screenshots

<img src=".github/media/shot-1.png" width="32%" alt="Article Search Engine search engine app screenshot 1"/> <img src=".github/media/shot-2.png" width="32%" alt="Article Search Engine search engine app screenshot 2"/> <img src=".github/media/shot-3.png" width="32%" alt="Article Search Engine search engine app screenshot 3"/>

## Features

- Keyword article search

## Quick start

```bash
./install.sh   # installs dependencies + seeds demo data
```

See [`INSTALL.md`](INSTALL.md) for full setup and usage instructions.

## Usage guide

### Overview

Article Search is a single-page-style web application with a mobile-first bottom-tab layout. Two main tabs anchor the experience: **Search** and **Browse**.

### Screens

#### Search (`/`)

The home screen. You'll see:

- A **search form** — type a keyword and press Enter or click the arrow button
- **Recent articles** — the six newest articles as cards; click any to read it

Try searching for: "Clojure", "database", "functional", "Ring", "testing"

#### Search Results (`/search?q=…`)

After submitting a search:

- A **header** tells you how many articles matched your query
- Each result card shows the **title**, a **snippet** with your keyword **highlighted** in accent, and the **date**
- The search form stays visible so you can refine your query immediately

If no articles match, you'll see a friendly empty state suggesting you browse all articles.

#### Browse All (`/articles`)

A scrollable list of every article, newest first. Each row shows the title, a brief snippet, and the date. Tap any article to read it in full.

#### Article Detail (`/articles/:id`)

The full text of a single article, rendered as readable paragraphs. A back link returns you to the article list.

### Keyboard & accessibility

- **Tab** navigates between interactive elements
- **Enter** submits forms and follows links
- All interactive elements have visible focus rings
- The layout respects `prefers-reduced-motion`

### JSON API

For programmatic access, use the search API:

```
GET /api/search?q=immutability
```

Returns:

_Full guide: [`USAGE.md`](USAGE.md)_

## Architecture

Clojure application, delivered as a complete, runnable project (29 files). Top-level layout: `data/`, `resources/`, `src/`, `test/`. `install.sh` provisions dependencies and seeds demo data, so the app boots with something to show. Setup details live in [`INSTALL.md`](INSTALL.md).

## FAQ

### How do I self-host Article Search Engine?

Clone this repository and run `./install.sh`, then start the app as described in [`INSTALL.md`](INSTALL.md). Article Search Engine is fully self-hostable — no external services are required to try it.

### What is Article Search Engine built with?

Clojure. The full source in this repository is exactly what the app runs. Highlights include keyword article search.

### Can I use Article Search Engine in a commercial project?

Yes. The code is Apache-2.0-licensed — use it, modify it, and ship it commercially. See [LICENSE](LICENSE).

### How do I make Article Search Engine my own brand?

Absolutely. [Open it on cenius.ai](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure) and remix it there — platform modifications come with full rebrand and relicense rights over your derivative, so the result is entirely yours.

### Can I change Article Search Engine without writing code?

Open it on [cenius.ai](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure) and describe the changes you want in plain English — the platform modifies the app and gives you a new, downloadable build.

## License & rebranding

Released under the [Apache License 2.0](LICENSE) (© 2026 Cenius AI) — free for personal and commercial use. The Cenius name/logo are trademarks (see NOTICE).

**Need a customized version?** [Remix this app on cenius.ai](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure) — modifications made on the platform come with **full rebrand & relicense rights** over your derivative.

## Built with cenius.ai

This entire application — code, design, seeded demo data — was generated on **[cenius.ai](https://cenius.ai)** from a plain-English description.

- 🚀 [Build your own app on cenius.ai](https://cenius.ai)
- 🎛️ [Remix Article Search Engine on the marketplace](https://cenius.ai/marketplace/p/article-search-engine?ref=gh&utm_campaign=article-search-engine-clojure) — open it in a workspace, prompt for changes, and ship your own version.

More open-source apps: [the Cenius-ai catalog](https://github.com/Cenius-ai) · [showcase index](https://github.com/Cenius-ai/showcase)
