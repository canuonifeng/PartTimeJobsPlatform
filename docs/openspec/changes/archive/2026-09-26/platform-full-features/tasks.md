<!-- 状态（2026-09-20 集成后）：后端 mvn test 134 全绿、前端 npm run build 通过、路由已接线。
     [x]=已真实连库/已渲染并通过编译测试；[ ]=未完成。
     说明：①前后端联调以“接口真实连库 + 前端接真接口 + 构建通过 + 路由可达”为准，未做浏览器逐页手点冒烟；
     ②operator_roles 不另建表，角色用 platform_operators.role/permissions 字段实现；③客服会话用轮询而非 WebSocket。 -->

## Phase 1: 核心业务运营模块（职位/报名/排班/考勤/结算）

### 1. 职位管理（平台视角）
- [x] 1.1 后端：JobController - 列表/详情/下架/置顶/推荐（set-top/set-recommended 已改 typed cmd）
- [x] 1.2 后端：JobService - 连库实现
- [x] 1.3 前端：JobList.vue
- [x] 1.4 前端：JobDetail.vue（基本信息+报名/排班/考勤/结算 Tabs）
- [x] 1.5 前端：菜单
- [x] 1.6 前后端联调

### 2. 报名审核（平台视角）
- [x] 2.1 后端：ApplicationController - 列表/详情/通过/拒绝/批量
- [x] 2.2 后端：ApplicationService - 真实连 ScheduleApplicationMapper
- [x] 2.3 前端：ApplicationList.vue
- [x] 2.4 前端：通用审核弹窗（AuditDialog 组件）
- [x] 2.5 前端：菜单
- [x] 2.6 前后端联调

### 3. 排班管理（平台视角）
- [x] 3.1 后端：JobScheduleController - 列表/详情/取消
- [x] 3.2 后端：JobScheduleService - 真实连 JobScheduleMapper
- [x] 3.3 前端：ScheduleList.vue
- [x] 3.4 前端：排班详情（详情弹窗，未单独建 Detail 路由）
- [x] 3.5 前端：菜单
- [x] 3.6 前后端联调

### 4. 考勤管理（平台视角）
- [x] 4.1 后端：AttendanceController - 列表/详情/异常审核/补卡
- [x] 4.2 后端：AttendanceService - 真实连 AttendanceRecordMapper
- [x] 4.3 前端：AttendanceList.vue
- [x] 4.4 前端：考勤审核弹窗
- [x] 4.5 前端：菜单
- [x] 4.6 前后端联调

### 5. 结算管理（平台视角）
- [x] 5.1 后端：SettlementController - 列表/详情/撤销（冲正流水）
- [x] 5.2 后端：SettlementService - 聚合 enterprise_balance_transactions
- [x] 5.3 前端：SettlementList.vue
- [x] 5.4 前端：结算详情
- [x] 5.5 前端：菜单
- [x] 5.6 前后端联调

---

## Phase 2: 交易财务模块

### 6. 交易流水
- [x] 6.1 后端：TransactionController - 列表/详情/筛选
- [x] 6.2 后端：TransactionService - 真实连库
- [x] 6.3 前端：TransactionList.vue
- [x] 6.4 前端：菜单
- [x] 6.5 前后端联调

### 7. 企业充值管理
- [x] 7.1 后端：EnterpriseTopUpController - 列表/审核通过/拒绝
- [x] 7.2 后端：TopUpService - 通过时真实入账余额+写流水（@Transactional）
- [x] 7.3 前端：TopUpList.vue
- [x] 7.4 前端：充值审核弹窗
- [x] 7.5 前端：菜单
- [x] 7.6 前后端联调

### 8. 财务对账
- [x] 8.1 后端：FinanceReportController - typed 真实聚合
- [x] 8.2 后端：FinanceReportService - 按日/区间聚合
- [x] 8.3 前端：FinanceReport.vue
- [x] 8.4 前端：服务费统计 ECharts（复用 ChartBox）
- [x] 8.5 前端：菜单
- [x] 8.6 前后端联调

---

## Phase 3: 风控治理模块

### 9. 投诉工单
- [x] 9.1 数据库：complaints 表
- [x] 9.2 后端：Entity/Mapper/VO/CMD
- [x] 9.3 后端：ComplaintController - list/detail/handle/arbitrate/close
- [x] 9.4 后端：ComplaintService
- [x] 9.5 前端：ComplaintList.vue
- [x] 9.6 前端：ComplaintDetail.vue
- [x] 9.7 前端：菜单
- [x] 9.8 前后端联调

### 10. 风控中心
- [x] 10.1 数据库：risk_blacklist/whitelist/rules 表
- [x] 10.2 后端：Entity/Mapper/VO/CMD
- [x] 10.3 后端：RiskController - 监控/黑白名单/规则 typed
- [x] 10.4 后端：RiskService 真实连库
- [x] 10.5 前端：RiskCenter.vue
- [x] 10.6 前端：菜单
- [x] 10.7 前后端联调

