# Tasks

## 1. Audit and Inventory

- [x] Run Controller hard-rule scan for `@PutMapping`, `@DeleteMapping`, `@PatchMapping`, `@PathVariable`, Controller `Map` contracts, and mixed query/body writes.
- [x] Run Service pagination scan for `subList`, `stream().skip`, `stream().limit`, and equivalent in-memory pagination patterns.
- [x] Run N+1 risk scan for database reads inside loops or `stream().map` list assembly.
- [x] Run Mapper annotation scan for `@Select`, `@Update`, `@Insert`, and `@Delete`.
- [x] Classify findings into must-fix-now, safe-to-defer, and legacy exception groups.

## 2. Controller Contract Hardening

- [x] Replace write-operation `@PutMapping` / `@DeleteMapping` / `@PatchMapping` with `@PostMapping`.
- [x] Replace write-operation query/path IDs with explicit cmd fields.
- [x] Replace Controller `@RequestBody Map` with typed cmd classes.
- [x] Replace Controller `ApiResponse<Map<...>>` responses with typed VO classes.
- [x] Remove `@PathVariable` usage from enterprise Controller write actions.

## 3. Frontend API Synchronization

- [x] Update `enterprise-pc/src/api` methods affected by backend POST/cmd changes.
- [x] Update `enterprise-uniapp/src/api` methods affected by backend POST/cmd changes.
- [x] Verify changed page flows still pass build and request body fields match backend cmd names.

## 4. Pagination Hardening

- [x] Move `ApplicationServiceImpl`报名列表 pagination into `ScheduleApplicationMapper.xml` using same-condition `COUNT(*)` and `LIMIT/OFFSET`.
- [x] Replace `OperationServiceImpl` in-memory default todo truncation with remaining-capacity Mapper queries.
- [x] Re-run memory pagination scan until no high-priority matches remain.

## 5. N+1 Query Hardening

- [x] Ensure `CompanyWorkerServiceImpl` list uses batch worker profile queries and Map assembly.
- [x] Add or update tests proving list assembly does not call single-record worker lookups.
- [x] Review remaining single-record mapper calls and mark non-list business actions as acceptable.

## 6. Mapper XML Alignment

- [x] Add new Mapper XML methods for any new pageable or batch query.
- [x] Keep `WorkerSyncMapper` and `EnterpriseMapper` annotation SQL as tracked legacy debt unless strict migration is explicitly approved.
- [x] Document any remaining annotation SQL with rationale and future migration path.

## 7. Verification

- [x] Run `git diff --check`.
- [x] Run `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests clean compile`.
- [x] Run `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests test`.
- [x] Run `cd enterprise-uniapp && npm run build:mp-weixin` if mini-program API files changed.
- [x] Run `cd enterprise-pc && npm run build` if PC API files changed.
- [x] Re-run static scans and attach remaining debt list in final handoff.
