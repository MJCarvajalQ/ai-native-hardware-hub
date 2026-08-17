# Hardware Hub

Internal tool for managing, renting, and maintaining company equipment: admins
manage the inventory and accounts, users rent and return hardware, and a
Claude-powered "Ask AI…" search understands free-text condition notes that a
keyword filter can't.

## Stack

Java 17 + Spring Boot · React + Vite · SQLite (via Spring Data JPA) · Claude API

**Why Java/React instead of the brief's preferred Python/Vue:** the brief states
the team "value[s] results and tool proficiency over specific syntax." Java is my
strongest language, and I have prior production experience with React; SQLite and
the overall architecture (layered controller → service → repository, DTOs at the
boundary, server-enforced state machine) are identical to what a Python/FastAPI
version would look like. This was a deliberate tradeoff, not an oversight — see
`PROMPTS.md` for how the decision was actually made (including the reversals).

## Setup

Requires JDK 17+, Maven, and Node 18+.

**1. Backend**

```
cd backend
cp .env.example .env   # fill in ANTHROPIC_API_KEY, ADMIN_EMAIL, ADMIN_PASSWORD
set -a && source .env && set +a
mvn spring-boot:run
```

Boots on `http://localhost:8080`, creates `hardwarehub.db` (SQLite, gitignored),
seeds the 11 hardware records from `seed-data.json`, and bootstraps one admin
account from `ADMIN_EMAIL`/`ADMIN_PASSWORD`. Spring Boot does not read `.env`
files automatically — the `source .env` step is what actually gets the variables
into the process; without it the app still boots (nothing hardcodes a default
admin password, on purpose — see ⚡ below) but the AI search returns a friendly
502 instead of running.

**2. Frontend**

```
cd frontend
npm install
npm run dev
```

Serves on `http://localhost:5173` (Vite proxies nothing — it calls the backend
directly at `http://localhost:8080`, overridable via `VITE_API_BASE_URL`).

**3. Log in** with the admin account from step 1. There is no self-registration
anywhere in the app — creating additional accounts (admin or regular user) is the
one thing an admin does from the Admin Panel, matching the brief's requirement
that account creation is "the only way for someone to gain access to the Hub."

**Tests:** `cd backend && mvn test` — 18 tests (unit + MockMvc integration), all
green. CI (`.github/workflows/ci.yml`) runs the same on every push to `main`.

## Implementation Status

### ✅ Fully Implemented

- Admin Command Center (`/admin`, admin-only route): add hardware, delete
  hardware, toggle Repair status, create user accounts.
- Login screen; no self-registration route exists anywhere.
- Smart Dashboard: Name / Brand / Purchase Date / Status table, sortable columns
  (click to sort, click again to reverse), status and brand filters.
- Rent → In Use, Return → Available, both server-enforced with a real state
  machine (`HardwareService`) — the API rejects illegal transitions with 409,
  not just the UI graying out a button.
- AI-Native layer: **Semantic Search** via the Claude API (`claude-opus-5`),
  reachable from the dashboard's "Ask AI…" box. Ranks the inventory against the
  query using name, brand, status, **and free-text notes/history** — the notes
  field is what lets it catch a device marked `Available` that's actually unsafe
  to hand out (see the Data Strategy and Correction sections below).
- File-based database (SQLite), started from the provided seed JSON, loaded with
  every correction logged rather than hand-edited.
- 3 required critical tests (rent-while-REPAIR rejected, rent-while-IN_USE
  rejected, rent→return round-trip), AI-generated and verified — generating
  prompt in `PROMPTS.md`. Plus 15 more covering the normalizers, the API
  contract, and the search response parser's defensive handling of fenced and
  malformed JSON.
- Clean commit history: one short-lived branch per feature block, fast-forward
  merged into `main` (see the git-workflow entry in `PROMPTS.md` for why plain
  merge produces a linear history here).

### ⚡ Shortcuts & Hacks

Each of these was a deliberate choice under a 2-day deadline, not an unexamined
default.

