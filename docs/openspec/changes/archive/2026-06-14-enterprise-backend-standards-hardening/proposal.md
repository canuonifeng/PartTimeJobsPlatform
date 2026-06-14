## Why

企业后端已经能够支撑当前业务，但全局扫描发现部分接口和实现仍偏离 `docs/后端技术规范.md`：Controller 写操作存在非 POST 方法、Map 入参/返回、路径参数，部分列表存在内存分页或潜在 N+1 查询。现在先形成集中治理 spec，避免零散修复造成前后端接口不一致或遗漏验证。

## What Changes

- 规范化企业后端 Controller：写操作统一使用 `@PostMapping` 和 `@RequestBody` cmd，查询保留 `@GetMapping` 和 query 参数。
- 移除 Controller 层 `Map` 入参/返回，改用明确的 `cmd` 和 `vo`。
- 消除列表接口内存分页，分页统一下沉到 Mapper XML 的 `LIMIT/OFFSET + COUNT`。
- 消除列表场景的 N+1 查询，改用批量查询 + 内存 Map 组装 VO。
- 同步企业 PC 与企业小程序 API 调用方式，确保前端和后端接口契约一致。
- 建立回归扫描与验证清单，后续改动以规范扫描结果为准。

## Capabilities

### New Capabilities
- `enterprise-backend-standards`: 企业后端接口、分层、分页、Mapper 与验证规范治理能力。

### Modified Capabilities
- `job-management`: 职位、报名、薪资规则、排班相关接口方法和请求体规范化。
- `schedule-attendance`: 班次、考勤工时、补卡、考勤确认相关接口方法和分页规范化。
- `payroll`: 结算、撤回结算、企业资金充值等写接口 cmd 化。
- `enterprise-account`: 账号列表、删除等接口 cmd 化并移除 Map 入参。
- `worker-profile`: 工人档案、黑名单、兼职管理相关接口和批量查询规范化。
- `notification`: 通知模板管理接口方法与请求体规范化。

## Impact

- Backend: 影响 `enterprise-service` 的 Controller、cmd/vo、Service、Mapper XML 与相关测试；部分接口路径或 HTTP method 会从 PUT/DELETE/path-variable 改为 POST body。
- Frontend: 企业 PC 与企业小程序对应 API 封装需要同步，避免请求方法和请求体不匹配。
- Database: 原则上不新增表结构；如发现分页或批量查询需要索引，另行补迁移脚本。
- Infrastructure: 不涉及 Redis、RocketMQ、XXL-Job 或部署拓扑变更；需要在发布前确认前后端同版本上线。
