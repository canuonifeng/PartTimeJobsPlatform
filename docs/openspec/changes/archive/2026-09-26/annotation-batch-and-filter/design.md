# Design: annotation-batch-and-filter

## 方案概述

在不重构标注抢单/任务单/结算链路的前提下，做三件事：
1. 企业端 PC 拆分"零工招聘"与"标注任务"两个独立模块（复用现有 Job 接口，靠 taskType 固化）；
2. 标注职位下新增"批次管理"入口，批次继续以 `job_schedules` 行承载，但日期/时段字段允许为空，提供独立的批次 CRUD + 上下线 + 进度统计接口；
3. C端找活页快捷筛选改造为固定入口（全部/零工/标注/附近/急招），后端 `jobs` 新增 `urgent` 字段并支持距离排序。

## 数据库变更（scripts/36_annotation_batch_and_filter.sql）

```sql
-- 1. jobs 表新增急招标记
ALTER TABLE jobs ADD COLUMN urgent TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否急招';

-- 2. job_schedules 日期/时段改为可空（标注批次不需要）
ALTER TABLE job_schedules MODIFY COLUMN schedule_date DATE NULL;
ALTER TABLE job_schedules MODIFY COLUMN start_time TIME NULL;
ALTER TABLE job_schedules MODIFY COLUMN end_time TIME NULL;

-- 3. job_schedules 新增批次代号（标注批次必填；零工班次可空）
ALTER TABLE job_schedules ADD COLUMN batch_code VARCHAR(64) NULL COMMENT '批次代号（标注批次）';
```

## 架构与模块

### 企业端 PC
- `views/jobs/JobList.vue`：改造为"零工招聘"（进入时**强制固定 taskType=WORK**，仅展示零工类型数据；移除任务类型下拉，仅保留标题/状态筛选；新建职位默认 WORK）
- `views/jobs/AnnotationJobList.vue`（新）："标注任务"模块——列表固定 taskType=ANNOTATION；操作含 新建/编辑/发布/下线/删除 + **批次管理**
- `views/jobs/AnnotationBatchList.vue`（新）：职位下批次管理抽屉/页面——列表（**批次代号**/数据量/外部批次ID/状态/已抢/已完成/进度）+ 新建/编辑/上线/下线
- `views/jobs/JobForm.vue`：增加"急招"开关（通用）；支持通过 query 参数预置任务类型
- `router/index.js`：新增 `/jobs/annotation`（标注任务）、`/jobs/annotation/:id/batches`（批次管理）；原 `/jobs` 语义改为零工
- `App.vue`：招聘管理子菜单拆为"零工招聘"（/jobs）与"标注任务"（/jobs/annotation）

### 后端（enterprise-service）
- 批次管理接口（新 Controller `AnnotationBatchController`，前缀 /api/enterprise/annotation-batches）：
  - POST /list {jobId} → 批次列表含已抢数/已完成数/进度
  - POST /create {jobId, batchCode, totalItems, externalBatchId} → 新建 ACTIVE 批次（**batchCode/totalItems 必填**；日期时段置空）
  - POST /update {id, batchCode, totalItems, externalBatchId}
  - POST /toggle {id, status} → ACTIVE/CANCELLED 上下线
- 复用/微调 `JobScheduleMapper`（按 jobId 查全部批次，含 status；新建标注批次时 schedule_date/start_time/end_time 置 null，batch_code 写批次代号，total_items 写数据量）
- 进度统计：按 schedule_id 聚合 annotation_task_orders（已抢=该批次任务单数，已完成=COMPLETED 数）
- Job 接口：create/update 支持 urgent 字段；列表已支持 taskType（复用）

### C端后端（c-service）
- `JobSummaryVO` / `JobDetailVO` 增加 `urgent` 字段
- 搜索接口扩展：`urgent`（true 过滤）、`sort`（distance → ORDER BY distance 升序）；taskType 已有
- 标注详情批次：`getJobDetail` 已返回 schedules（jobSchedules），新增 job.batches 别名视图或前端沿用 schedules（C端 jobDetailAnnotation 已兼容 `job.jobSchedules`/`job.batches`，确保真实数据返回）

### C端前端（worker-uniapp）
- `pages/jobs/jobList.vue`：顶部快捷筛选改为固定入口 `全部 / 零工 / 标注 / 附近 / 急招`（替换原"任务类型Tab + 全量分类平铺"，分类入口降级为次要或保留折叠）
- `api/jobs.js`：搜索参数扩展 urgent / sort

## 关键流程

1. 企业端新建标注职位 → JobForm（taskType 预置 ANNOTATION + urgent 开关）→ 保存 → 列表（标注模块）
2. 标注职位 → 批次管理 → 新建批次（**批次代号/数据量**/外部批次ID）→ job_schedules 行（日期时段 null, ACTIVE, batch_code/total_items 落库）
3. 工人 C端 → 选"标注"/"全部" → 详情页显示 ACTIVE 批次 → 抢单（复用 grabTaskOrder，scheduleIds 关联批次）
4. 下线批次 → CANCELLED → C端详情不再展示 → 已抢任务单不受影响
5. C端选"急招" → searchJobs(urgent=true) → 返回已发布且急招职位
6. C端选"附近" → searchJobs(sort=distance, 带经纬度) → 距离升序

## 接口定义

| 接口 | 说明 |
|---|---|
| POST /api/enterprise/annotation-batches/list {jobId} | 批次列表（含进度） |
| POST /api/enterprise/annotation-batches/create {jobId,batchCode,totalItems,externalBatchId} | 新建批次（代号/数据量必填） |
| POST /api/enterprise/annotation-batches/update {id,batchCode,totalItems,externalBatchId} | 编辑批次 |
| POST /api/enterprise/annotation-batches/toggle {id,status} | 上线/下线 |
| GET /api/enterprise/jobs?taskType=ANNOTATION&urgent=... | 标注职位列表（复用） |
| GET /api/worker/jobs?taskType=&urgent=&sort=distance&latitude=&longitude= | C端搜索（扩展） |

## 对现有代码的影响

- `JobScheduleMapper`/XML：新增 findByIds、countByScheduleIds 等（部分已有）；新建批次方法允许日期时段 null
- `JobController`/`JobServiceImpl`：create/update 增加 urgent；列表已支持 taskType
- c-service `JobController`/`JobServiceImpl`：搜索参数扩展 urgent/sort；VO 加 urgent
- enterprise-pc：JobList 拆零工、新增 AnnotationJobList + AnnotationBatchList、JobForm 加急招开关、路由与菜单
- worker-uniapp：jobList 快捷筛选改造、api 扩展

## 备选方案

- **批次独立建表**（annotation_batches）：更干净，但要改任务单/抢单/回调/结算的关联（schedule_id → batch_id），影响面大；本次选复用 job_schedules，零工/标注共用，日期时段可空，风险最低。
- **急招=系统规则**（如报名数/浏览量阈值）：不可控、难解释；选企业主动标记，语义清晰。
- **附近=限定半径**：需要定义阈值；选距离升序，无需阈值且符合直觉。
