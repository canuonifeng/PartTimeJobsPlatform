# AI 批量操作

## 需求 1：AI 批量通过报名

- Given 保安岗位有 3 个待审核报名
- When 用户说"把保安岗位所有待审核的都通过了"
- Then AI 先调用 query_data 查询待审核列表
- Then AI 调用 batch_action({"action":"batch_accept", "targetIds":[1,2,3]})
- Then 前端展示批量操作卡（3 条记录，报名人信息列表）
- When 用户确认
- Then 后端逐个 POST /applications/accept
- Then AI 回复"已通过 3 人"

## 需求 2：AI 批量结算

- Given 保安岗位有 5 条未结算考勤记录
- When 用户说"结算保安岗位上周的工资"
- Then AI 先调用 query_data 查 UNPAID 记录
- Then AI 调用 batch_action({"action":"batch_pay", "filters":{"jobId":1, "settlementStatus":"UNPAID"}})
- Then 前端展示批量操作卡（5 条记录，总金额汇总）
- When 用户确认
- Then 后端 POST /attendance/hours/pay
- Then AI 回复"已结算 5 条，共 ¥720"

## 需求 3：拒绝时 targetIds 为空

- Given 保安岗位没有待审核的报名
- When 用户说"把保安岗位所有待审核的都通过了"
- Then AI 先查询 → 返回空列表
- Then AI 回复"保安岗位没有待审核的报名"
- Then 不调用 batch_action
