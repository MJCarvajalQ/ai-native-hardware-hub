# Prompt Trail

The prompts below shaped the architecture and design of Hardware Hub, in the order
the decisions were made. Entries from the planning session (before any code existed)
are dated to that session; entries from the build itself are added as the work
happens.

Not every prompt sent during the build appears here — only the ones that changed
architecture, data model, state machine, AI design, or toolchain. Routine
implementation prompts are omitted; see "Which prompts earn an entry" for the rule.

**The plan:** the prompts below produced a full build plan — stack, data model,
state machine, AI design, seed-audit strategy, block-by-block steps, and a
requirements checklist. That plan is the actual skeleton the rest of this repo
was built against; see [`PLAN.md`](./PLAN.md) for a summary of it.

**Planning window:** the entries below dated 2026-08-15 come from one continuous
planning session, 10:49–15:05 UTC — about 4 hours, before a single line of code
existed. The first actual build commit didn't land until the next day. That
upfront time is why the build itself moved quickly: the architecture, the scope,
the block-by-block plan, and the verification gate on every step were already
settled going in, so the build was mostly execution against a plan rather than
figuring things out while also writing code.

**Why this file exists in this shape:** partway through planning, the question
came up of how prompts would actually get recorded once the build itself
started — not wanting to fabricate polished-looking prompts after the fact, but
also not wanting to dump a raw, unreadable transcript. That's what led to the
"curate, don't dump" approach used throughout this file (real prompts, including
the messy or reversed ones, but only the ones that actually shaped a decision),
and to the deferred-commit workflow this file follows — drafted after every
build block, reviewed as a whole, committed once at the end rather than
piecemeal.

---

## 2026-08-15 — Stack selection: Java + Spring Boot vs. Python + Vue
**Tool:** Claude Code (Sonnet 5 / Opus 5)

**Prompt:**
> what do you think about java and react? I know java more and react

