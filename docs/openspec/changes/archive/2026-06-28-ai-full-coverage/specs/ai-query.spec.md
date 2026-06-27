# AI 数据查询

## 需求 1：AI 查询岗位列表

- Given 企业有 3 个岗位（保安、保洁、服务员）
- When 用户说"看看我有哪些岗位"
- Then AI 调用 query_data({"type":"jobs"})
- Then 后端返回岗位列表
- Then AI 用自然语言总结岗位名称、状态和数量
- Then 前端展示 AI 的文字回复

## 需求 2：AI 查询岗位详情

- Given 存在岗位"保安"（ID=1）
- When 用户说"保安岗位详情是什么"
- Then AI 识别岗位名称后调用 search_jobs("保安") 获取 ID
- Then AI 调用 query_data({"type":"job_detail", "jobId":1})
- Then 后端返回岗位详情（标题、描述、薪资、联系人、班次数）
- Then AI 逐项总结

## 需求 3：AI 查询班次列表

- Given 保安岗位有 3 个班次
- When 用户说"保安岗位有哪些班次"
- Then AI 调用 search_jobs → query_data({"type":"schedules", "jobId":1})
- Then 后端返回班次列表（日期、时间、报名人数）
- Then AI 逐条列出

## 需求 4：AI 统计报名情况

- Given 保安岗位有 8 人报名（5 待审、2 通过、1 拒绝）
- When 用户说"保安岗位报名情况怎么样"
- Then AI 调用 query_data({"type":"applications", "jobTitle":"保安"})
- Then AI 总结：总报名 8、待审 5、通过 2、拒绝 1

## 需求 5：AI 查询考勤记录

- Given 保安岗位有未结算考勤记录
- When 用户说"保安岗位哪些人还没结算"
- Then AI 调用 query_data({"type":"attendance", "jobId":1, "settlementStatus":"UNPAID"})
- Then 后端返回未结算记录列表
- Then AI 按工人分组展示

## 需求 6：AI 查询考勤异常

- Given 保安岗位有迟到和缺勤记录
- When 用户说"哪些人考勤异常"
- Then AI 调用 query_data({"type":"attendance", "jobId":1})
- Then 后端返回所有考勤记录
- Then AI 过滤异常状态（LATE/ABSENT）并总结
