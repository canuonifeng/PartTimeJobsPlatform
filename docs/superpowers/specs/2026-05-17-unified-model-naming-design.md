# Unified Model Naming Design

**Goal:** Remove C-side/B-side naming from business models and payloads so both services speak the same domain language: `岗位`, `排班`, `报名`, `工人`, `企业`.

**Architecture:** Keep the existing service split and API routes. Rename exported domain types, request commands, and response VOs to business nouns that describe the same concept everywhere, while leaving database table names and endpoint paths unchanged. Frontend labels and any user-facing copy should use the same nouns so the naming is consistent across both mini programs and backend APIs.

**Tech Stack:** Spring Boot, MyBatis, Vue 3, uni-app, Java DTO/VO/entity classes.

---

## Scope

This change unifies naming in three layers:
- Backend Java model names
- Frontend model and display names
- Docs/comments that still say C-side or B-side when the same business object is meant

This change does **not** rename:
- REST paths
- database tables and columns
- service module names
- login/session behavior

## Naming Rules

Use one business word for one concept:
- `Job` for 岗位
- `JobSchedule` for 排班
- `JobApplication` for 报名
- `Worker` or `WorkerProfile` for 工人资料
- `Company` or `EnterpriseAccount` only when the object is truly company-specific

Use consistent suffixes:
- `Cmd` for write requests
- `VO` for read responses
- `Entity` for persistence objects

Avoid side prefixes in exported model names:
- Do not introduce `CJob*`, `BJob*`, `CWorker*`, `BWorker*` for shared business concepts
- Prefer one shared name per concept in each module

## Backend Model Plan

### enterprise-service
- Keep `Job`, `JobRate`, `JobSchedule`, `JobVO`, `JobRateVO`, `JobScheduleVO` as the canonical enterprise-side job vocabulary.
- Replace any side-specific naming in new or exposed models with the shared job vocabulary.
- Keep `EnterpriseAccount` only for the enterprise login identity, because that concept is inherently enterprise-specific.

### c-service
- Replace side-specific job and worker model names with shared vocabulary.
- Use `Job`, `JobDetailVO`, `JobSummaryVO`, `JobScheduleVO`, `JobApplicationVO`, `WorkerVO`, `WorkerProfileVO` consistently.
- Remove any `C`-prefixed model names from exported controller/service signatures.

### platform-service
- Keep company/platform administration models as company-facing business names.
- Remove `C`/`B` prefixes from shared business models if any appear in platform payloads.

## Frontend Model Plan

- Keep page behavior and routes as-is.
- Standardize displayed labels to `岗位`, `排班`, `报名`, `工人`, `企业`.
- Any code comments or local variables that encode C/B division should be renamed to business nouns where practical.

## Migration Strategy

1. Rename the public model types first.
2. Update controller signatures and service methods to use the new names.
3. Update frontend API wrappers and page code to the same terminology.
4. Run builds and tests for both mini programs and the backend services.

No compatibility aliases are planned. The codebase is small enough that direct renames are less risky than carrying old and new names together.

## Validation

- Backend builds succeed after the rename sweep.
- Both mini programs build successfully.
- Existing job, schedule, and application flows still work.
- No user-visible copy mentions `C端` or `B端` for shared business objects.