**What came back:** A note that the job posting and task guardrails list
Python/JavaScript as the "preferred" stack, but the brief explicitly allows
substituting a language the candidate is more productive in ("we value results and
tool proficiency over specific syntax").

**Decision:** Java 17 + Spring Boot backend, React + Vite frontend. Justified in the
README as a deliberate tradeoff, not an oversight.

**Rejected:** Python + FastAPI (closer to the brief's stated preference, but slower
for this candidate) and Node/full-JS (no strong reason to abandon Java strength).

---

## 2026-08-15 — AI feature: first pass
**Tool:** Claude Code (Sonnet 5)

**Prompt:**
> Which AI feature should we build? explain to me each one, so far I think semantic
> search could be interesting because i did something similar before

**What came back:** Difficulty/risk breakdown of semantic search, the tool-calling
assistant, and the inventory auditor.

**Decision (later revisited):** Semantic search, initially favored partly because of
prior RAG experience.

**Rejected at this stage:** the assistant (higher build risk) and the auditor
(seemed redundant with the required Data Strategy section).

---

## 2026-08-15 — AI feature: reconsidered against prior work
**Tool:** Claude Code (Sonnet 5)

**Prompt:**
> Please guide me in choosing a single option to build. Consider my previous
> experience, but I don't need to copy anything from before.

**What came back:** A review of prior Java/AI projects (a Claude-powered CLI support
tool with RAG and a tool-calling loop). Recommendation shifted to the tool-calling
assistant, on the basis that it was the one capability not yet demonstrated.

**Decision (later reverted):** tool-calling assistant.

---

## 2026-08-15 — "Read between the lines": scope discipline
**Tool:** Claude Code (Opus 5)

**Prompt:**
> Please read between the lines to understand what they are asking... about 20% of
> the decision should rely on my previous work, using Java and React. I have
> experience with Java, so that will be the primary technology.

**Decision:** Weighted the AI-feature and stack choices toward what the brief
literally asks for, with prior experience as a minority input rather than the
deciding factor.

---

## 2026-08-16 — MVP-first restructure ("Plan B")
**Tool:** Claude Code (Opus 5)

**Prompt:**
> Create a plan B of a project with the minimum requirements mentioned in the task,
> and then, little by little, we will be incrementing more features... We need the
> MVP to be working properly.

**Decision:** Restructured the plan into Part 1 (every stated requirement, nothing
more — the definition of a submittable project) and Part 2 (an ordered increment
roadmap, only attempted once Part 1 works end-to-end). Every increment boundary
becomes a valid submission point.

---

## 2026-08-16 — Granular steps with verification gates
**Tool:** Claude Code (Opus 5)

**Prompt:**
> lets also divide the first part in steps, because you have issues sometimes when
> we create everything all at once

**Decision:** Broke Part 1 into 12 lettered blocks (A–L) and ~40 micro-steps, each
with its own explicit *Verify:* gate. Working rule: implement one step, verify it,
only then move to the next — never batch.

---

## 2026-08-16 — Engineering-practices audit
**Tool:** Claude Code (Opus 5)

**Prompt:**
> is this plan following good software development practices? like in the real
> work?

**What came back:** An honest gap list against real-world practice: no CI, no
integration tests (only the 3 required unit tests), schema via `ddl-auto` instead of
migrations, no `.env.example`.

**Decision:** Added GitHub Actions CI (Block F′), MockMvc integration tests
(Block F6), and `.env.example` (Block A3) to the MVP. Flyway migrations were
deliberately deferred to the increment roadmap (I9) rather than added to the MVP,
given the 2-day window — and declared as a ⚡ shortcut with reasoning rather than
left as an unexamined gap.

---

## 2026-08-16 — Git workflow: the user's own practice
**Tool:** Claude Code (Sonnet 5)

**Prompt:**
> I have always created the main branch and worked on features, later merging to
> main. can we use that approach? is the one I know

**What came back:** Confirmation that plain `git merge` (no `--no-ff`) works cleanly
here specifically because the blocks are built sequentially and solo — `main` never
moves between branching and merging a block, so every merge fast-forwards, producing
a linear history for free.

**Decision:** Branch per block → plain `git merge` into `main` → push. No PRs (the
brief asks only for "a clean commit history," not review process) and no `--no-ff`
(unnecessary once every merge is a fast-forward by construction).

---

## 2026-08-16 — Correction: inconsistent error handling in the seed normalizers
**Tool:** Claude Code (Sonnet 5)

**Context:** Building the seeder's `StatusNormalizer` (Block D3), right after
`DateNormalizer` (D2). `DateNormalizer` throws `IllegalArgumentException` on a date
it can't parse, rather than guessing. The first draft of `StatusNormalizer` did not
follow that pattern — it silently mapped any unrecognized status (including the
seed's `"Unknown"`) straight to `REPAIR` and logged the decision, all inside the
normalizer itself.

**The error:** two components with the same job — validate one field from the same
row — handled failure two different ways, for no real reason. Silently defaulting
also mixed two separate concerns into one class: "can I parse this" and "what do we
do when we can't."

**Prompt:**
> is there an alternative? like throwing an error? or isn't [silently defaulting] a
> good practice?

**How it was caught:** the user noticed the inconsistency with `DateNormalizer` and
asked directly whether throwing would be better practice — before any code was
written for the fix, on request ("explain it before we implement a change, I prefer
to make decisions with your guidance").

**Fix:** laid out three options — (1) split responsibility: `StatusNormalizer`
becomes a strict validator that throws, `DataSeeder` catches the exception per
record and owns the recovery policy (default to `REPAIR`, log why); (2) keep the
silent default, accept the inconsistency; (3) let it crash the whole app on boot
("fail fast"), rejected because the seed data is required input the task grades —
refusing to boot on the planted bad row isn't auditing it, it's failing the
assignment. Went with (1): `StatusNormalizer` now throws on anything unrecognized,
matching `DateNormalizer`; the recovery policy moves to `DataSeeder` (D4), where it
belongs, since "what do we do about bad data" is a product decision, not a parsing
one.

---

## 2026-08-16 — Correction: a silent-failure bug in my own "fix"
**Tool:** Claude Code (Sonnet 5)

**Context:** `HardwareService.toggleRepair` (Block E4) needs to flip `AVAILABLE` to
`REPAIR` and back, and reject the flip entirely while a device is `IN_USE`.

**First draft:** a ternary — `status == REPAIR ? AVAILABLE : REPAIR`.

**Prompt:**
> isn't this hardcoded?

**First "fix":** rewrote it as a `switch` *statement* with explicit `AVAILABLE` and
`REPAIR` cases, claiming it would "throw for anything else."

**Prompt:**
> are you sure? does it make sense this change?

**The actual bug:** the claim was false. A `switch` *statement* over an enum in
Java does not require exhaustiveness — with no `default`, an unhandled case
silently does nothing. If `HardwareStatus` ever grew a 4th value, this "fix" would
have looked safe while actually being a silent no-op: the status stays unchanged,
`save()` persists it as-is, and the caller gets a success response for a change
that didn't happen. Caught by the user re-asking, not by anything self-verified.

**Second fix, also reverted:** a `switch` *expression* (assigned to a variable),
which Java does enforce exhaustively at compile time for enums. Technically
correct, but arrived with a long comment explaining Java's switch-expression
exhaustiveness rules — a sign the code needed the reader to already know a fairly
advanced language feature.

**Prompt:**
> i liked it before, i am not sure the cases make sense with switch, plus the
> comments are too long

**Final version:** plain `if`/`else if`/`else`, one branch per `HardwareStatus`
value, with a genuine `else { throw ... }` instead of an implicit fallback. Same
safety property as the switch expression (every status is explicitly handled, an
unexpected one throws instead of silently doing nothing) via runtime checks
instead of a compile-time guarantee — less clever, but readable without needing to
know a specific Java feature, and easier to explain and defend later.

---

## 2026-08-16 — The 3 required critical tests (AI-generated, per task requirement)
**Tool:** Claude Code (Sonnet 5)

**Prompt (from planning, "MVP tests" section of the build plan):**
> The 3 required critical tests (service layer): 1. Renting REPAIR hardware is
> rejected. 2. Renting IN_USE hardware is rejected. 3. Rent → return round-trip
> restores AVAILABLE and clears assignedTo.

**What came back:** `HardwareServiceTest` — three JUnit 5 tests using Mockito to
mock `HardwareRepository`, exercising `HardwareService` directly with no Spring
context (fast, and appropriate since the service has no framework-specific
behavior to test). `cannotRentHardwareUnderRepair` and
`cannotRentHardwareAlreadyInUse` assert `IllegalHardwareStateException`;
`rentThenReturnRestoresAvailableAndClearsAssignedTo` exercises the full
rent→return cycle and asserts both the status and `assignedTo` field at each step.

**Kept as generated**, no corrections needed — verified green via `mvn test`
before committing.

---

## 2026-08-16 — 401 vs 403: a self-caught correctness bug in the auth wiring
**Tool:** Claude Code (Sonnet 5)

**Context:** Block G4, wiring the token filter into `SecurityConfig` with
`anyRequest().authenticated()`. The plan's stated verification was "protected
endpoint without a token → 401."

**What happened:** the manual curl check returned **403**, not 401. Spring
Security's default behavior, with no `AuthenticationEntryPoint` configured, is
to answer *every* rejection — missing token and (later, in G5) wrong role alike
— with a bare 403. That conflates two different situations: 401 means "you are
not authenticated," 403 means "you are, but you're not allowed to do this."
Left as default, G5's role check would have been indistinguishable from simply
forgetting to log in.

**Fix:** added `RestAuthenticationEntryPoint`, a small `AuthenticationEntryPoint`
that explicitly returns 401 with the same `ErrorResponse` shape the rest of the
API uses, wired via `.exceptionHandling(...)` in `SecurityConfig`. Verified by
re-running the same curl check: 401 without a token, 200 with a valid one.
Reserves 403 exclusively for the "authenticated but wrong role" case Block G5
builds next — the two failure modes are now distinguishable by status code
alone, which matters for anyone building a frontend against this API later.

---

## 2026-08-16 — The Correction: a role-gated user got 401 instead of 403
**Tool:** Claude Code (Sonnet 5)

**Context:** Block G5, restricting `POST /api/hardware` (and delete/repair-toggle)
to `hasRole("ADMIN")`. Test setup: created a regular user ("jane") via Block G6's
`POST /api/users`, logged her in, and hit the admin-only endpoint with her token.

**The bug:** the response was `401 {"message":"authentication required"}` — my
own `AuthenticationEntryPoint` text — even though jane was genuinely
authenticated. A user who is logged in but lacks permission should get 403
("you're known, but not allowed"), not 401 ("we don't know who you are").
Getting this wrong would have meant Block G5's role check was indistinguishable
from simply not being logged in at all — a real correctness bug in exactly the
mechanism the task explicitly asks for ("Only users previously created by the
Admin can access the Hub").

**How it was diagnosed, step by step, not guessed:**
1. Reproduced with `curl -v` — confirmed the 401 body was mine, not generic.
2. Suspicious that jane's *other* endpoints (list, rent) worked fine with the
   identical token — ruled out a general authentication failure.
3. Added temporary debug logging inside `TokenAuthenticationFilter`, printing
   the resolved authentication both before and immediately after
   `filterChain.doFilter()` returned.
4. The debug log showed the filter *itself* saw `response status 403` right
   after the chain completed — meaning Spring Security's authorization check
   worked correctly and set 403 internally. But the client received 401. Two
   different, both-true-sounding facts that could only be reconciled by
   something happening *after* that point in the same request.
5. Traced the mechanism: Spring Security's default `AccessDeniedHandler` calls
   `response.sendError(403)`, which doesn't write a body — it flags the
   response for the servlet container to perform an **internal forward to
   `/error`**. That forward re-enters the dispatch cycle, and the security
   context (a per-request `ThreadLocal`, cleared once the original filter chain
   unwinds) is gone by then — so the forwarded request has no authentication
   and lands on the `AuthenticationEntryPoint` instead, overwriting the correct
   403 with a 401.

**Fix:** `RestAccessDeniedHandler`, mirroring `RestAuthenticationEntryPoint` —
writes the 403 response body directly via `response.getWriter()` instead of
calling `sendError()`, which avoids the internal forward (and the context loss
it causes) entirely. Verified: wrong role → 403 with the correct message, no
token → 401, allowed actions → 200 — all three now correctly distinguished.
Debug logging was temporary and removed once the fix was confirmed.

---

## 2026-08-16 — Note: autonomous overnight work begins here

**Prompt:**
> can you develop the rest by yourself without merging
> to main? when you finish, i can continue?

**Important note:** this was only possible because Blocks I–L already existed as
a fully specified, step-by-step plan with its own verification gates, from the
~4-hour planning session up front (see the note at the top of this file).
Working "by myself" overnight meant advancing through a plan that was already
agreed on, not inventing scope on the fly.

**Arrangement:** work continues through Blocks I–L on a single branch, never
merged into `main`, never pushed (SSH stays untouched, as established earlier).
The same discipline as every block before this — one micro-step at a time,
verify each, commit at block boundaries with the same message conventions —
just without a live back-and-forth. Judgment calls that would normally be a
question get made, documented here with reasoning, and left for review rather
than blocking on an answer that isn't available. Anything genuinely ambiguous
(not "which is more correct," but actually the user's call) gets flagged
explicitly rather than guessed silently.

One deliberate scope cut for this stretch: Block K's semantic search will be
built without live verification against the real Claude API, since no API key
is available in this shell. The code, prompt design, and defensive parsing are
built to the same standard as everything else; only the "does the real call
behave as expected" check is deferred.

---

## 2026-08-16 — Self-caught: rent/return trusted a client-supplied email
**Tool:** Claude Code (Sonnet 5), working autonomously overnight — see the note
above about that arrangement

**Context:** starting Block I (the dashboard), about to wire up the frontend's
rent/return buttons. Went to write the API call and realized `HardwareController`'s
`/rent` and `/return` endpoints still took a `RentRequest` body with a client-supplied
`userEmail` field — a leftover from Block F, before real auth existed. The DTO's own
comment even said this should be replaced once Block G added a resolvable principal,
but that follow-up never actually happened.

**The bug:** with real login now in place, this meant any authenticated user could
rent hardware "as" a different email just by changing the request body — a genuine
authorization gap, not just leftover scaffolding.

**Fix:** `@AuthenticationPrincipal User currentUser` replaces the request body
entirely; the acting user always comes from the verified token, never from
anything the client sends. Deleted `RentRequest.java` (no longer needed — the
endpoints take no body at all now). Updated the integration test accordingly.
Verified with curl: sent a spoofed `userEmail` in the request body anyway — the
resulting `assignedTo` was still the real authenticated user, confirming the
spoofed field is fully ignored, not just unused by the happy path.

---

## 2026-08-17 — The Correction: Claude's own response format broke my Claude integration
**Tool:** Claude Code (Opus 5), with a real `ANTHROPIC_API_KEY` for the first time
this session

**Context:** Block K (semantic search) was built overnight without live
verification — see the note above. First thing this session, the user supplied a
real API key and asked to verify it. `ClaudeClientManualTest` (a "reply OK" sanity
check) passed. Wiring the actual search prompt through the full stack did not:
every call returned a 502, with the parser reporting "expected a JSON array from
search response."

**The bug:** `ClaudeClient.sendMessage` read `response.content[0].text` unconditionally,
assuming the first content block is always the answer. That assumption was true
when the client was written (against older non-thinking models) but not against
Claude Opus 5, which thinks by default now — `content[0]` is a `"thinking"` block
with no `text` field, and the real answer is a later `"text"` block. Jackson's
`path("text")` on a node with no such field silently returns an empty string
rather than erroring, so `ClaudeClient` returned `""`, which the (correctly
defensive) `SearchResponseParser` correctly rejected as "not a JSON array" — the
parser did its job; the bug was one layer up, in what it was handed.

**Prompt:**
> what is the correct model ID string to use for the Anthropic Messages API... I
> need the exact model identifier to hardcode in a backend integration

(asked before writing `ClaudeClient` at all, to get `claude-opus-5` right rather
than guessing — the API-drift table this returned is exactly what made the later
bug diagnosable instead of mysterious)

**How it was diagnosed:** reproduced the exact prompt payload with a raw `curl`
against the Messages API directly (bypassing the Java client entirely), inspected
the full JSON response, and saw two content blocks: `{"type":"thinking",...}`
first, `{"type":"text",...}` second. That confirmed the shape assumption was wrong
before touching any code.

**Fix:** `ClaudeClient` now scans the `content` array for the first block with
`"type": "text"` instead of assuming index 0. Verified against the real API with
both planned demo queries: "something to test a mobile app on" correctly returned
the phone/tablet cluster, and — the headline case — "something safe to give a new
hire" correctly excluded the Dell XPS whose notes read "Battery swelling, do not
issue without service," even though its status is `AVAILABLE`. That's the specific
capability the AI layer exists to demonstrate: a `LIKE` query on `status` would
have handed that device to a new hire; reading the note stopped it.

**Why this is the featured Correction, not the 401/403 one earlier:** both are
real, self-diagnosed bugs, but this one is inside the AI-native layer itself — a
change in the model provider's own default behavior silently broke code written
against the old default, and it was only catchable by testing against the live
API, which is exactly the gap the overnight "unverified" note flagged in advance.

---

## Which prompts earn an entry

Anything that changed the architecture, the data model, the state machine, the AI
design, or the toolchain. Not "fix this typo," "why won't this compile," or routine
code generation with no decision attached.
