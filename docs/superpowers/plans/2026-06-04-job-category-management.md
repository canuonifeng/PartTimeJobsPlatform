# Job Category Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Centralize job category management in the platform and make C-side and enterprise-side category selectors read active categories from backend APIs.

**Architecture:** Reuse the existing shared `job_categories` table and add a soft `status` column. Platform service/PC manages all categories including disabled ones, while C service and enterprise service expose active-only category trees for frontend selection and filtering.

**Tech Stack:** Spring Boot services, MyBatis XML mappers, MySQL, Vue 3/Element Plus platform PC, UniApp worker and enterprise miniapps.

---

## Task 1: Database Status Column and Data Backfill

**Files:** none required unless adding SQL script is desired later.

- [ ] Check whether `job_categories.status` exists in local MySQL `part_time_work`.
- [ ] If missing, run: `ALTER TABLE job_categories ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';`
- [ ] Verify existing rows are active: `SELECT id,name,parent_id,sort_order,status FROM job_categories ORDER BY sort_order,id;`

## Task 2: Platform Service Category Status Support

**Files:**
- Modify `platform-service/src/main/java/com/parttime/platform/pojo/entity/JobCategory.java`
- Modify `platform-service/src/main/java/com/parttime/platform/pojo/cmd/JobCategoryCmd.java`
- Modify `platform-service/src/main/java/com/parttime/platform/pojo/vo/JobCategoryVO.java`
- Modify `platform-service/src/main/java/com/parttime/platform/service/impl/JobCategoryServiceImpl.java`
- Modify `platform-service/src/main/resources/mapper/JobCategoryMapper.xml`

- [ ] Add `String status` to entity/cmd/vo.
- [ ] Include `status` in resultMap, insert, update.
- [ ] Default create status to `ACTIVE` if request status is blank/null.
- [ ] Keep admin `findAll` returning all statuses.
- [ ] Change `deleteCategory(id)` implementation to load the category, set status `DISABLED`, and update it instead of physical delete.
- [ ] Compile platform service with JDK 17.

## Task 3: Platform PC Category Management UI

**Files:**
- Modify `platform-pc/src/views/categories/JobCategoryList.vue`

- [ ] Add status column showing `启用`/`禁用`.
- [ ] Add status selector in create/edit dialog.
- [ ] Initialize new category form with `status: 'ACTIVE'`.
- [ ] Replace delete button label/confirmation with disable behavior for active rows.
- [ ] Add enable action for disabled rows using existing update API with `status: 'ACTIVE'`.
- [ ] Build platform PC if package scripts are available.

## Task 4: Enterprise Service Active-Only Categories

**Files:**
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/JobCategory.java`
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/JobCategoryCmd.java`
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobCategoryVO.java`
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobCategoryServiceImpl.java`
- Modify `enterprise-service/src/main/resources/mapper/JobCategoryMapper.xml`

- [ ] Add `String status` to entity/cmd/vo.
- [ ] Include `status` in resultMap, insert, update.
- [ ] Change public category list to read active categories only.
- [ ] Preserve tree shape and sort order.
- [ ] Compile enterprise service with JDK 17.

## Task 5: C Service Active Category API

**Files:**
- Create `c-service/src/main/java/com/parttime/cservice/controller/JobCategoryController.java`
- Create `c-service/src/main/java/com/parttime/cservice/service/JobCategoryService.java`
- Create `c-service/src/main/java/com/parttime/cservice/service/impl/JobCategoryServiceImpl.java`
- Create `c-service/src/main/java/com/parttime/cservice/mapper/JobCategoryMapper.java`
- Create `c-service/src/main/java/com/parttime/cservice/pojo/entity/JobCategory.java`
- Create `c-service/src/main/java/com/parttime/cservice/pojo/vo/JobCategoryVO.java`
- Create `c-service/src/main/resources/mapper/JobCategoryMapper.xml`

- [ ] Implement `GET /api/job-categories` returning active category tree.
- [ ] Mapper reads `WHERE status = 'ACTIVE' ORDER BY sort_order ASC, id ASC`.
- [ ] VO fields: `id`, `name`, `parentId`, `sortOrder`, `status`, `children`.
- [ ] Compile c-service with JDK 17.

## Task 6: Worker Miniapp Dynamic Category Tabs

**Files:**
- Modify `worker-uniapp/src/api/jobs.js`
- Modify `worker-uniapp/src/pages/jobs/jobList.vue`

- [ ] Add `getCategories()` -> `GET /api/job-categories` using existing request wrapper.
- [ ] Replace hardcoded `categories` array with reactive categories initialized to `[{ id: undefined, name: '全部' }]`.
- [ ] On mount, load categories from backend and prepend `全部`.
- [ ] Flatten category tree for horizontal tabs, preserving child categories after parent labels.
- [ ] If loading fails, keep only `全部`.
- [ ] Existing category filter continues passing selected `categoryId` to `getJobs`.
- [ ] Run `npm run build:mp-weixin` in `worker-uniapp`.

## Task 7: Enterprise Miniapp Category Picker Normalization

**Files:**
- Modify `enterprise-uniapp/src/pages/jobs/jobForm.vue`
- Modify `enterprise-uniapp/src/pages/templates/templateForm.vue`
- Modify `enterprise-uniapp/src/pages/templates/templateList.vue` if needed

- [ ] Ensure category API data is flattened from tree before picker usage.
- [ ] Picker labels should show child categories clearly; parent categories can be shown as `父级 / 子级` if needed.
- [ ] Existing `categoryId` saved on create/update remains unchanged.
- [ ] Disabled categories do not appear because enterprise service returns active-only data.
- [ ] Build enterprise miniapp if package scripts are available.

## Task 8: Verification

- [ ] Verify DB status column exists and sample categories have statuses.
- [ ] Compile `platform-service`.
- [ ] Compile `enterprise-service`.
- [ ] Compile `c-service`.
- [ ] Build `worker-uniapp` with `npm run build:mp-weixin`.
- [ ] Build platform/enterprise frontends where scripts exist.
- [ ] Search for hardcoded worker category array names and ensure job list no longer uses hardcoded job type tabs.
- [ ] Confirm disabled categories are visible in platform admin but absent from C/enterprise selectors.

## Self Review

- Preserves parent-child category structure.
- Adds soft disable instead of hard delete.
- C and enterprise clients consume backend categories.
- Existing jobs keep historical category IDs.
