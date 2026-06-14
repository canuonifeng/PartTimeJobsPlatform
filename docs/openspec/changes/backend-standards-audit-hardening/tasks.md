# Tasks

## 1. Audit Baseline

- [ ] 确认当前工作区企业端未提交改动是否继续保留，并以最终 diff 作为企业端扫描基线。
- [ ] 复跑 Controller 硬规则扫描，按服务输出最终违规清单。
- [ ] 复跑分页、Mapper 注解、敏感日志、Entity 关联字段扫描。
- [ ] 按 must-fix、needs-db-confirm、legacy-debt 三类标记问题。

## 2. Worker Backend (`c-service`)

- [ ] 将 `AuthController`、`ProfileController`、`NotificationController`、`ReferralController`、`WorkerBankCardController`、`WorkerSettingsController` 的 PUT/DELETE 接口改为 POST。
- [ ] 将 `JobController.applyForJob` 的 `id` 合并进 `ApplyJobCmd`。
- [ ] 将 `CorrectionController`、`WithdrawalController`、`ReferralController.updateConfig` 的 Map/List body 改为 typed cmd。
- [ ] 将 `PublicConfigController`、`ReferralController.getReferralPoster` 的 Map 返回改为 VO + `ApiResponse`。
- [ ] 迁移 `CompanyWorkerInsertMapper` 注解 SQL 到 XML 或登记为遗留债务。
- [ ] 处理 `WeChatPayServiceImpl`、`WorkerServiceImpl` 敏感日志和验证码输出。

## 3. Enterprise Backend (`enterprise-service`)

- [ ] 复核当前企业端已改造 Controller，确保无 PUT/DELETE、Map 入参/返回、POST query/body 混用。
- [ ] 决策 `FileController.upload` 是否作为 Multipart 例外，或改为 JSON 元数据流程。
- [ ] 将 `WorkerProfileController.evaluateWorker` 的 `workerId` 合并进 `EvaluationCmd`。
- [ ] 迁移 `EnterpriseMapper` 注解 SQL 到 XML。
- [ ] 迁移或标记 `WorkerSyncMapper` 注解 SQL，确保列表场景只用批量查询。
- [ ] 保持已修复的报名列表数据库分页和待办聚合查询，并复扫确认。

## 4. Platform Backend (`platform-service`)

- [ ] 将 `JobCategoryController`、`JobReportController`、`JobTagController`、`ReferralController`、`SystemConfigController` 的 PUT/DELETE 改为 POST。
- [ ] 将 `EnterpriseAccountController`、`EnterpriseController`、`WorkerController` 的 Map body 改为 typed cmd。
- [ ] 将企业/工人实名认证审核、推荐奖励审核中的 `@RequestParam id` 和 `Map` body 合并为审核 cmd。
- [ ] 将审核 Controller 内 try/catch 改为统一异常处理模式或现有服务异常约定。
- [ ] 核对实名认证、提现、企业 Entity 中疑似关联字段是否为真实表字段；非真实列迁移到 VO。

## 5. Frontend API Synchronization

- [ ] 同步 `worker-uniapp` 受影响 API 方法的 HTTP method 和请求体。
- [ ] 同步 `enterprise-uniapp`、`enterprise-pc` 受影响 API 方法的 HTTP method 和请求体。
- [ ] 同步 `platform-pc` 受影响 API 方法的 HTTP method 和请求体。

## 6. Verification

- [ ] 运行 `git diff --check`。
- [ ] 运行 `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`。
- [ ] 运行 `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`。
- [ ] 运行 `cd platform-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`。
- [ ] 受影响前端分别运行对应 build。
- [ ] 复跑静态扫描，并在交付说明中列出剩余 legacy-debt。
