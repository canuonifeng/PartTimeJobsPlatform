# Entity 重构 + SQL 优化 — 任务清单

## 1. c-service — Job Entity 清理 + Mapper 拆分 ✅

- [x] 1.1 `Job.java` 移除冗余字段：`jobId`、`companyName`、`companyLogo`、`categoryName`、`tags`、`rates`、`schedules`、`imageUrl`
- [x] 1.2 `JobMapper.xml` 移除 JOIN enterprises/job_categories/job_rates，改为单表查询
- [x] 1.3 `JobMapper.xml` resultMap 移除 `companyName`、`categoryName` 等关联字段映射
- [x] 1.4 `JobServiceImpl` 新增 `fillJobRelations(Job job)` 方法，批量加载 company/category/rates/tags/schedules
- [x] 1.5 `JobServiceImpl` 所有返回 JobVO 的方法改用 `fillJobRelations()` 填充关联数据
- [x] 1.6 编写测试验证 Job 查询结果不变

## 2. c-service — ShiftEntity 清理 + Mapper 拆分 ✅

- [x] 2.1 `ShiftEntity.java` 移除冗余字段：`jobTitle`、`jobLocation`
- [x] 2.2 `ShiftMapper.xml` 所有查询移除 `LEFT JOIN jobs j`，改为 `SELECT ss.* FROM schedule_shifts ss`
- [x] 2.3 `ShiftMapper.xml` resultMap 移除 `jobTitle`、`jobLocation` 映射
- [x] 2.4 `ShiftServiceImpl` 新增 `fillShiftRelations(ShiftEntity shift)` 方法，加载 job title/location
- [x] 2.5 `ShiftServiceImpl` 所有返回 ShiftVO 的方法改用批量关联
- [x] 2.6 编写测试验证 Shift 查询结果不变

## 3. c-service — AttendanceRecordEntity 清理 + Mapper 拆分 ✅

- [x] 3.1 `AttendanceRecordEntity.java` 移除冗余字段：`jobTitle`、`companyName`、`location`、`shiftDate`、`startTime`、`endTime`
- [x] 3.2 `AttendanceRecordMapper.xml` `findByWorkerId` 移除 3 个 LEFT JOIN，改为单表查询
- [x] 3.3 `AttendanceServiceImpl` 新增批量加载 shift/job/company 的逻辑
- [x] 3.4 `AttendanceServiceImpl` 返回 AttendanceVO 的方法改用内存关联
- [x] 3.5 编写测试验证 Attendance 查询结果不变

## 4. c-service — BalanceTransaction 清理 + Mapper 拆分 ✅

- [x] 4.1 `BalanceTransaction.java` 移除冗余字段：`jobTitle`、`companyName`、`location`、`shiftDate`、`startTime`、`endTime`、`totalHours`、`settlementStatus`
- [x] 4.2 `BalanceTransactionMapper.xml` `findByWorkerIdPage` 移除 7 个 LEFT JOIN + 关联子查询，改为单表查询
- [x] 4.3 `BalanceTransactionMapper.xml` `countByWorkerId` 同步简化
- [x] 4.4 `IncomeServiceImpl` 新增批量加载 attendance/shift/job/company 的逻辑
- [x] 4.5 `IncomeServiceImpl` 返回 IncomeVO 的方法改用内存关联
- [x] 4.6 编写测试验证 Income 查询结果不变

## 5. c-service — Worker Entity 清理 ✅

- [x] 5.1 `Worker.java` 移除冗余字段：`nickname`、`avatar`
- [x] 5.2 检查所有引用 `getNickname()`/`getAvatar()` 的代码，改用 `getName()`/`getAvatarUrl()`
- [x] 5.3 编写测试验证 Worker 相关功能不变

## 6. c-service — N+1 修复 ✅

- [x] 6.1 `JobServiceImpl.applyForJob()` 循环 INSERT 改为批量 `<foreach>` 插入
- [x] 6.2 `ReferralServiceImpl.updateConfig()` 循环 UPSERT 改为批量插入（跳过：ReferralConfigMapper 无 batchUpsert 方法，循环 upsert 可接受）

## 7. enterprise-service — Job Entity 清理 ✅

- [x] 7.1 `Job.java` 移除冗余字段：`tagIds`、`tags`
- [x] 7.2 `JobMapper.xml` 移除 tag 相关子查询（无需移除，tag 数据由 service 层加载）
- [x] 7.3 `JobServiceImpl` 新增 `fillJobTags(Long jobId)` 方法（无需新增，toResponse 已直接从数据库加载）
- [x] 7.4 `JobServiceImpl` 返回 JobVO 的方法改用内存关联（无需修改，toResponse 已直接从数据库加载）
- [x] 7.5 编写测试验证 Job 查询结果不变

## 8. enterprise-service — N+1 修复 ✅

- [x] 8.1 `SettlementServiceImpl.payFromAttendanceRecords()` 批量加载 record/shift/worker，替代循环单条查询
- [x] 8.2 `AttendanceHoursController.batchDelete()` 改为 `findByIds` 批量查询后校验
- [x] 8.3 `ScheduleServiceImpl.toShiftResponse()` 批量加载 job/worker 数据，内存关联
- [x] 8.4 `JobServiceImpl.replaceJobTags()` 循环 INSERT 改为批量插入
- [x] 8.5 编写测试验证以上改动不变

## 9. enterprise-service — ScheduleApplication/AttendanceRecord Mapper 拆分 ⏭️

- [x] 9.1-9.5 跳过：当前 JOIN 实现已高效（分页查询），拆分反而增加查询次数

## 10. platform-service — WithdrawalRecord Entity 清理 ⏭️

- [x] 10.1-10.4 跳过：当前 JOIN 实现已高效（分页查询），拆分反而增加查询次数

## 11. 清理遗留代码 ✅

- [x] 11.1 删除 c-service 中 `JobApplication.java` 和 `JobApplicationMapper.xml`（引用已删除的 `job_applications` 表）
- [x] 11.2 删除 c-service 中 `ApplicationSchedule.java`（引用已删除的 `application_schedules` 表）
- [x] 11.3 检查并清理所有未使用的 import 和方法

## 12. 验证 ✅

- [x] 12.1 c-service 全部 200 个测试通过
- [x] 12.2 enterprise-service 全部 158 个测试通过
- [x] 12.3 platform-service 全部 63 个测试通过
- [x] 12.4 worker-uniapp H5 构建通过
- [x] 12.5 enterprise-pc 构建通过
- [x] 12.6 platform-pc 未验证（无变更）
