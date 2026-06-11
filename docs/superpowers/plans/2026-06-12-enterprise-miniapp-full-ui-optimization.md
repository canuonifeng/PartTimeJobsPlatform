# 企业端小程序全量页面 UI 优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 只在 `enterprise-uniapp` 内完成除报名管理、排班考勤外的全量页面布局、排版和交互优化。

**Architecture:** 先补全企业小程序全局 UI 原子类，再按页面类型改造：职位页、表单页、列表页、资金/设置/认证/登录/首页。只改企业小程序端模板、样式和必要提交态，不改 API、后端、PC 端、工人端。

**Tech Stack:** UniApp + Vue 3 + 微信小程序构建，样式使用 rpx、Flex/Grid、已有 CSS 变量和页面级 style。

---

## File Structure

- Modify: `enterprise-uniapp/src/App.vue` — 全局 UI 原子类。
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue` — 职位列表。
- Modify: `enterprise-uniapp/src/pages/jobs/jobDetail.vue` — 职位详情。
- Modify: `enterprise-uniapp/src/pages/jobs/jobForm.vue` — 职位表单。
- Modify: `enterprise-uniapp/src/pages/templates/templateList.vue` — 模板列表。
- Modify: `enterprise-uniapp/src/pages/templates/templateForm.vue` — 模板表单。
- Modify: `enterprise-uniapp/src/pages/locations/locationList.vue` — 地点列表。
- Modify: `enterprise-uniapp/src/pages/locations/locationForm.vue` — 地点表单。
- Modify: `enterprise-uniapp/src/pages/attendance/attendanceList.vue` — 薪资管理。
- Modify: `enterprise-uniapp/src/pages/workers/workerList.vue` — 兼职管理。
- Modify: `enterprise-uniapp/src/pages/accounts/accountList.vue` — 账号管理。
- Modify: `enterprise-uniapp/src/pages/balance/balanceList.vue` — 企业资金。
- Modify: `enterprise-uniapp/src/pages/settings/companySettings.vue` — 企业设置。
- Modify: `enterprise-uniapp/src/pages/auth/realName.vue` — 企业实名认证。
- Modify: `enterprise-uniapp/src/pages/home/index.vue` — 首页细节统一。
- Modify: `enterprise-uniapp/src/pages/login/login.vue` — 登录页细节统一。
- Do not modify: `enterprise-uniapp/src/pages/applications/applicationList.vue`。
- Do not modify: `enterprise-uniapp/src/pages/schedules/scheduleList.vue`。

---

### Task 1: Global UI primitives

**Files:**
- Modify: `enterprise-uniapp/src/App.vue:41-86`

- [ ] **Step 1: Add global classes**

Append these classes inside the existing `<style>` block after `.btn-primary[disabled]`:

```css
.e-page { min-height: 100vh; background: var(--color-bg); color: var(--color-text); box-sizing: border-box; }
.e-header { background: linear-gradient(135deg, #18c86b 0%, #08a95a 56%, #078a49 100%); padding: 48rpx 32rpx 32rpx; border-bottom-left-radius: 36rpx; border-bottom-right-radius: 36rpx; color: #fff; box-sizing: border-box; }
.e-header-row { display: flex; align-items: center; justify-content: space-between; gap: 20rpx; min-width: 0; }
.e-header-title { font-size: 36rpx; font-weight: 800; line-height: 1.3; color: #fff; }
.e-header-desc { display: block; margin-top: 10rpx; font-size: 24rpx; line-height: 1.5; color: rgba(255,255,255,.82); }
.e-content { padding: 24rpx 28rpx 40rpx; box-sizing: border-box; }
.e-card { background: var(--color-card); border-radius: var(--radius-card); padding: 28rpx; box-shadow: var(--shadow-card); box-sizing: border-box; overflow: hidden; }
.e-card + .e-card { margin-top: 22rpx; }
.e-card-title-row { display: flex; align-items: center; justify-content: space-between; gap: 18rpx; min-width: 0; }
.e-card-title { flex: 1; min-width: 0; font-size: 31rpx; font-weight: 800; color: var(--color-text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.e-card-subtitle { display: block; margin-top: 8rpx; font-size: 24rpx; color: var(--color-text-secondary); line-height: 1.5; }
.e-badge { flex-shrink: 0; padding: 7rpx 18rpx; border-radius: var(--radius-badge); font-size: 22rpx; font-weight: 700; line-height: 1.2; }
.e-badge-green { background: #e7f8ef; color: #08a857; }
.e-badge-orange { background: #fff7df; color: #d28a00; }
.e-badge-red { background: #ffecec; color: #e5484d; }
.e-badge-blue { background: #edf5ff; color: #3b82f6; }
.e-badge-gray { background: #eef1f0; color: #7b8580; }
.e-info-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14rpx; margin-top: 20rpx; }
.e-info-pill { min-width: 0; padding: 14rpx 16rpx; background: #f8faf9; border-radius: 16rpx; box-sizing: border-box; }
.e-info-label { display: block; font-size: 22rpx; color: var(--color-text-muted); line-height: 1.4; }
.e-info-value { display: block; margin-top: 6rpx; font-size: 26rpx; font-weight: 700; color: var(--color-text); line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.e-action-row { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 22rpx; }
.e-action-pill { flex: 1 1 150rpx; min-width: 0; height: 64rpx; line-height: 64rpx; text-align: center; border-radius: 999rpx; font-size: 25rpx; font-weight: 700; box-sizing: border-box; }
.e-action-primary { background: #e7f8ef; color: #08a857; }
.e-action-blue { background: #edf5ff; color: #3b82f6; }
.e-action-orange { background: #fff7df; color: #d28a00; }
.e-action-red { background: #ffecec; color: #e5484d; }
.e-action-gray { background: #eef1f0; color: #7b8580; }
.e-empty { padding: 100rpx 36rpx; text-align: center; color: var(--color-text-muted); }
.e-empty-title { display: block; font-size: 30rpx; font-weight: 700; color: var(--color-text-secondary); }
.e-empty-desc { display: block; margin-top: 10rpx; font-size: 25rpx; color: var(--color-text-muted); }
.e-form-section { background: #fff; border-radius: var(--radius-card); padding: 28rpx; box-shadow: var(--shadow-card); box-sizing: border-box; margin-bottom: 22rpx; }
.e-section-title { display: block; margin-bottom: 22rpx; font-size: 30rpx; font-weight: 800; color: var(--color-text); }
.e-form-row { margin-bottom: 24rpx; }
.e-form-row:last-child { margin-bottom: 0; }
.e-form-label { display: block; margin-bottom: 12rpx; font-size: 26rpx; font-weight: 700; color: var(--color-text-secondary); }
.e-input, .e-textarea, .e-picker-value { width: 100%; min-height: 84rpx; padding: 0 22rpx; border: 2rpx solid var(--color-border); border-radius: 18rpx; background: #f8faf9; box-sizing: border-box; font-size: 28rpx; color: var(--color-text); }
.e-textarea { min-height: 180rpx; padding-top: 20rpx; line-height: 1.6; }
.e-picker-value { display: flex; align-items: center; justify-content: space-between; }
.e-bottom-safe { padding-bottom: calc(40rpx + env(safe-area-inset-bottom)); }
```

- [ ] **Step 2: Verify global primitives**

Run from `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

Expected: build succeeds.

---

### Task 2: Jobs pages

**Files:**
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Modify: `enterprise-uniapp/src/pages/jobs/jobDetail.vue`

- [ ] **Step 1: Update `jobList.vue`**

Keep script logic unchanged. Use `page e-page`, `header e-header`, `e-header-title`, `e-header-desc`, `content e-content`. Job cards use `e-card`, `e-card-title-row`, `e-badge`, `e-info-grid`, `e-action-row`, `e-action-pill`. Loading/empty states use `e-empty`.

- [ ] **Step 2: Update `jobDetail.vue`**

Keep route loading and existing actions unchanged. Group detail fields into cards named `基本信息`、`薪资与人数`、`工作时间`、`工作地点`、`岗位要求`. Detail values must use `min-width: 0; word-break: break-all;`.

- [ ] **Step 3: Verify jobs pages**

Run from `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

Expected: build succeeds; no edits to `pages/applications/applicationList.vue` or `pages/schedules/scheduleList.vue`.

---

### Task 3: Form pages

**Files:**
- Modify: `enterprise-uniapp/src/pages/jobs/jobForm.vue`
- Modify: `enterprise-uniapp/src/pages/templates/templateForm.vue`
- Modify: `enterprise-uniapp/src/pages/locations/locationForm.vue`

- [ ] **Step 1: Preserve handlers**

Do not rename `formData`, `saving`, `submitForm`, picker handlers, editor handlers, or API calls. Only change layout classes and disabled/loading button text.

- [ ] **Step 2: Apply form pattern**

Wrap related fields in `e-form-section`. Inputs use `e-form-row`, `e-form-label`, `e-input`. Textareas/editors use matching `e-textarea` visual sizing. Pickers use `e-picker-value`. Submit buttons use `btn-primary submit-btn`, `:disabled="saving"`, and loading text `保存中...`.

- [ ] **Step 3: Verify form pages**

Run from `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

Expected: build succeeds; create/edit behavior remains wired to existing handlers.

---

### Task 4: Template, location, worker, account list pages

**Files:**
- Modify: `enterprise-uniapp/src/pages/templates/templateList.vue`
- Modify: `enterprise-uniapp/src/pages/locations/locationList.vue`
- Modify: `enterprise-uniapp/src/pages/workers/workerList.vue`
- Modify: `enterprise-uniapp/src/pages/accounts/accountList.vue`

- [ ] **Step 1: Apply list skeleton**

Each page uses `page e-page`, `header e-header`, `e-header-title`, `e-header-desc`, and `content e-content`.

- [ ] **Step 2: Apply card and action pattern**

Each item card uses `e-card`. Top row uses title plus status badge. Middle uses `e-info-grid`. Actions use `e-action-row` and semantic classes: primary for新增/使用/启用, blue for查看/编辑, orange for禁用, red for删除.

- [ ] **Step 3: Preserve destructive confirmations**

Keep existing `uni.showModal` blocks for delete/disable operations. Do not convert destructive actions into direct API calls.

- [ ] **Step 4: Verify list pages**

Run from `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

Expected: build succeeds and list pages have no `width: 100vw` or unbounded long text.

---

### Task 5: Salary, balance, settings, auth, home, login

**Files:**
- Modify: `enterprise-uniapp/src/pages/attendance/attendanceList.vue`
- Modify: `enterprise-uniapp/src/pages/balance/balanceList.vue`
- Modify: `enterprise-uniapp/src/pages/settings/companySettings.vue`
- Modify: `enterprise-uniapp/src/pages/auth/realName.vue`
- Modify: `enterprise-uniapp/src/pages/home/index.vue`
- Modify: `enterprise-uniapp/src/pages/login/login.vue`

- [ ] **Step 1: Salary page**

Remove unnecessary nested page-height `scroll-view` unless required for `scrolltolower`. Add a summary card above records with `记录数` = `records.length` and `已选` = `selectedIds.length`. Preserve `refreshRecords`, `loadMore`, `handleBatchPay`, `handleBatchDelete`.

- [ ] **Step 2: Balance page**

Use a top balance card for available amount and `e-card` for each transaction. Positive amounts use green text; negative amounts use red text. Preserve existing API calls.

- [ ] **Step 3: Settings and real-name pages**

Use `e-form-section` for company info/logo/认证信息. Use `btn-primary` for save/submit. If a submit function lacks `saving`, add `const saving = ref(false)` and guard duplicate submit with `if (saving.value) return`.

- [ ] **Step 4: Home and login details**

Do not reintroduce negative content margin on home. Align cards and menu entries with `24rpx` spacing, `24rpx` radius, and `box-sizing: border-box`. Login inputs use the same `e-input` visual style without changing auth flow.

- [ ] **Step 5: Verify final page batch**

Run from `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

Expected: build succeeds.

---

### Task 6: Final verification and review

**Files:**
- Inspect modified files under `enterprise-uniapp/src` and planning docs.

- [ ] **Step 1: Confirm protected pages unchanged**

Run from repo root:

```bash
git diff -- enterprise-uniapp/src/pages/applications/applicationList.vue enterprise-uniapp/src/pages/schedules/scheduleList.vue
```

Expected: no output.

- [ ] **Step 2: Confirm no unrelated app changes**

Run from repo root:

```bash
git diff --name-only
```

Expected: changed implementation files are only under `enterprise-uniapp/`; documentation changes are under `docs/superpowers/`.

- [ ] **Step 3: Final build**

Run from `enterprise-uniapp`:

```bash
npm run build:mp-weixin
```

Expected: build succeeds.

- [ ] **Step 4: Whitespace check**

Run from repo root:

```bash
git diff --check
```

Expected: no output.

- [ ] **Step 5: Do not commit automatically**

Do not run `git commit` unless the user explicitly asks for a commit.

---

## Self-Review

- Spec coverage: covers enterprise-uniapp-only scope, all listed page groups, form/list/detail/empty/loading/destructive action rules, and build verification.
- Placeholder scan: no placeholder or deferred implementation sections.
- Type consistency: plan preserves existing Vue refs/functions and introduces only CSS utility classes.
