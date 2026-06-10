# Entity 重构 + SQL 优化 — 设计文档

## 架构概览

```
改造前：
  Entity = 表字段 + JOIN 字段（冗余）
  Mapper = 多表 JOIN 查询
  Service = 直接使用 Entity 的 JOIN 字段

改造后：
  Entity = 仅表字段（纯净）
  Mapper = 单表查询
  Service = 批量加载 + 内存关联 → 填充 VO
  VO = 表字段 + 关联字段（保留）
```

## 数据流对比

### 改造前（以 Job 查询为例）

```
JobMapper.findById()
  → SELECT j.*, e.company_name, e.company_logo, jc.name AS category_name
    FROM jobs j
    LEFT JOIN enterprises e ON j.company_id = e.id
    LEFT JOIN job_categories jc ON j.category_id = jc.id
  → Job entity 含 companyName, categoryName（冗余）
```

### 改造后

```
JobMapper.findById()
  → SELECT * FROM jobs WHERE id = ?
  → Job entity 纯净

JobService.findById()
  → job = jobMapper.findById(id)
  → company = enterpriseMapper.findById(job.getCompanyId())
  → category = jobCategoryMapper.findById(job.getCategoryId())
  → jobVO = fillJobVO(job, company, category)
```

## Entity 冗余字段清单

### c-service

| Entity | 冗余字段 | 来源 |
|--------|----------|------|
| `Job` | `jobId` | `id` 的别名 |
| `Job` | `companyName` | JOIN `enterprises.company_name` |
| `Job` | `companyLogo` | JOIN `enterprises.company_logo` |
| `Job` | `categoryName` | JOIN `job_categories.name` |
| `Job` | `tags` | Collection from `job_tag_relations` |
| `Job` | `rates` | Collection from `job_rates` |
| `Job` | `schedules` | Collection from `job_schedules` |
| `Job` | `imageUrl` | 未映射到 DB |
| `ShiftEntity` | `jobTitle` | JOIN `jobs.title` |
| `ShiftEntity` | `jobLocation` | JOIN `jobs.location` |
| `AttendanceRecordEntity` | `jobTitle` | JOIN `jobs.title` |
| `AttendanceRecordEntity` | `companyName` | JOIN `enterprises.company_name` |
| `AttendanceRecordEntity` | `location` | COALESCE from `schedule_shifts`/`jobs` |
| `AttendanceRecordEntity` | `shiftDate` | JOIN `schedule_shifts.shift_date` |
| `AttendanceRecordEntity` | `startTime` | JOIN `schedule_shifts.start_time` |
| `AttendanceRecordEntity` | `endTime` | JOIN `schedule_shifts.end_time` |
| `BalanceTransaction` | `jobTitle` | Complex JOIN chain |
| `BalanceTransaction` | `companyName` | Complex JOIN chain |
| `BalanceTransaction` | `location` | Complex JOIN chain |
| `BalanceTransaction` | `shiftDate` | JOIN `schedule_shifts.shift_date` |
| `BalanceTransaction` | `startTime` | JOIN `schedule_shifts.start_time` |
| `BalanceTransaction` | `endTime` | JOIN `schedule_shifts.end_time` |
| `BalanceTransaction` | `totalHours` | COALESCE from `attendance_records` |
| `BalanceTransaction` | `settlementStatus` | COALESCE from `attendance_records` |
| `Worker` | `nickname` | NOT in DB |
| `Worker` | `avatar` | NOT in DB |

### enterprise-service

| Entity | 冗余字段 | 来源 |
|--------|----------|------|
| `Job` | `tagIds` | Derived from `job_tag_relations` |
| `Job` | `tags` | Collection from `job_tag_relations` |

### platform-service

| Entity | 冗余字段 | 来源 |
|--------|----------|------|
| `WithdrawalRecord` | `workerName` | JOIN `c_worker.name` |
| `WithdrawalRecord` | `workerPhone` | JOIN `c_worker.phone` |

## N+1 查询修复清单

| # | 位置 | 修复方案 |
|---|------|----------|
| 1 | `SettlementServiceImpl.payFromAttendanceRecords()` | 批量 `findByIds()` + `findByWorkerIds()` 替代循环单条查询 |
| 2 | `AttendanceHoursController.batchDelete()` | `SELECT id FROM ... WHERE id IN (...) AND status='PAID'` 一次校验 |
| 3 | `ScheduleServiceImpl.toShiftResponse()` | 批量加载 job/worker 数据，内存关联 |

## 设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| Entity 是否保留 JOIN 字段 | 否，全部移除 | Entity 语义是"表的一行数据"，不应包含关联数据 |
| VO 是否保留关联字段 | 是，保持不变 | VO 是"前端需要的展示数据"，可以包含关联数据 |
| JOIN 拆分方式 | Service 层批量加载 + 内存关联 | 避免 N+1，保持查询效率 |
| 是否需要新 VO 类 | 尽量复用现有 VO | 减少变更范围 |
| 循环 INSERT 是否改 batch | 是，使用 `<foreach>` 批量插入 | 减少 DB 往返次数 |

## 风险与缓解

| 风险 | 缓解 |
|------|------|
| 改动范围大，可能引入回归 | 每个 Task 独立验证，保持现有测试通过 |
| Service 层内存关联增加内存占用 | 数据量有限（分页查询），影响可忽略 |
| VO 字段依赖 Entity 冗余字段 | 先检查所有 VO 的赋值来源，确保平滑迁移 |
