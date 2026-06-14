# Tasks

## 1. Audit and Inventory

- [ ] Run Controller hard-rule scan for `@PutMapping`, `@DeleteMapping`, `@PatchMapping`, `@PathVariable`, Controller `Map` contracts, and mixed query/body writes.
- [ ] Run Service pagination scan for `subList`, `stream().skip`, `stream().limit`, and equivalent in-memory pagination patterns.
- [ ] Run N+1 risk scan for database reads inside loops or `stream().map` list assembly.
- [ ] Run Mapper annotation scan for `@Select`, `@Update`, `@Insert`, and `@Delete`.
- [ ] Classify findings into must-fix-now, safe-to-defer, and legacy exception groups.

## 2. Controller Contract Hardening

- [ ] Replace write-operation `@PutMapping` / `@DeleteMapping` / `@PatchMapping` with `@PostMapping`.
- [ ] Replace write-operation query/path IDs with explicit cmd fields.
- [ ] Replace Controller `@RequestBody Map` with typed cmd classes.
- [ ] Replace Controller `ApiResponse<Map<...>>` responses with typed VO classes.
- [ ] Remove `@PathVariable` usage from enterprise Controller write actions.

## 3. Frontend API Synchronization

- [ ] Update `enterprise-pc/src/api` methods affected by backend POST/cmd changes.
- [ ] Update `enterprise-uniapp/src/api` methods affected by backend POST/cmd changes.
- [ ] Verify changed page flows still pass build and request body fields match backend cmd names.

## 4. Pagination Hardening

- [ ] Move `ApplicationServiceImpl`报名列表 pagination into `ScheduleApplicationMapper.xml` using same-condition `COUNT(*)` and `LIMIT/OFFSET`.
- [ ] Replace `OperationServiceImpl` in-memory default todo truncation with remaining-capacity Mapper queries.
- [ ] Re-run memory pagination scan until no high-priority matches remain.

## 5. N+1 Query Hardening

- [ ] Ensure `CompanyWorkerServiceImpl` list uses batch worker profile queries and Map assembly.
- [ ] Add or update tests proving list assembly does not call single-record worker lookups.
- [ ] Review remaining single-record mapper calls and mark non-list business actions as acceptable.

## 6. Mapper XML Alignment

- [ ] Add new Mapper XML methods for any new pageable or batch query.
- [ ] Keep `WorkerSyncMapper` and `EnterpriseMapper` annotation SQL as tracked legacy debt unless strict migration is explicitly approved.
- [ ] Document any remaining annotation SQL with rationale and future migration path.

## 7. Verification

- [ ] Run `git diff --check`.
- [ ] Run `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests clean compile`.
- [ ] Run `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests test`.
- [ ] Run `cd enterprise-uniapp && npm run build:mp-weixin` if mini-program API files changed.
- [ ] Run `cd enterprise-pc && npm run build` if PC API files changed.
- [ ] Re-run static scans and attach remaining debt list in final handoff.
