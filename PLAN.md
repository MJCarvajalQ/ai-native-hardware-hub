# Build Plan (summary)

This is a condensed version of the planning document that shaped this project —
the full block-by-block plan Claude Code and I worked out together before writing
any code, kept here because it's genuinely the skeleton the rest of the repo was
built against, not just a prompt that led somewhere else. See
[`PROMPTS.md`](./PROMPTS.md) for the actual prompts that produced it.

## Strategy: MVP first

Build everything the brief explicitly requires — and nothing more — as a complete,
working whole. Only after that works end-to-end does a numbered "Part 2" increment
roadmap get attempted (deployment, My Rentals, extra fields, etc.), one increment
at a time, each one independently shippable. Every increment boundary is a valid
submission point on its own.

## Stack (locked upfront, justified in the README)

- **Backend:** Java 17 + Spring Boot.
- **Frontend:** React + Vite, minimal hand-rolled CSS, no component library.
- **DB:** SQLite via Spring Data JPA, with a documented fallback to H2 file-mode
  if the SQLite dialect wiring became a time sink (it didn't).
- **AI:** Claude API via a small raw `HttpClient` client — no SDK dependency for
  one call.

## Engineering practices — what's followed and what's deliberately skipped

**Followed:** layered separation (controller → service → repository), DTOs at the
API boundary, domain exceptions mapped to real HTTP status codes via a global
handler, server-side enforcement of every business rule (the UI just mirrors it),
secrets from env vars only, Conventional Commit messages, one short-lived branch
per build block merged back into `main`, CI running the test suite on every push,
tests at two levels (service-layer unit tests + MockMvc integration tests).

**Deliberately skipped, each declared as a ⚡ shortcut in the README with a Why
and a Future:** Hibernate `ddl-auto` instead of migrations, opaque in-memory
tokens instead of JWT, the token in `localStorage` instead of an httpOnly cookie,
no frontend tests, no pull requests.

## Data model & rental state machine

`Hardware` — id, name, brand, purchaseDate (nullable), status
(`AVAILABLE`/`IN_USE`/`REPAIR`), notes, history, assignedTo. `User` — id, email,
BCrypt password hash, role.

All transitions live in `HardwareService`, enforced server-side:
- `rent` requires `AVAILABLE` → `IN_USE`, sets `assignedTo`.
- `returnItem` requires `IN_USE` and the caller to be the assignee → `AVAILABLE`.
- `toggleRepair` flips `AVAILABLE ↔ REPAIR`; `IN_USE → REPAIR` is rejected —
  return it first. A deliberate rule, not an oversight.

## Auth (deliberately simple, two documented shortcuts)

Spring Security only for the BCrypt `PasswordEncoder`. A custom
`OncePerRequestFilter` resolves an opaque bearer token to the current user.
Admin-only endpoints check the role. The initial admin account bootstraps from
env config — never hardcoded, never logged in plaintext. There is no
self-registration route anywhere; account creation is admin-only, matching the
brief's requirement that it's "the only way to gain access."

## AI layer — Semantic Search

One Claude call per search: the query plus the full inventory (id, name, brand,
status, **and free-text notes/history** — not optional, not trimmed for token
savings) goes in; a strict JSON array of `{id, reason}` ranked by relevance comes
back. The `notes`/`history` fields are the entire point — ranking by name/brand is
a `LIKE` query; catching "battery swelling, do not issue" on a device marked
Available and excluding it from a "safe for a new hire" query is real language
understanding a keyword filter cannot do. Response parsing is defensive: strips
code fences, validates shape, drops unknown ids, never crashes on malformed
output.

## Seed-data audit strategy

The provided seed JSON is saved verbatim and never hand-edited. The seeder fixes
**structural** blockers only (a duplicate id, an unparseable date format);
**semantic** anomalies (a typo'd brand, an unrecognized status, a future
purchase date, hardware whose notes contradict its status) are preserved and
logged, not silently cleaned away — they're exactly what the AI search feature
and the README's Data Strategy table exist to catch.

## Build blocks (the actual skeleton)

Each block: branch → small verified steps (one at a time, never batched) →
commit → merge into `main`.

| Block | What |
|---|---|
| A | Repo skeleton, seed data saved verbatim, `.env.example` |
| B | Spring Boot + SQLite boots |
| C | Hardware domain model |
| D | Audited seeder (date/status normalizers, logged corrections) |
| E | Rental guards + the 3 required critical tests |
| F / F′ | REST API + MockMvc contract tests + CI |
| G | Users, auth, role-gated endpoints |
| H | Frontend shell + login |
| I | Dashboard: sort, filter, rent/return |
| J | Admin Command Center |
| K | Claude semantic search |
| L | README + prompt trail + submission |

## Prompt-trail methodology

Decided early that `PROMPTS.md` would be curated, not a raw transcript dump —
real prompts, including the messy or reversed ones, but only the ones that
actually shaped a decision. Drafted after every block while the exchange was
fresh, reviewed as a whole, and deliberately kept out of git (`.gitignore`) until
a final review pass right before submission — accuracy mattered more than being
committed from block one.

## Requirements checklist

Used throughout the build to keep the MVP honest against the brief — every item
below maps to something the finished repo actually does, checked one by one
before submission:

- [x] Admin Command Center (`/admin`, admin-only): add / delete / toggle Repair
- [x] Admin creates user accounts; account creation is the only way to gain access — no self-registration route exists
- [x] Login screen; only admin-created users can log in
- [x] Dashboard: Name / Brand / Purchase Date / Status, sortable, filterable
- [x] Rent → In Use, Return → Available
- [x] Guards prevent impossible states (409 on illegal transitions)
- [x] AI-Native layer: Semantic Search via Claude
- [x] File-based DB (SQLite), started from the provided JSON seed
- [x] Non-preferred stack justified in the README
- [x] Wireframe deviations justified in the README
- [x] At least 3 critical tests, AI-generated/guided, with the generating prompt logged
- [x] README: ✅/⚡/⚠️/🔮 sections, each shortcut with a Why and a Future
- [x] AI Development Log: Tooling, Data Strategy, Prompt Trail, The Correction
- [x] Public repository with a clean, incremental commit history
- [x] Setup instructions, validated by a fresh clone
- [ ] Live demo link — not required (PDF calls it "a huge plus," not a requirement); declared honestly in the README's ⚠️/🔮 sections instead
