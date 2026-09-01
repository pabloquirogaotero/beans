# Beans

**A store, and the system that runs it.**

Beans is an inventory, ordering, and fulfilment system for a specialty-coffee retailer, with a thin storefront on top. I built it from scratch to learn how real backend systems are *structured and reasoned about* — the patterns, the trade-offs, and the judgment.

To be clear about what this is: **it's a learning vehicle, not a product**. I chose this domain precisely *because* it forces the problems that matter in backend engineering (concurrency, consistency, traceability, bounded contexts) rather than to reinvent a wheel that already rolls fine.

---

## The headline: a checkout that can't oversell

The most interesting problem in the system is deceptively simple: **two customers, one last bag of coffee — who gets it, and how do you guarantee you never sell it twice?**

The answer is a **reserve-before-pay** checkout backed by **optimistic locking**:

1. A checkout *reserves* stock (a timed hold) before payment, so the customer isn't told at the till that the item vanished.
2. Reserving decrements a per-lot counter guarded by an aggregate invariant (`reserved ≤ on_hand`) that makes overselling structurally impossible within a single operation.
3. A `@Version` column on the stock row makes the *database* reject a second concurrent commit based on a stale read — so two transactions that both pass the in-memory guard can't both succeed.
4. The loser of the race retries with jittered backoff, or is cleanly reported as sold-out.
5. Unpaid holds expire on a schedule and return their stock to the pool.

