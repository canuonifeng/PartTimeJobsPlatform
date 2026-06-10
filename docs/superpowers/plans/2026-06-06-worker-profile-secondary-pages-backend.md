# Worker Profile Secondary Pages Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace mock/static data in worker “我的” secondary pages with backend-backed data.

**Architecture:** Reuse existing C-service APIs for schedules, attendance, earnings, profile, real-name, and bank cards. Add minimal backend APIs for “我的报名” and worker settings preferences, then wire worker-uniapp pages to those APIs. Empty/error responses render empty states, not demo records.

**Tech Stack:** Spring Boot, MyBatis XML, Java 17, Vue 3 script setup, uni-app request wrapper, npm uni build.

---

## Files

- Backend signup API: `JobController.java`, `JobService.java`, `JobServiceImpl.java`, `ScheduleApplicationMapper.java`, `ScheduleApplicationMapper.xml`, new `WorkerSignupVO.java`.
- Backend settings API: new `WorkerSettingsController.java`, `WorkerSettingsMapper.java`, `WorkerSettingsMapper.xml`, `WorkerSettings.java`, `WorkerSettingsVO.java`, `UpdateWorkerSettingsCmd.java`, `scripts/14_worker_settings.sql`.
- Frontend APIs: `worker-uniapp/src/api/jobs.js`, new `worker-uniapp/src/api/settings.js`.
- Frontend pages: new `worker-uniapp/src/pages/signup/signup.vue`; modify `profile.vue`, `pages.json`, `clockIn.vue`, `earnings.vue`, `settings.vue`.

## Tasks

### Task 1: Add backend “我的报名” API
- [ ] Add `WorkerSignupVO` with application, job, company, schedule, pay, status, applied time fields.
- [ ] Add `ScheduleApplicationMapper.findMySignups(workerId, offset, pageSize)` and `countMySignups(workerId)`.
- [ ] Add MyBatis SQL joining `schedule_applications`, `job_schedules`, `jobs`, and company table using existing schema column names.
- [ ] Add `JobService.getMySignups(workerId, page, pageSize)` returning existing `PageVO` shape.
- [ ] Add `GET /api/jobs/applications/my` in `JobController` using current worker id.
- [ ] Verify with `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile` and relevant tests if present.

### Task 2: Add backend settings preferences API
- [ ] Add migration `scripts/14_worker_settings.sql` for `worker_settings` with push/location/quiet booleans.
- [ ] Add settings entity, VO, update command, mapper, XML.
- [ ] Add `GET /api/settings` returning persisted settings or defaults.
- [ ] Add `PUT /api/settings` merging partial booleans and persisting by worker id.
- [ ] Verify compile/tests.

### Task 3: Wire frontend APIs and signup page
- [ ] Add `getMySignups` to `worker-uniapp/src/api/jobs.js`.
- [ ] Add `worker-uniapp/src/api/settings.js` with get/update settings.
- [ ] Create `pages/signup/signup.vue` listing backend applications with pagination/refresh/empty states.
- [ ] Register signup page in `pages.json` and add it to `profile.vue` navigation allowlist.

### Task 4: Remove mock/fallback data from existing pages
- [ ] In `clockIn.vue`, remove fallback demo shifts and “示例排班” behavior.
- [ ] In `earnings.vue`, remove fallback summary/transactions and “示例/非真实” notices.
- [ ] In `settings.vue`, load profile/settings from backend, persist switches, keep real-name/bank-card navigation and logout.
- [ ] Ensure load failures show toast/empty values, not mock records.

### Task 5: Verify
- [ ] Run C-service compile/tests with JDK 17.
- [ ] Run `npm run build:h5` and `npm run build:mp-weixin` in `worker-uniapp`.
- [ ] Run `git diff --check`.
