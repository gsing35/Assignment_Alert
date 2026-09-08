# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository structure

This is a two-project repo: a Spring Boot backend at the repo root, and a Chrome extension frontend in `assignment-alert-ext/`. There is no CI config and no monorepo tooling (no Turborepo/Nx/workspaces) tying the two together — they're built and run independently.

## Backend (Spring Boot, Java 21, Maven)

Commands (run from repo root):
- Build: `./mvnw clean install`
- Run locally: `./mvnw spring-boot:run` (requires env vars `DB_HOST`, `DB_PORT`, `DB_NAME`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `AWS_REGION` — see `.env`)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=AssignmentAlertApplicationTests`
- Run a single test method: `./mvnw test -Dtest=AssignmentAlertApplicationTests#contextLoads`
- Package without tests (matches the Docker build): `./mvnw package -DskipTests`
- Docker: `docker-compose up` (Postgres 18.1 + app, `network_mode: host`, reads root `.env`)

### Architecture

Package-by-feature under `com.assignment_alert.Assignment_Alert`:

- `assignments/`, `courses/`, `user/` — each domain follows the same layering: entity, Spring Data JPA repository, service, controller, and response DTO(s). DTOs are Java records with a static `from(entity)` factory method; controllers never return entities directly.
- `canvas/` — integration layer with the Canvas LMS API. `CanvasApiClient` (via a shared `RestTemplate` bean) calls Canvas, `CanvasAuthenticationService` validates/stores access tokens, `CanvasSyncService` pulls courses/assignments into the local DB. `canvas/dtos/` holds the DTOs and mappers for parsing Canvas's JSON into local entities. This package is actively evolving (numerous TODOs).
- `aws/` — `AwsConfig` and `AwsSecretsManagerService`. Each user's Canvas access token is stored in AWS Secrets Manager (key `User-{userId}`), not in Postgres. Uses `InstanceProfileCredentialsProvider`, i.e. it expects to run on EC2 with an IAM role — there are no static AWS credentials, so this won't work locally without a workaround.
- `security/` — `SecurityConfig` currently disables CSRF and permits all requests unauthenticated; no auth is enforced yet. `AuthticationEntryPoint` (note the filename typo) is an unwired empty stub for future work.
- `exceptions/` — `GlobalExceptionHandler` (`@ControllerAdvice`) plus a set of domain-specific `RuntimeException` subclasses (e.g. `AssignmentNotFoundException`, `DuplicateCourseException`, `CanvasApiException`) and an `ErrorResponse` record.
- `config/` — the shared `RestTemplate` bean used by `CanvasApiClient`.

### Domain model

`User 1—* Course 1—* Assignment` (all entities use Lombok `@Data`). Note: each user gets their own copy of a course and its assignments — courses/assignments are duplicated per user rather than normalized/shared.

### Persistence

PostgreSQL via Spring Data JPA/Hibernate, with `ddl-auto: update` (no Flyway/Liquibase — schema evolves via Hibernate, not migrations). `src/main/resources/db/V1_Inital_Setup.sql` is a stale/broken migration file with invalid SQL and an outdated schema; it isn't referenced by any migration tool and has no effect at runtime — ignore it. The only Spring config file is `src/main/resources/application.yaml` (no dev/prod profile split yet). H2 is a dependency but is not wired as the active datasource.

### API surface

All endpoints are under `/api/v1`. Each controller is annotated `@CrossOrigin` individually (allow-all origins) rather than using a central CORS configuration.

- `AssignmentsController`: `GET /assignments?courseId=&filter=` (filter is one of `upcoming`|`incomplete`|`blocking`), `GET /assignments/{id}`, `PUT /assignments/{id}` (body: completed/priority/blockedUntil/blockingEnabled), `PUT /assignments/{id}/completed?completed=`
- `CourseController`: `GET /courses/{courseId}/users/{userId}`, `PUT /courses/{courseId}/users/{userId}` (triggers a Canvas re-sync for that course)
- `UserController`: `GET /users/{userId}`
- `CanvasController`: `POST /canvas/connect` (body: domain + Canvas access token; validates the token, stores it in Secrets Manager, creates/updates the User, triggers an initial full sync)

### Testing

JUnit 5 via Spring Boot 4's granular test starters (`spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test`, `spring-boot-starter-webmvc-test`). Only one test currently exists — `AssignmentAlertApplicationTests#contextLoads`, an empty context-load smoke test. There are no controller/service/repository tests yet.

## Frontend (`assignment-alert-ext/` — Chrome Extension, Manifest V3)

Commands (run from `assignment-alert-ext/`):
- Install: `npm install`
- Dev server: `npm run dev`
- Build: `npm run build` (runs `tsc -b && vite build`)
- Lint: `npm run lint`
- Preview: `npm run preview`
- No test suite is configured.

### Architecture

React 19 + TypeScript + Vite 8, bundled as a Manifest V3 Chrome extension via `@crxjs/vite-plugin` (see `vite.config.ts` and `manifest.json`). `src/main.tsx`/`App.tsx` is the extension popup UI (still largely scaffold-level); `src/background.ts` is the MV3 service worker (uses `chrome.alarms`).

- `src/api/` — one file per backend resource (`assignments.ts`, `canvas.ts`, `courses.ts`, `users.ts`), all using native `fetch` against `import.meta.env.VITE_API_URL`, with shared response/error handling in `apiUtil.ts` (`handleResponse<T>()`). `apiTests.ts` holds manual console-log smoke tests for these calls.
- `src/types/index.ts` — TypeScript types that manually mirror the backend's response DTOs; keep these in sync by hand when backend DTOs change.
- No router and no state-management library — plain `useState` only, beyond the `api/`/`types/` layers.

### Note on a committed credential

`assignment-alert-ext/src/api/apiTests.ts` contains a hardcoded Canvas access token used for manual smoke testing, committed to the repo. Treat it as a credential to flag for rotation/removal — do not reuse or extend it.