This is proven by an **integration test that fires 60 parallel reserves at 40 units and asserts no lot is ever oversold**, running against a real PostgreSQL instance (via Testcontainers), not a mock. Getting it green was the most instructive part of the project — see [Concurrency](#concurrency-optimistic-locking-and-its-limits) below for the trade-offs I hit and the reasoning behind the choices.

---

## Architecture

**Modular monolith, hexagonal (ports & adapters), Domain-Driven Design.**

### Why a modular monolith, not microservices

The checkout flow crosses two bounded contexts (Ordering → Inventory) in a **synchronous, consistency-critical** call: reserving stock must be atomic and immediately consistent. Splitting that across a network boundary would turn a database transaction into a distributed-transaction problem for no benefit at this scale. So it's a monolith — but a *modular* one, with contexts separated by clean ports, so the async fulfilment tail (the natural first thing to extract) could be split out later without touching the core.

### Hexagonal architecture

The domain core has **zero framework imports** — no Spring, no JPA, nothing. Business rules live in plain Java that runs in milliseconds without a container. Everything technical (persistence, web, scheduling) is an *adapter* plugged into a *port*. This keeps the interesting logic testable in isolation and makes the framework a detail rather than the foundation.

### Domain-Driven Design

- **Bounded contexts**: Inventory (core — the real logic), Ordering (core — thin, owns the checkout), Catalog (supporting — owns names/prices/images, built later). Contexts reference each other only by ID (`SkuId`), never by sharing models.
- **Aggregates protect invariants**: `LotStock` is the crown jewel — it guards `reserved ≤ on_hand` and exposes only guarded mutations (`reserve`, `release`), never setters.
- **Value objects everywhere**: `SkuId`, `Quantity`, `LotCode`, `Coordinates`, etc. — typed identifiers instead of primitive obsession, self-validating in their constructors, so an invalid value literally cannot exist.
- **JPA entities are kept separate from domain aggregates.** The domain stays immutable and framework-free; mappers translate at the boundary. This costs some mapping code and buys a domain that JPA's requirements can't corrupt.

---

## Key design decisions

Each of these is a deliberate choice with a real trade-off — the point of the project was to make and defend them, not to reach for a default.

### Batch/lot-based inventory from day one
Stock is tracked per **production lot**, not just per SKU. Food traceability and recall are legal requirements (EU 178/2002): if a lot goes bad, you must know which orders it reached. This also enables **FEFO** allocation (first-expiry-first-out). The cost is a more complex model (a `LotStock` per lot per location); the payoff is that traceability and expiry handling are possible at all.

### Reserve-before-pay, with expiry
Holding stock before payment protects the customer experience, but a hold that never expires leaks stock forever. So holds carry an `expiresAt`, and a scheduled sweep releases expired ones. This is the full lifecycle: `reserve → (confirm | expire) → release`.

### Allocation: consolidated, closest, FEFO — with a shelf-life gate
Online orders are fulfilled by an allocation strategy that (1) uses **warehouses only**, (2) prefers a **single closest** location that can cover the whole order, (3) splits across locations only if forced, and (4) picks lots **FEFO** within a location. A separate **shelf-life threshold** filters out lots too close to expiry *before* allocation — you don't ship someone coffee that's about to go stale. The documented trade-off: distance dominates FEFO across locations (optimising delivery over global freshness), which is a deliberate call.

### Channel isolation: store stock is never sold online
The hardest real-world problem here is two channels competing for one pool of stock — an online order and a walk-in customer both wanting the last bag. You *can't* tell a customer holding the item at the till "sorry, it just sold online." The clean resolution isn't a smarter lock, it's a **business rule**: online orders draw only from warehouse stock; store stock belongs to the store. This dissolves the conflict by design rather than racing to resolve it.

### Concurrency: optimistic locking, and its limits
Optimistic locking (`@Version`) is the right default here because genuine contention on a single coffee lot is low, and it holds no database locks. I proved it prevents overselling under parallel load — but I also hit its boundaries and can articulate them:

- **Under high contention on one hot row it degrades** (retry storms, and an unlucky request can starve). The fix at real scale is pessimistic locking (so requests queue instead of thrash) or removing the hot row entirely.
- Getting the test green surfaced two genuinely subtle bugs: an optimistic-lock leak caused by re-reading the row inside `save()` (the version check ended up validating against a *fresher* version than the reservation decision was made on — fixed by carrying the version through the aggregate), and a *test-fixture* flaw where FEFO was quietly spreading reservations across a second, untracked lot, making the success counter and the availability read measure different rows. The database invariant was never actually violated; the test's accounting was.

That debugging arc taught me more about concurrency than any amount of reading about `@Version` — the difference between "it prevents oversell" and "it prevents oversell *and I can prove which layer guarantees that*."

### Where Kafka does and doesn't belong
Kafka is planned for the **async fulfilment tail** (Sprint 2), not the synchronous checkout. Using events for a consistency-critical reserve would trade a simple transaction for the dual-write problem. Event-driven where it fits (async, decoupled), transactional where correctness is immediate — not events everywhere because they're fashionable.

---

## Tech stack

- **Java 21**, **Spring Boot**
- **PostgreSQL** with **Flyway** migrations
- **Docker** (local database and, via **Testcontainers**, real-database integration tests)
- **JUnit 5** for unit and integration tests
- **Maven**

---

## Testing strategy

The tests are shaped by *what's worth proving*, not a coverage percentage:

- **Domain unit tests** — the `reserve` invariant at its boundaries, every branch of the allocation policy, value-object validation, reservation state transitions. Pure Java, milliseconds, no Spring, no database.
- **Integration tests** (`*IT`, Testcontainers) — real persistence round-trips, that the `@Version` column actually increments, and that migrations and JPA mappings agree with the real schema.
- **The concurrency test** — parallel reserves against real PostgreSQL, proving the no-oversell guarantee under contention. This is the test that can only pass if every layer (aggregate guard, optimistic lock, transaction boundary) is correct.

Coverage tooling is used as a gap-finder, never a target — a test earns its place by catching a plausible wrong implementation, not by touching a line.

---

## Running it

```bash
# start PostgreSQL
docker compose up -d

# run the application
./mvnw spring-boot:run
```

The checkout endpoint is `POST /api/checkout` (interactive docs via Swagger UI when running).

```bash
# run everything, including Testcontainers integration tests (Docker required)
./mvnw verify
```

---

## Deliberately out of scope (and why)

Naming what I *didn't* build is as much a design decision as what I did:

- **The transactional outbox / dual-write problem** is understood and documented, not solved — reserve is synchronous and publishes nothing, so it doesn't arise yet; it will when async fulfilment lands.
- **Authentication** — checkout is a guest flow for now. The `CheckoutId` is an unguessable correlation handle, not a security token; identity binding arrives with the auth slice.
- **Geocoding** — the checkout API takes coordinates, assuming client-side address resolution; a server-side geocoding adapter is the alternative.
- **Payment** is stubbed until Sprint 2.

These are production concerns I chose to defer deliberately, so the core stays focused on the patterns the project exists to demonstrate.

---

## Roadmap

- **✓ DONE: Sprint 0 — Foundations** project setup, CI, containerized database, integration-test harness
- **✓ DONE: Sprint 1 — Checkout slice** the full reserve-before-pay lifecycle: domain, application, persistence, web, concurrency-safe reserve, and expiry
- **Sprint 2 — Payment & fulfilment** — stubbed payment → confirm → order creation, plus async fulfilment via Kafka
- **Sprint 3 — Admin & traceability** — inventory back office, lot-to-customer traceability queries, authentication
- **Sprint 4 — Storefront** — React frontend

---

*Built to learn how real systems are made — by making one.*
