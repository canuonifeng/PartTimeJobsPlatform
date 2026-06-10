# Worker Profile Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the C-side “我的” page load all displayed user, stats, and earnings data from the backend without mock values.

**Architecture:** Add a C-service dashboard endpoint under the existing profile domain that aggregates profile, home stats, and earnings summary for the current worker. The uniapp profile page calls this single endpoint and renders empty/zero states on failure.

**Tech Stack:** Spring Boot Java 17, Vue 3 setup syntax, uni-app request wrapper, Maven tests.

---

### Task 1: Backend dashboard API

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/vo/ProfileDashboardVO.java`
- Modify: `c-service/src/main/java/com/parttime/cservice/controller/ProfileController.java`
- Test: `c-service/src/test/java/com/parttime/cservice/controller/ProfileControllerTest.java`

- [ ] Add `ProfileDashboardVO` with `ProfileVO profile`, `HomeStatsVO stats`, `EarningsSummaryVO earningsSummary`.
- [ ] Inject `HomeService` and `WithdrawalService` into `ProfileController`.
- [ ] Add `GET /api/profile/dashboard`, reuse current worker auth, return 401 when unauthenticated.
- [ ] Add controller test verifying authenticated response contains profile/stats/earningsSummary.
- [ ] Run `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -Dtest=ProfileControllerTest test` in `c-service`.

### Task 2: Frontend profile page data binding

**Files:**
- Modify: `worker-uniapp/src/api/profile.js`
- Modify: `worker-uniapp/src/pages/profile/profile.vue`

- [ ] Add `getProfileDashboard()` calling `GET /api/profile/dashboard`.
- [ ] Replace hardcoded `stats` values with computed values from dashboard stats/earnings.
- [ ] Replace hardcoded income card values with dashboard values.
- [ ] On load failure, show toast `我的页面加载失败` and render zero values instead of mock numbers.
- [ ] Keep menu and navigation unchanged.

### Task 3: Verification

- [ ] Run `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile` in `c-service`.
- [ ] Run `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -Dtest=ProfileControllerTest test` in `c-service`.
- [ ] Run `npm run build:mp-weixin` in `worker-uniapp`.
- [ ] Run `git diff --check`.
