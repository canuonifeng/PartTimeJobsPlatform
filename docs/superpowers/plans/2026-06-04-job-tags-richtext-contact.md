# Job Tags Rich Text Contact Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增运营后台标签管理、岗位多选标签、岗位职责/任职要求富文本、岗位联系方式。

**Architecture:** 使用规范化表：`job_tag_groups`、`job_tags`、`job_tag_relations`；平台端管理全部标签，企业端和 C 端只读取启用标签。`jobs.description` 继续承载“岗位职责”以兼容旧数据，新增 `jobs.requirements` 和 `jobs.contact_phone`。

**Tech Stack:** Spring Boot + MyBatis XML + MySQL；平台 PC Vue3 + Element Plus；企业/C 端 uni-app Vue。

---

## File Structure

- Create: `scripts/13_job_tags_richtext_contact.sql`
- Modify: `scripts/1_from-database.sql`
- Create/modify: `platform-service/src/main/java/com/parttime/platform/**/JobTag*.java`
- Create: `platform-service/src/main/resources/mapper/JobTagMapper.xml`
- Create: `platform-pc/src/api/jobTags.js`
- Create: `platform-pc/src/views/tags/JobTagList.vue`
- Modify: `platform-pc/src/router/index.js` and current menu/layout file
- Create/modify: `enterprise-service/src/main/java/com/parttime/enterprise/**/JobTag*.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/Job.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/JobCreateCmd.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/UpdateJobCmd.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobVO.java`
- Modify: `enterprise-service/src/main/resources/mapper/JobMapper.xml`
- Modify: `enterprise-uniapp/src/api/jobs.js`
- Modify: `enterprise-uniapp/src/pages/jobs/jobForm.vue`
- Modify: `c-service/src/main/java/com/parttime/cservice/**/Job*.java`
- Modify: `c-service/src/main/resources/mapper/JobMapper.xml`
- Modify: `worker-uniapp/src/pages/jobs/jobList.vue`
- Modify: `worker-uniapp/src/pages/jobs/jobDetail.vue`

## Task 1: Database

- [ ] Create `scripts/13_job_tags_richtext_contact.sql` with:

```sql
CREATE TABLE IF NOT EXISTS job_tag_groups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(100) NOT NULL UNIQUE,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='岗位标签组';

CREATE TABLE IF NOT EXISTS job_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_job_tags_group_code (group_id, code),
  KEY idx_job_tags_group_id (group_id)
) COMMENT='岗位标签';

CREATE TABLE IF NOT EXISTS job_tag_relations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_job_tag_relations_job_tag (job_id, tag_id),
  KEY idx_job_tag_relations_job_id (job_id),
  KEY idx_job_tag_relations_tag_id (tag_id)
) COMMENT='岗位标签关联';

ALTER TABLE jobs ADD COLUMN requirements LONGTEXT NULL COMMENT '任职要求富文本';
ALTER TABLE jobs ADD COLUMN contact_phone VARCHAR(30) NULL COMMENT '岗位联系方式';

INSERT INTO job_tag_groups (name, code, sort_order, status)
SELECT '结算周期', 'settlement_cycle', 10, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM job_tag_groups WHERE code = 'settlement_cycle');
INSERT INTO job_tag_groups (name, code, sort_order, status)
SELECT '结算方式', 'settlement_method', 20, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM job_tag_groups WHERE code = 'settlement_method');

INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '日结', 'daily', 10, 'ACTIVE' FROM job_tag_groups g WHERE g.code = 'settlement_cycle'
AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'daily');
INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '周结', 'weekly', 20, 'ACTIVE' FROM job_tag_groups g WHERE g.code = 'settlement_cycle'
AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'weekly');
INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '月结', 'monthly', 30, 'ACTIVE' FROM job_tag_groups g WHERE g.code = 'settlement_cycle'
AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'monthly');
INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '计件', 'piecework', 10, 'ACTIVE' FROM job_tag_groups g WHERE g.code = 'settlement_method'
AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'piecework');
INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '按时', 'hourly', 20, 'ACTIVE' FROM job_tag_groups g WHERE g.code = 'settlement_method'
AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'hourly');
```

- [ ] Update `scripts/1_from-database.sql` with the same tables and new `jobs` columns.
- [ ] Apply migration locally and verify the new tables/columns exist.

## Task 2: Platform Service Tag APIs

- [ ] Add failing tests for grouping/sorting and soft-disable behavior.
- [ ] Create `JobTagGroup`, `JobTag`, `JobTagGroupCmd`, `JobTagCmd`, `JobTagGroupVO`, `JobTagVO`.
- [ ] Create mapper + XML for listing, insert, update, soft disable group/tag.
- [ ] Create service that returns groups with nested tags sorted by `sort_order, id`.
- [ ] Create controller endpoints:
  - `GET /admin/job-tags`
  - `POST /admin/job-tag-groups`
  - `PUT /admin/job-tag-groups?id={id}`
  - `DELETE /admin/job-tag-groups?id={id}`
  - `POST /admin/job-tags`
  - `PUT /admin/job-tags?id={id}`
  - `DELETE /admin/job-tags?id={id}`
