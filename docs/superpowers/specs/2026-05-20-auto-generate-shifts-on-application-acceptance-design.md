## Overview
报名通过后，系统自动按岗位现有排班信息生成班次；每条岗位排班生成一条班次快照。班次的打卡、工时和工资计算都围绕这条快照展开。

## Goals
- 报名通过后自动生成班次。
- 每条岗位排班都生成一条班次。
- 已生成班次保持快照，不随岗位后续排班变更而回写。
- 支持班次签到、签退、自动记录打卡时间、自动计算工时和工资。

## Scope
- 企业端报名通过流程。
- 班次数据生成与快照字段落库。
- 上班打卡、下班打卡。
- 下班后工时与工资自动计算。

## Non-Goals
- 不改岗位排班的编辑流程。
- 不做异步事件化处理。
- 不重构现有薪资批次计算整体架构。

## Proposed Flow
1. 报名审核通过时，更新 `job_applications.status` 为 `ACCEPTED`。
2. 读取该岗位全部 `job_schedules`。
3. 为每条岗位排班生成一条 `schedule_shifts` 记录。
4. 生成时复制排班快照字段，包括：
   - 兼职ID
   - 报名ID
   - 岗位ID
   - 上班时间
   - 薪资标准
5. 打卡时写入 `attendance_records.checkInTime` / `checkOutTime`。
6. 签退后自动计算 `totalHours`。
7. 签退后按班次快照薪资标准计算 `payAmount`。

## Data Model Changes
### `ScheduleShift`
- `applicationId`: 关联报名记录。
- `salaryType`: 快照薪资类型。
- `salaryAmount`: 快照薪资金额。
- `salaryCurrency`: 快照币种。

### `AttendanceRecord`
- `payAmount`: 本次班次工资。
- `calculatedAt`: 工资计算时间。

## Service Behavior
### `ApplicationService.acceptApplication`
- 继续保留岗位录满校验。
- 成功通过后调用班次生成逻辑。
- 生成失败时整体回滚，避免出现“报名已通过但班次未生成”的半成品状态。

### `ScheduleService`
- 新增签到接口。
- 新增签退接口。
- 签退时自动计算工时与工资。

### `PayrollService`
- 保持现有批次结构。
- 后续批次计算优先读取已落库的工时和工资结果。

## Error Handling
- 若岗位没有任何排班，不生成班次，但报名仍可通过。
- 若班次已存在，则不重复生成。
- 若签到/签退顺序错误，返回业务错误。
- 若签退时缺少签到时间，拒绝计算。

## Testing
- 报名通过后应生成与岗位排班数量一致的班次。
- 已生成班次应保存快照字段。
- 岗位排班更新后，不应影响已生成班次。
- 签到应记录打卡时间。
- 签退应计算工时和工资。
- 通过、打卡、计算的失败路径应有对应测试。

## Open Questions
- 无。
