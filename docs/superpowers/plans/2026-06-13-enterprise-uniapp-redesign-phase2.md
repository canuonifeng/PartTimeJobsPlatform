# Enterprise UniApp Redesign Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the enterprise miniapp core business list pages for jobs, applications, schedules, and salary settlement.

**Architecture:** Keep all existing API calls, state variables, pagination, pull-down refresh, and mutation handlers. Replace page templates and styles with the Phase 1 operation-center visual system so the core workflow matches 工作台、流程、待办、我的.

**Tech Stack:** UniApp, Vue 3 `<script setup>`, existing API modules, shared `op-*` classes from `App.vue`, WeChat mini program compatible CSS.

---

## Scope

Included:

- Redesign `enterprise-uniapp/src/pages/jobs/jobList.vue` as a medium-density operation list.
- Redesign `enterprise-uniapp/src/pages/applications/applicationList.vue` as a review-focused application list.
- Redesign `enterprise-uniapp/src/pages/schedules/scheduleList.vue` as a shift-operation list.
- Redesign `enterprise-uniapp/src/pages/attendance/attendanceList.vue` as a salary-settlement list with clear batch selection.

Deferred:

- Job detail redesign.
- Job form step-by-step redesign.
- Basic management pages.
- New backend-driven dashboard statistics.

## File Structure

- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Modify: `enterprise-uniapp/src/pages/applications/applicationList.vue`
- Modify: `enterprise-uniapp/src/pages/schedules/scheduleList.vue`
- Modify: `enterprise-uniapp/src/pages/attendance/attendanceList.vue`

## Tasks

### Task 1: Jobs list visual refresh

- [ ] Preserve existing script logic and all handlers.
- [ ] Replace template with operation header, status tabs, medium-density cards, and floating publish button.
- [ ] Replace style block with local styles using shared `op-*` classes.
- [ ] Build with `cd enterprise-uniapp && npm run build:mp-weixin`.

### Task 2: Applications list visual refresh

- [ ] Preserve pagination, status helpers, accept/reject handlers, and phone masking.
- [ ] Replace template with review-focused cards and primary accept/reject actions.
- [ ] Replace style block with operation-list styles.
- [ ] Build with `cd enterprise-uniapp && npm run build:mp-weixin`.

### Task 3: Schedules list visual refresh

- [ ] Preserve pagination, status helpers, delete confirmation, and worker info helpers.
- [ ] Replace template with shift cards emphasizing date, time, worker, status, and cancel action.
- [ ] Replace style block with timeline-compatible operation styles.
- [ ] Build with `cd enterprise-uniapp && npm run build:mp-weixin`.

### Task 4: Salary settlement list visual refresh

- [ ] Preserve selection, batch pay, batch delete, pagination, and settlement helpers.
- [ ] Replace template with settlement summary, select-all bar, and medium-density salary cards.
- [ ] Replace style block with batch-operation styles.
- [ ] Build with `cd enterprise-uniapp && npm run build:mp-weixin`.

### Task 5: Final verification

- [ ] Run `cd enterprise-uniapp && npm run build:mp-weixin`.
- [ ] Run `cd enterprise-uniapp && npm run build:h5`.
- [ ] Run `git diff --check`.
- [ ] Commit source changes only; do not commit `dist/` or `node_modules`.