| Shortcut | Why | Future |
|---|---|---|
| Opaque in-memory bearer tokens instead of JWT | Single-instance demo; avoids fiddly stateless-JWT + refresh-rotation config for a project that will never run more than one instance | Stateless JWT with refresh tokens and rotation, so the API can scale horizontally |
| Token stored in `localStorage` instead of an httpOnly cookie | Simpler to wire against a separate-origin Vite dev server in the time available | httpOnly + `SameSite` cookies with CSRF protection |
| Schema via Hibernate `ddl-auto=update` instead of Flyway/Liquibase | Single-developer demo with a disposable database | Versioned migrations before any real data exists |
| No frontend tests | Risk is concentrated in the rental state machine, which is fully covered server-side (`HardwareServiceTest`, `HardwareControllerIntegrationTest`) | Vitest + React Testing Library on the dashboard's status→disabled-button logic |
| No pull requests / code review | Solo project; the brief asks only for "a clean commit history," not a review process | PR-per-branch with required review and branch protection on `main` |
| `HardwareService.list()` filters/sorts in memory rather than a JPA `Specification` | 11 seed rows, no realistic growth for an internal tool this size — a proportionate choice, not laziness | A `Specification`-based query if the inventory ever grows past what fits comfortably in memory |

### ⚠️ Partial / Missing

- **My Rentals view, Serial Number/Category/Location fields, an Edit-device
  action, and the wireframe's full visual styling** are not implemented. All are
  wireframe extras or "plus" items, not literal task requirements — see
  "Wireframe deviations" below for the full reasoning on each.
- **Live verification of the Claude integration was deferred overnight** (no API
  key available in the build shell at 2am) and completed live this morning once
  a key was supplied — see `PROMPTS.md`'s "Correction" entry for the real bug
  that surfaced the moment it was tested against the live API.
- **No deployment.** The brief calls a live demo "a huge plus" but not a
  requirement; it's the first item in Next Steps below.

### 🔮 Next Steps (top 3, in order)

1. **Deploy** — backend to Railway/Fly (with a persistent volume for the SQLite
   file), frontend to Vercel. Highest grading value per hour once the MVP is
   solid, per the brief's own framing.
2. **My Rentals view** — the wireframe's second nav item; a simple filter of the
   existing dashboard data (`assignedTo == me`), no new backend work needed.
3. **Auth hardening** — replace opaque tokens with JWT + refresh, move the token
   to an httpOnly cookie with CSRF protection. Converts two of the ⚡ shortcuts
   above into ✅ items.

### Wireframe deviations

The brief explicitly invites remodeling the wireframe "with justification." What
changed and why:

- **Omitted:** Serial Number, Category, Location, Edit-device action, My Rentals
  view — none are task requirements (the task text lists Name/Brand/Purchase
  Date/Status and add/delete/repair-toggle explicitly; these are wireframe-only
  extras). All are cheap Part-2 increments if time allows.
- **Repair is a toggle, not a one-way wrench icon** — the task text says "toggle
  the 'Repair' status," which the wireframe's wrench icon doesn't actually show
  reversing. Implemented as a real toggle (`AVAILABLE ↔ REPAIR`) since that's what
  the words say, with `IN_USE → REPAIR` explicitly rejected (return it first).
