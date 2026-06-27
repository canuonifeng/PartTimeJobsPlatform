## Why

当前后端存在两个结构性问题：

1. **SQL JOIN 过多**：多个 Mapper 使用 3-7 个 LEFT JOIN 从关联表取数据，导致查询复杂、性能差、维护困难。例如 `BalanceTransactionMapper` 使用 7 个 LEFT JOIN + 关联子查询，`JobMapper` 使用 3 个 JOIN。
2. **Entity 字段冗余**：Entity 类中混入了 JOIN 查询产生的关联字段（如 `Job.companyName`、`ShiftEntity.jobTitle`、`AttendanceRecordEntity.shiftDate` 等），这些字段不属于对应数据库表，导致 Entity 与表结构不对应，代码语义混乱。

需要将 Entity 严格对齐数据库表字段，同时将 JOIN 查询拆分为单表查询，在 Service 层组装数据。

## What Changes

- **Entity 清理**：移除所有 Entity 中不属于对应数据库表的冗余字段
- **Mapper 拆分**：将多表 JOIN 查询拆分为单表查询，Service 层批量加载后内存关联
- **VO 保持不变**：VO 仍可包含关联数据，但数据来源从 SQL JOIN 改为 Service 层组装
- **N+1 修复**：将循环内的逐条查询改为批量 IN 查询

## Capabilities

### Modified Capabilities

- `job-management`：Job 查询不再 JOIN enterprises/job_categories，改为单表查询 + Service 层关联
- `schedule-attendance`：排班/考勤查询不再 JOIN jobs/enterprises，改为单表查询 + Service 层关联
- `income-withdrawal`：收入记录查询不再使用 7 个 LEFT JOIN，改为单表查询 + Service 层关联

## Impact

- **Entity 类**：
  - c-service：`Job`（移除 8 字段）、`ShiftEntity`（移除 2 字段）、`AttendanceRecordEntity`（移除 6 字段）、`BalanceTransaction`（移除 8 字段）、`Worker`（移除 2 字段）
  - enterprise-service：`Job`（移除 2 字段）
  - platform-service：`WithdrawalRecord`（移除 2 字段）
- **Mapper XML**：
  - c-service：`JobMapper.xml`、`ShiftMapper.xml`、`AttendanceRecordMapper.xml`、`BalanceTransactionMapper.xml` 拆分 JOIN 为单表查询
  - enterprise-service：`ScheduleApplicationMapper.xml`、`AttendanceRecordMapper.xml` 拆分 JOIN
- **Service 层**：新增批量加载 + 内存关联逻辑
- **VO 层**：不变，仍包含关联数据
- **数据库**：无变更
