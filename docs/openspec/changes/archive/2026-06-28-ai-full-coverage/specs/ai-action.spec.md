# AI 单条操作

## 需求 1：AI 通过报名

- Given 保安岗位有一个待审核报名（张三）
- When 用户说"通过张三的报名"
- Then AI 调用搜索定位到 applicationId
- Then AI 调用 execute_action({"action":"accept_application", "targetId":1})
- Then 前端展示报名操作卡（张三、保安、班次信息）
- When 用户点击"确认通过"
- Then 后端 POST /applications/accept
- Then 报名状态变更为 ACCEPTED
- Then AI 回复"已通过张三的报名"

## 需求 2：AI 拒绝报名

- Given 保安岗位有一个待审核报名（李四）
- When 用户说"拒绝李四的报名，年龄超了"
- Then AI 调用 execute_action({"action":"reject_application", "targetId":2, "reason":"年龄超了"})
- Then 前端展示报名操作卡（含拒绝原因）
- When 用户确认
- Then 后端 POST /applications/reject
- Then 报名状态变更为 REJECTED

## 需求 3：AI 关闭岗位

- Given 保安岗位状态为 PUBLISHED
- When 用户说"把保安岗位关闭"
- Then AI 调用 execute_action({"action":"close_job", "targetId":1})
- Then 前端展示状态操作卡（当前：已发布 → 变更：已关闭）
- When 用户确认
- Then 后端 POST /jobs/close

## 需求 4：AI 重新开放岗位

- Given 保安岗位状态为 CLOSED
- When 用户说"重新开放保安岗位"
- Then AI 调用 execute_action({"action":"reopen_job", "targetId":1})
- Then 前端展示状态操作卡
- When 用户确认
- Then 后端 POST /jobs/reopen

## 需求 5：AI 取消班次

- Given 存在一个 ACTIVE 的保安班次
- When 用户说"取消6月30号的保安班"
- Then AI 调用 execute_action({"action":"cancel_schedule", "targetId":1})
- Then 前端展示状态操作卡
- When 用户确认
- Then 后端 POST /schedules/update {status:CANCELLED}

## 需求 6：AI 修改工时

- Given 张三月薪保安考勤记录已存在
- When 用户说"把张三的工时改成8小时"
- Then AI 搜索定位 attendanceRecordId
- Then AI 调用 execute_action({"action":"update_attendance_hours", "targetId":1, "updates":{"totalHours":8}})
- Then 前端展示考勤编辑卡（当前值→新值）
- When 用户确认
- Then 后端 POST /attendance/hours/update

## 需求 7：AI 单条结算

- Given 张三月薪保安考勤记录状态为 UNPAID
- When 用户说"结算张三的工资"
- Then AI 调用 execute_action({"action":"pay_attendance", "targetId":1})
- Then 前端展示操作卡
- When 用户确认
- Then 后端 POST /attendance/hours/pay
