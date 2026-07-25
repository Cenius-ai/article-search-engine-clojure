# Usage Guide

## Overview

Article Search is a single-page-style web application with a mobile-first bottom-tab layout. Two main tabs anchor the experience: **Search** and **Browse**.

## Screens

### Search (`/`)

The home screen. You'll see:

- A **search form** — type a keyword and press Enter or click the arrow button
- **Recent articles** — the six newest articles as cards; click any to read it

Try searching for: "Clojure", "database", "functional", "Ring", "testing"

### Search Results (`/search?q=…`)

After submitting a search:

- A **header** tells you how many articles matched your query
- Each result card shows the **title**, a **snippet** with your keyword **highlighted** in accent, and the **date**
- The search form stays visible so you can refine your query immediately

If no articles match, you'll see a friendly empty state suggesting you browse all articles.

### Browse All (`/articles`)

A scrollable list of every article, newest first. Each row shows the title, a brief snippet, and the date. Tap any article to read it in full.

### Article Detail (`/articles/:id`)

The full text of a single article, rendered as readable paragraphs. A back link returns you to the article list.

## Keyboard & accessibility

- **Tab** navigates between interactive elements
- **Enter** submits forms and follows links
- All interactive elements have visible focus rings
- The layout respects `prefers-reduced-motion`

## JSON API

For programmatic access, use the search API:

```
GET /api/search?q=immutability
```

Returns:

```json
{
  "query": "immutability",
  "results": [
    {
      "id": "a0000011-…",
      "title": "Concurrency in Clojure: Atoms, Refs, and Agents",
      "snippet": "Concurrency is hard in most languages because…",
      "created_at": "2025-04-08T11:30:00Z"
    }
  ]
}
```

The `snippet` is the first 200 characters of the article content. For HTML-rendered highlights, use the `/search` page instead.
