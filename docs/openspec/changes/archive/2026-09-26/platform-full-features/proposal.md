## Why

当前运营平台仅覆盖基础的用户管理、实名认证、提现审核等核心功能，缺少职位全生命周期管理、交易全链路监控、风控治理、数据运营、活动工具等关键模块。运营人员需要在多个系统间切换处理业务，效率低下且无法形成完整的运营闭环。

补齐运营平台全功能后，将实现：
1. 一站式运营工作台，所有业务操作在一个系统内完成
2. 全链路数据监控，及时发现和处理异常
3. 精细化运营工具，提升平台活跃度和交易量
4. 完善的风控治理体系，保障平台健康发展

## What Changes

### 新增模块（14个）

#### 🔥 核心业务运营
1. **职位管理** - 职位列表、详情、下架/置顶/推荐、敏感内容审核、导出
2. **报名审核** - 全平台报名记录、审核操作、批量处理、数据导出
3. **排班管理** - 全平台班次列表、详情、取消、数据导出
4. **考勤管理** - 全平台打卡记录、异常审核、补卡审批、数据导出
5. **结算管理** - 全平台结算单、对账、撤销、平台服务费统计
6. **交易流水** - 企业充值记录、工人提现流水、平台资金台账

#### 🛡️ 风控与治理
7. **投诉工单** - 工人/企业双向投诉、工单处理流程、纠纷仲裁
8. **风控中心** - 异常行为监控、黑名单/白名单管理、风控规则配置
9. **评价管理** - 企业/工人评价审核、信用分体系、违规处理

#### 📈 数据运营
10. **报表中心** - 运营日报/周报/月报、活跃度分析、供需分析、转化漏斗
11. **活动运营** - 新人红包、满单奖励、限时置顶、节日活动配置、Push推送

#### ⚙️ 平台配置
12. **内容运营** - 轮播图管理、热门推荐、地区管理、服务费率配置
13. **账号权限** - 运营人员管理、角色权限、操作日志审计
14. **客服中心** - 在线客服会话、FAQ管理、工单处理

### Capabilities

#### New Capabilities
- `job-management-platform`: 平台视角职位全生命周期管理
- `application-platform`: 全平台报名审核与管理
- `schedule-platform`: 全平台班次监控与管理
- `attendance-platform`: 全平台考勤审核与异常处理
- `settlement-platform`: 全平台结算对账与服务费管理
- `transaction-ledger`: 全平台资金流水与台账
- `complaint-workflow`: 双向投诉工单系统与纠纷处理
- `risk-control-center`: 异常监控、黑白名单、风控规则
- `review-management`: 评价审核与信用分体系
- `report-center`: 多维度运营报表与分析
- `operation-activities`: 红包、奖励、置顶、Push等运营工具
- `content-operation`: 轮播图、推荐位、地区等内容配置
- `platform-rbac`: 运营账号与权限管理
- `customer-service`: 客服会话与工单系统

#### Modified Capabilities
- `dashboard`: 数据看板增强，新增核心指标卡片与趋势图
- `enterprise-management`: 企业列表增强，增加活跃度/资金/风险标签
- `worker-management`: 工人列表增强，增加活跃度/收入/风险标签

## Impact

### Frontend
- `platform-pc/src/views/` 新增 14 个模块页面
- `platform-pc/src/App.vue` 菜单结构重组
- 新增通用组件：数据表格、审核弹窗、批量操作栏、图表组件

### Backend
- `platform-service/controller/` 新增 14 个 Controller
- `platform-service/service/` 新增对应 Service 实现
- `platform-service/mapper/` 新增查询方法
- 复用现有 Entity，新增部分 VO/DTO

### Database
- 新增表：`complaints`、`risk_blacklist`、`risk_whitelist`、`risk_rules`、`reviews`、`operation_activities`、`banners`、`platform_operators`、`operator_roles`、`operation_logs`、`push_tasks`、`faqs`
- 部分现有表增加字段：`jobs`(is_top,is_recommended)、`enterprise_accounts`(credit_score)、`workers`(credit_score)