- [ ] Verify: `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile` in `platform-service`.

## Task 3: Platform PC Tag Management

- [ ] Add source assertion that `src/views/tags/JobTagList.vue` contains `标签管理`、`新增标签组`、`新增标签`.
- [ ] Add `platform-pc/src/api/jobTags.js` for all platform tag endpoints.
- [ ] Add `platform-pc/src/views/tags/JobTagList.vue`, following `JobCategoryList.vue` style.
- [ ] UI supports create/edit/disable tag groups and tags; fields: name, code, sortOrder, status.
- [ ] Add route `/job-tags` and menu item `标签管理`.
- [ ] Verify: `npm run build` in `platform-pc`; then restore generated `dist` changes.

## Task 4: Enterprise Service Job Tags, Rich Text, Contact Phone

- [ ] Add failing test: create/update job persists `requirements`, `contactPhone`, and `tagIds`.
- [ ] Add fields to Job entity/CMD/VO:

```java
private String requirements;
private String contactPhone;
private List<Long> tagIds;
private List<JobTagVO> tags;
```

- [ ] Keep `description` but relabel meaning as 岗位职责.
- [ ] Update `JobMapper.xml` insert/update/resultMap with `requirements` and `contact_phone`.
- [ ] Create `JobTagRelationMapper` with delete/insert/find methods.
- [ ] In create/update service, save job then replace tag relations from deduplicated `tagIds`.
- [ ] In detail/list VO assembly, populate `tagIds` and `tags`.
- [ ] Add `GET /api/job-tags` returning only active groups/tags for enterprise app.
- [ ] Verify: `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile` in `enterprise-service`.

## Task 5: Enterprise Miniapp Job Form

- [ ] Add source assertion: form contains `岗位职责`、`任职要求`、`联系方式`、`selectedTagIds` and `editor`.
- [ ] Add `getJobTags()` in `enterprise-uniapp/src/api/jobs.js`.
- [ ] Load active tag groups on page load.
- [ ] Replace “职位描述” label with “岗位职责”.
- [ ] Use uni-app `editor` for responsibilities and requirements; store HTML in `formData.description` and `formData.requirements`.
- [ ] Add contact phone input bound to `formData.contactPhone`.
- [ ] Add tag group sections with multi-select tag chips; selected IDs stored in `selectedTagIds`.
- [ ] Include `requirements`, `contactPhone`, `tagIds` in create/update payload.
- [ ] On edit, hydrate `requirements`, `contactPhone`, `tagIds` from job detail.
- [ ] Verify: `npm run build:mp-weixin` in `enterprise-uniapp`.

## Task 6: C Service Job Detail/List Exposure

- [ ] Add failing test: job detail VO returns `requirements`, `contactPhone`, and selected tags.
- [ ] Add `requirements`, `contactPhone`, `tags` to C-side Job entity/VOs.
- [ ] Update C-side `JobMapper.xml` result maps and select SQL.
- [ ] Add relation mapper to fetch tags for job list/detail.
- [ ] Ensure job search still searches title and responsibilities (`description`).
- [ ] Verify: `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile` in `c-service`.

## Task 7: Worker Miniapp Display

- [ ] Add source assertion: detail page uses `rich-text` for responsibilities and requirements, phone uses `contactPhone`, list uses backend tags when available.
- [ ] Update job list item type to include `tags?: { id: number, name: string, groupName?: string }[]`.
- [ ] Update `getSettlementTags(job)` to return `job.tags.map(t => t.name)` first, then legacy fallback.
- [ ] Update detail page duties/needs rendering to use `rich-text` for HTML content; fallback remains for old plain text.
- [ ] Update phone action priority to `job.contactPhone || job.phone || job.contactPhone || job.mobile` with duplicate removed in implementation.
- [ ] Verify: `npm run build:mp-weixin` in `worker-uniapp`.

## Final Verification

- [ ] `git diff --check`
- [ ] `platform-service`: `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile`
- [ ] `enterprise-service`: `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile`
- [ ] `c-service`: `JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests compile`
- [ ] `platform-pc`: `npm run build`, then restore `dist`
- [ ] `enterprise-uniapp`: `npm run build:mp-weixin`
- [ ] `worker-uniapp`: `npm run build:mp-weixin`

## Commit

- [ ] Review `git status --short` and `git diff --stat`.
- [ ] Commit intended files only:

```bash
git add scripts platform-service platform-pc enterprise-service enterprise-uniapp c-service worker-uniapp
git commit -m "feat: add job tags and rich text fields"
```