### 11. 评价管理
- [x] 11.1 数据库：reviews 表 + enterprise_accounts/c_worker 增加 credit_score（scripts/32）
- [x] 11.2 后端：Entity/Mapper/VO/CMD
- [x] 11.3 后端：ReviewController - 列表/详情/删违规/信用分调整
- [x] 11.4 后端：ReviewService 真实连库
- [x] 11.5 前端：ReviewList.vue
- [x] 11.6 前端：企业/工人详情页"信用信息"Tab（列表页信用分展示与调整弹窗，调 /reviews/adjust-credit）
- [x] 11.7 前端：菜单
- [x] 11.8 前后端联调

---

## Phase 4: 数据运营模块

### 12. 报表中心
- [x] 12.1 后端：ReportController - 概览/企业/工人/供需/漏斗
- [x] 12.2 后端：ReportService 真实聚合
- [x] 12.3 前端：ReportCenter.vue 多 Tab
- [x] 12.4 前端：ECharts 折线/柱/漏斗（ChartBox）
- [x] 12.5 前端：CSV 导出
- [x] 12.6 前端：菜单
- [x] 12.7 前后端联调

### 13. 活动运营
- [x] 13.1 数据库：operation_activities/push_tasks 表
- [x] 13.2 后端：Entity/Mapper/VO/CMD
- [x] 13.3 后端：ActivityController - typed CRUD/效果/Push
- [x] 13.4 后端：ActivityService
- [x] 13.5 前端：ActivityManage.vue
- [x] 13.6 前端：菜单
- [x] 13.7 前后端联调

---

## Phase 5: 平台配置模块

### 14. 内容运营
- [x] 14.1 数据库：banners 表 + jobs.is_top/is_recommended
- [x] 14.2 后端：Entity/Mapper/VO/CMD
- [x] 14.3 后端：ContentController typed - 轮播/推荐/地区/费率
- [x] 14.4 后端：ContentService
- [x] 14.5 前端：ContentOperation.vue
- [x] 14.6 前端：菜单
- [x] 14.7 前后端联调

### 15. 账号权限管理
- [x] 15.1 数据库：platform_operators 表（角色用 role/permissions 字段，不另建 operator_roles）
- [x] 15.2 后端：Entity/Mapper/VO/CMD
- [x] 15.3 后端：OperatorController - 账号 CRUD/角色/权限
- [x] 15.4 后端：OperatorService - 六角色权限矩阵
- [x] 15.5 前端：OperatorManage.vue
- [x] 15.6 前端：菜单
- [x] 15.7 前后端联调

### 16. 操作日志审计
- [x] 16.1 数据库：operation_logs 表
- [x] 16.2 后端：Entity/Mapper/VO/CMD
- [x] 16.3 后端：OperationLogAspect AOP 切面自动记录
- [x] 16.4 后端：OperationLogController typed 列表/筛选
- [x] 16.5 前端：OperationLog.vue
- [x] 16.6 前端：菜单
- [x] 16.7 前后端联调

---

## Phase 6: 客服中心模块

### 17. 在线客服会话
- [x] 17.1 数据库：cs_sessions/cs_messages 表（scripts/34）
- [x] 17.2 后端：轮询方案（非 WebSocket）
- [x] 17.3 后端：CustomerServiceController - 会话/消息/结束 typed
- [x] 17.4 后端：CsSessionService
- [x] 17.5 前端：CustomerService.vue（5s 轮询）
- [x] 17.6 前端：聊天窗口（页内实现）
- [x] 17.7 前端：菜单
- [x] 17.8 前后端联调

### 18. FAQ管理
- [x] 18.1 数据库：faqs 表
- [x] 18.2 后端：Entity/Mapper/VO/CMD
- [x] 18.3 后端：FaqController - list/create/update/sort/delete
- [x] 18.4 后端：FaqService
- [x] 18.5 前端：FaqManage.vue
- [x] 18.6 前端：菜单
- [x] 18.7 前后端联调

### 19. 工单处理
- [x] 19.1 复用投诉工单
- [x] 19.2 前端：TicketHandle.vue
- [x] 19.3 前端：菜单
- [x] 19.4 前后端联调

---

## Phase 7: 通用组件与优化

### 20. 通用组件增强
- [x] 20.1 前端：ProTable.vue（筛选/表格/分页/批量）
- [x] 20.2 前端：AuditDialog.vue
- [x] 20.3 前端：DetailDialog.vue
- [x] 20.4 前端：ChartBox.vue（折线/饼/柱/漏斗）
- [x] 20.5 后端：CsvExportUtil 导出工具
- [x] 20.6 后端：OperationLogAspect AOP

### 21. Dashboard增强
- [x] 21.1 后端：/stats 指标卡片改真实聚合
- [x] 21.2 后端：/trend 趋势接口（近 N 天）
- [x] 21.3 前端：Dashboard.vue 接 /trend
- [x] 21.4 前后端联调

### 22. 菜单结构重组
- [x] 22.1 前端：App.vue 菜单新架构
- [x] 22.2 路由全部可达（build 通过，4 个新详情/页面路由已注册）
