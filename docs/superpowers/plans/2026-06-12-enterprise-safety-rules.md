# 企业端安全操作规则 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在企业小程序、企业 PC 和企业后端统一限制排班取消、薪资结算/删除、职位删除、账号删除，并修复缺勤状态显示。

**Architecture:** 前端负责中文显示和隐藏/禁用非法操作入口；后端负责在对应 Service 方法中强制校验，防止绕过前端直接调用接口。保持现有接口路径不变，除账号禁用优先复用已有更新接口。

**Tech Stack:** Spring Boot + MyBatis + Vue 3 + Element Plus + UniApp。

---

## File Structure

- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java` — 取消排班时间校验。
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/AttendanceHoursServiceImpl.java` — 已结算不可结算/删除。
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java` — 删除职位改为拒绝。
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/AccountServiceImpl.java` — 删除账号改为拒绝，禁用走更新状态。
- Modify: `enterprise-uniapp/src/pages/schedules/scheduleList.vue` — 缺勤显示、未来排班才可取消。
- Modify: `enterprise-uniapp/src/pages/attendance/attendanceList.vue` — 已结算不可选、不可结算/删除。
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue` — 移除删除职位入口。
- Modify: `enterprise-uniapp/src/pages/accounts/accountList.vue` — 删除改禁用。
- Modify: `enterprise-pc/src/views/schedules/ScheduleShiftList.vue` — 缺勤显示、未来排班才可取消。
- Modify: `enterprise-pc/src/views/attendance/AttendanceHoursList.vue` — 已结算不可结算/删除。
- Modify: `enterprise-pc/src/views/jobs/JobList.vue` — 移除删除职位入口。
- Modify: `enterprise-pc/src/views/accounts/AccountList.vue` — 删除改禁用。

---

### Task 1: 后端强制规则

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/AttendanceHoursServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/AccountServiceImpl.java`

- [ ] **Step 1: 排班取消校验**

在 `ScheduleServiceImpl.cancelShift(Long id)` 读取排班后增加：已取消拒绝；`LocalDateTime.of(shift.getShiftDate(), shift.getStartTime()).isAfter(LocalDateTime.now())` 为 false 时拒绝。

- [ ] **Step 2: 薪资结算/删除校验**

在批量结算和批量删除入口中，读取本次 ID 对应考勤记录；任一 `settlementStatus` 为 `PAID` 时抛出运行时异常，文案为“已结算记录不能结算或删除”。

- [ ] **Step 3: 职位删除拒绝**

在职位删除 Service 方法开头直接抛出运行时异常，文案为“职位不能删除，请关闭职位”。保留关闭接口不变。

- [ ] **Step 4: 账号删除拒绝**

在账号删除 Service 方法开头直接抛出运行时异常，文案为“账号不能删除，请禁用账号”。确认更新账号接口支持 `status` 后由前端调用更新状态。

- [ ] **Step 5: 验证后端**

Run:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

Expected: tests pass.

---

### Task 2: 企业小程序规则

**Files:**
- Modify: `enterprise-uniapp/src/pages/schedules/scheduleList.vue`
- Modify: `enterprise-uniapp/src/pages/attendance/attendanceList.vue`
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Modify: `enterprise-uniapp/src/pages/accounts/accountList.vue`

- [ ] **Step 1: 排班考勤**

`statusLabel` 增加 `ABSENT: '缺勤'`；`statusClass` 增加缺勤样式；新增 `canCancelShift(s)`，用 `new Date(`${date}T${startTime}`) > new Date()` 判断未来排班，且状态不是 `CANCELLED`。取消按钮只在 `canCancelShift(s)` 为 true 时显示。

- [ ] **Step 2: 薪资管理**

选择记录时过滤 `settlementStatus === 'PAID'`；批量结算和删除前只保留未结算 ID；已结算记录卡片显示“已结算”，不允许选中参与操作。

- [ ] **Step 3: 职位管理**

移除 `handleDelete`、`deleteJob` import 和删除按钮。关闭状态只显示重新发布或无危险操作。

- [ ] **Step 4: 账号管理**

删除按钮改为“禁用”；点击后调用现有更新账号接口传 `{ id, status: 'DISABLED' }`，成功提示“账号已禁用”。禁用账号不显示禁用按钮。

- [ ] **Step 5: 验证小程序**

Run:

```bash
npm run build:mp-weixin
```

Expected: build succeeds; generated wxss has no invalid wildcard selector.

---

### Task 3: 企业 PC 规则

**Files:**
- Modify: `enterprise-pc/src/views/schedules/ScheduleShiftList.vue`
- Modify: `enterprise-pc/src/views/attendance/AttendanceHoursList.vue`
- Modify: `enterprise-pc/src/views/jobs/JobList.vue`
- Modify: `enterprise-pc/src/views/accounts/AccountList.vue`

- [ ] **Step 1: 排班考勤**

新增 `statusLabel` 或扩展现有状态渲染，`ABSENT` 显示“缺勤”；新增 `canCancelShift(row)`，只对未来未取消排班显示“取消排班”。

- [ ] **Step 2: 薪资管理**

表格选择列增加 `:selectable="row => row.settlementStatus !== 'PAID'"`；已结算行不显示结算和删除按钮，只保留已有撤回结算。

- [ ] **Step 3: 职位管理**

移除 `deleteJob` import、`handleDelete` 和删除按钮。关闭仍可用。

- [ ] **Step 4: 账号管理**

移除 `deleteAccount` import 和 `handleDelete`；新增 `handleDisable(row)` 调用 `updateAccount({ id: row.id, status: 'DISABLED' })`。ACTIVE 账号显示“禁用”，DISABLED 账号不显示禁用。

- [ ] **Step 5: 验证 PC**

Run:

```bash
npm run build
```

Expected: build succeeds.

---

### Task 4: 总体验证

- [ ] **Step 1: 后端测试**

Run in `enterprise-service`:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

- [ ] **Step 2: 小程序构建**

Run in `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

- [ ] **Step 3: PC 构建**

Run in `enterprise-pc`:

```bash
npm run build
```

- [ ] **Step 4: Git 检查**

Run from repo root:

```bash
git diff --check
git status --short --branch
```

---

## Self-Review

- 覆盖 5 条用户规则。
- 前端和后端均有对应任务。
- 不新增接口路径，优先复用现有更新接口。
- 不提交构建产物。