- **Sorting and filtering are added** — the task text requires them ("must
  support sorting and filtering"); the wireframe's table doesn't show any control
  for either.
- **Status labels follow the wireframe's wording** (Available / In Use / Repair)
  while the internal enum (`AVAILABLE`/`IN_USE`/`REPAIR`) follows the seed data's
  own vocabulary — the two never had to match exactly, so I didn't force it.

## AI Development Log

### Tooling

Built entirely with **Claude Code** (Sonnet 5 for routine implementation, Opus 5
for planning, architecture decisions, and the trickier debugging sessions). No
MCP servers were used in the build itself.

### Data Strategy

The provided seed JSON was saved **verbatim** to `seed-data.json` and never
hand-edited — every fix happens in code, in `DataSeeder` and its two normalizer
helpers, and is logged at startup so the audit trail is reproducible by anyone
who runs the app. The stance: the seeder repairs **structural** blockers (things
that would crash a naive loader — a duplicate id, an unparseable date format);
**semantic** anomalies (a typo'd brand, a status the app doesn't recognize, a
future purchase date, hardware whose notes contradict its status) are preserved
and surfaced, not silently cleaned away, because they're exactly what the AI
search feature and this table exist to catch.

Actual seeder log from a fresh boot (`mvn spring-boot:run` against a deleted
`hardwarehub.db`):

```
seeding hardware from seed-data.json — data audit findings below
seed id 6: purchaseDate 2027-10-10 is in the future; loading it as-is
seed id 4 appears more than once in seed-data.json ('Duplicate ID Test Laptop');
  loading it anyway as its own record, since this app assigns its own database
  ids and never trusts seed-provided ids as primary keys
seed id 9: brand 'Appel' looks like a typo for 'Apple'; correcting it
seed id 9: purchaseDate '22-05-2023' was not in ISO format; normalized to 2023-05-22
seed id 10: brand is blank; leaving it blank
seed id 10: purchaseDate is null; leaving it null
seed id 10: unrecognized status: Unknown; defaulting status to REPAIR pending manual review
seed complete: 11 records loaded, 7 data-quality findings
```

| Planted issue | Handling | Kind |
|---|---|---|
| Duplicate `id: 4` | Reassigned a fresh database id; the original seed id is kept on the record purely for traceability (`Hardware.seedId`), never used as a primary key | Structural (auto-fixed) |
| `purchaseDate: "22-05-2023"` (DD-MM-YYYY) | Explicit multi-format parser recognizes it and normalizes to ISO | Structural (auto-fixed) |
| `purchaseDate: null` | Column is nullable; UI renders "—" | Structural (kept as-is by design) |
| `status: "Unknown"` | `StatusNormalizer` throws on anything unrecognized; `DataSeeder` catches it and defaults to `REPAIR` — never `AVAILABLE`, since unverified inventory must not be rentable — and logs the decision | Semantic (defaulted safely, logged) |
| `brand: ""` (blank) | Loaded as-is | Semantic (preserved, flagged) |
| `brand: "Appel"` | Corrected to `"Apple"`, logged | Semantic (corrected, logged) |
| `purchaseDate: "2027-10-10"` (future) | Loaded as-is | Semantic (preserved, flagged) |
| Dell XPS 15 (id 5) marked `Available` with notes *"Battery swelling, do not issue without service"* | Loaded as-is — the database says it's rentable; only the AI search catches that it shouldn't be | Semantic (preserved — this is the point) |
| `assignedTo: "j.doe@booksy.com"` on the Sony WH-1000XM4, no such user exists | Loaded as-is; no user lookup happens at seed time | Semantic (preserved, flagged) |

The Dell XPS row is the reason the AI Development Log's "Correction" story below
matters: it's the concrete case the search feature has to get right, and the one
that a plain `LIKE`/keyword filter structurally cannot.

### Prompt Trail

See [`PROMPTS.md`](./PROMPTS.md) — every prompt that shaped the architecture,
data model, state machine, or AI design, in order, including the reversals (the
AI-feature choice changed twice before landing on semantic search).

### The Correction

**Claude's own response format broke my Claude integration.** `ClaudeClient` (the
raw `HttpClient` wrapper around the Messages API) was written and unit-tested
overnight without a live API key, on the assumption that `response.content[0]`
is always the text answer. That assumption held for older, non-thinking models
but not for `claude-opus-5`, which **thinks by default**: `content[0]` is a
`"thinking"` block with no `text` field, and the actual answer is a later
`"text"` block. Jackson's `.path("text")` on a node with no such field returns an
empty string rather than throwing, so the client silently handed the parser `""`
— which the parser correctly rejected as "not a JSON array," producing a clean
502 rather than a crash, but the wrong result all the same.

**How it was caught:** the moment a real API key was supplied this morning, the
first end-to-end search call failed. I reproduced the exact prompt payload with a
raw `curl` against the Messages API directly — bypassing the Java client
entirely — and the response body showed two content blocks: `thinking` first,
`text` second. That's what confirmed the shape assumption was wrong before
touching any code.

**Fix:** `ClaudeClient` now scans the `content` array for the first block with
`"type": "text"` instead of trusting index 0. Verified against the live API with
both planned demo queries — *"something to test a mobile app on"* correctly
returns the phone/tablet cluster, and, the headline case, *"something safe to
give a new hire"* correctly **excludes the Dell XPS** with the battery-swelling
note even though its status is `Available`. That's the specific thing this
feature exists to do: a keyword/status filter would have handed that device to a
new hire; reading the note stopped it.

Full diagnostic trail — including an earlier, equally real 401-vs-403 bug in the
auth layer, caught via the same discipline of reproducing with `curl` and
reasoning from evidence rather than guessing — is in `PROMPTS.md`.

---

**Time spent:** approximately 10 hours of active work across two sessions
(Saturday afternoon/evening, Sunday morning), against the brief's 4–5 hour
estimate. The gap is almost entirely the MVP-first restructuring documented in
`PROMPTS.md` — building every stated requirement properly, with tests and CI,
rather than a faster but thinner pass.
