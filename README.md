# TicketFlow

A booking/resource-claiming system built to solve one real problem twice: preventing double-booking under concurrent access, once with a JVM lock and once with database-level optimistic locking.

Two separate projects live in this repo, each opened independently in IntelliJ:

```
TicketFlow/
├── Engine/            pure Java, no framework
└── booking-service/   Spring Boot service
```

## What's actually in here

**Engine/** — the core domain model (`Resource`, `RoomResource`, `GearResource`, `Claim`) with custom exceptions, a `BookingManager` that uses `synchronized` to prevent double-booking, and a JDBC/HikariCP persistence layer on top of PostgreSQL. No Spring, no framework — this proves the concurrency problem is understood and solved from first principles before any framework hides it.

**booking-service/** — a Spring Boot service wrapping the same domain (via JPA, `@Entity`, single-table inheritance for `GearResource`/`RoomResource`) and solving the identical double-booking problem a different way: `@Transactional` + `@Version` optimistic locking instead of a JVM lock. Also has Redis caching on the read endpoints and a Docker Compose setup.

## The actual point of the project

Both sides solve the same race condition — two threads/requests trying to claim the last available slot at the same instant. Both are proven under real concurrent load, not just written and assumed to work:

- **Engine side:** JUnit stress test fires 500 threads at 10 resources simultaneously, asserts exactly the right number of successes/failures. Also re-run against a real Postgres backend (not just in-memory) via `DbConcurrencyStressRunner`.
- **Spring side:** JUnit test fires 50 concurrent claims at one resource via `@SpringBootTest`. Separately, also verified with real HTTP requests fired at the actual running Docker container (PowerShell `Start-Job`, 50 parallel `POST` requests), independently checked against Postgres afterward.

| | synchronized (Engine) | @Version (Spring) |
|---|---|---|
| Mechanism | JVM-level lock | Database rejects the losing write |
| Scales past one instance? | No | Yes |
| Blocking? | Yes, other threads wait | No, loser just gets rejected and can retry |
| Trade-off | Simple, guaranteed correct, bottleneck under load | No lock overhead, but more failure handling on the caller's side |

## Running the Engine

Requires PostgreSQL running locally with a `ticketflow` database.

1. Open `Engine/` in IntelliJ as its own Maven project
2. Run `schema.sql` then `seed.sql` against your `ticketflow` database
3. Copy `db.properties.example` to `db.properties`, fill in your actual password
4. Run `Main.java` for the pure in-memory proof, or `DbIntegrationRunner.java` / `DbConcurrencyStressRunner.java` for the DB-backed versions
5. Run the JUnit tests (`BookingManagerTest`, `CancellationManagerTest`) directly in IntelliJ

## Running the Spring service

**Without Docker (local dev):**
1. Open `booking-service/` in IntelliJ as its own Maven project
2. Copy `application.properties.example` to `application.properties`, fill in your actual password
3. Run `add_version_column.sql` against your `ticketflow` database (adds the `version` column `@Version` needs)
4. Start Redis locally: `docker run -p 6379:6379 redis:alpine`
5. Run `BookingServiceApplication.java`
6. Swagger UI at `http://localhost:8080/swagger-ui.html`

**With Docker Compose (full stack):**
```
cd booking-service
docker compose up --build
```
Starts the app + Redis together. The app reaches your existing native Postgres install via `host.docker.internal` — Postgres itself is deliberately not containerized here.

## What this is NOT

Being upfront about scope, since it matters more than padding a tech list:

- Not a microservices system — one Spring service, not multiple independently deployable ones
- No React frontend
- No CI pipeline
- Cancellation is only implemented on the Engine side, not persisted or exposed via the Spring API
- The Spring service duplicates a couple of exception classes from the Engine rather than sharing a common module — a deliberate corner cut to avoid restructuring into multi-module Maven under time pressure
