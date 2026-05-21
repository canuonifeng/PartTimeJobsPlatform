# C-Job/Shift Unification & Attendance Flow Design

## Overview
合并 c_job → jobs、c_shift → schedule_shifts，报名通过后生成班次，兼职端打卡签退后自动算工时和工资。

## Goals
- 统一 c_job 和 jobs 两张表，用 jobs
- 统一 c_shift 和 schedule_shifts 两张表，用 schedule_shifts
- c-service 保留现有接口，底层切到新表
- 报名通过后按岗位排班生成 schedule_shifts，含薪资快照
- 兼职端签到/签退后端写入 attendance_records
- 签退后自动算 totalHours 和 payAmount
- worker-uniapp 已有打卡页照常工作

## Non-Goals
- 不改 enterprise-service 的排班编辑流程
- 不改 PayrollServiceImpl 批次计算的核心逻辑
- 不做异步事件化

## Data Model Changes
### jobs 表新增字段
- `company_name`、`company_logo`、`category_name`、`rate_type`、`rate_amount`、`published_at`、`accepted_count`
- 删除 c_job 表

### schedule_shifts 表新增字段
- `company_id`（方便 c-service 查）
- 已有 application_id/salary_type/salary_amount/salary_currency 等快照字段
- 删除 c_shift 表

### attendance_records
- 已有 payAmount/calculatedAt（上次增加）
- c_attendance_record → 改为使用 attendance_records（统一用 enterprise 的表）

## Migration Strategy
1. 向 jobs 表加列
2. INSERT 回填 c_job 数据
3. 向 schedule_shifts 加列（company_id）
4. INSERT 回填 c_shift 数据
5. 删除 c_job / c_shift
6. c-service mapper 全改指向新表名

## Service Changes
### c-service
- JobMapper XML：c_job → jobs
- ShiftMapper XML：c_shift → schedule_shifts（join jobs 拿 job_title/location）
- AttendanceRecordMapper：c_attendance_record → attendance_records
- AttendanceServiceImpl.checkOut：补算 payAmount 和 calculatedAt
- JobServiceImpl：findByJobId → findById（id 即 jobId）
- InMemoryMappers/测试注册同步更新

### enterprise-service
- application.yml flyway baseline-version 维持 8
- V11 迁移：加列 + 回填 + 删旧表
- 无其他代码改动（报名生成班次已有）

### worker-uniapp
- 已有打卡页，只校准前端入参字段（latitude→lat、longitude→lng）
- PS: shiftId 已在响应中，无需改

## API Changes
- c-service 的 GET /api/schedule-shifts/my 返回里补 jobTitle/jobLocation（来自 join）
- POST /api/attendance/check-in / check-out 入参用 shiftId/lat/lng（不变）
- AttendanceVO 补 payAmount/calculatedAt

## Testing
- c-service AttendanceServiceTest：改 mapper 注入为 InMemoryJobs 生成
- c-service AttendanceControllerTest：不动（mock service）
- c-service JobServiceTest：mapper 指向 jobs 表
- enterprise-service：现有 ApplicationServiceTest 已有班次生成测试
