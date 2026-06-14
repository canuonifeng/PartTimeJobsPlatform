## Overview

本变更用于集中治理 `enterprise-service` 中与 `docs/后端技术规范.md` 不一致的实现，目标是先建立可执行的规范清单，再按清单统一改造。治理范围覆盖 Controller 接口契约、cmd/vo 使用、分页策略、批量查询、Mapper XML、前端 API 同步与验证命令。

## Current Non-compliance Inventory

### Controller 接口规范

扫描命令：

```bash
rg -n "@(PutMapping|DeleteMapping|PatchMapping)|@PathVariable|ApiResponse<.*Map|@RequestBody.*Map|public .*\(@RequestParam.*@RequestBody|Map<" enterprise-service/src/main/java/com/parttime/enterprise/controller
```

发现的不合规类型：

- 写操作使用 `@PutMapping` / `@DeleteMapping`，不符合“Controller 只能用 `@GetMapping` 和 `@PostMapping`”要求。
- 写操作混用 `@RequestParam` 和 `@RequestBody`，不符合“POST 只能用 `@RequestBody` 作为入参”要求。
- 部分 Controller 使用 `Map` 作为请求体或返回值，不符合“禁止用 Map 返回/入参优先 cmd”要求。
- `OperationController` 待办动作使用 `@PathVariable`，不符合路径参数限制。

涉及模块：

- `ApplicationController`：报名审核通过/拒绝使用 PUT + query 参数。
- `AttendanceHoursController`：工时更新、结算、删除使用 PUT/DELETE 或裸数组请求体。
- `SettlementController`：撤回结算使用 PUT + query 参数。
- `JobController`：职位、薪资规则、排班更新/删除/状态操作使用 PUT/DELETE 与 query 参数。
- `ScheduleController`：班次更新/删除/取消、补卡审批使用 PUT/DELETE 或 Map 入参。
- `EnterpriseController`：企业 Logo 更新使用 PUT + Map，企业信息返回 Map。
- `EnterpriseBalanceController`：充值使用 Map 入参。
- `AccountController`：列表、删除使用 Map 入参。
- `CompanyLocationController`：列表、删除、启用、禁用使用 Map 入参。
- `JobTemplateController`：删除使用 Map 入参。
- `NotificationController`：通知模板更新/删除使用 PUT/DELETE。
- `WorkerProfileController`：移除黑名单使用 DELETE，拉黑使用 RequestParam + Body。
- `FileController`：文件上传返回 `ApiResponse<Map<String,String>>`。

### 分页规范

扫描命令：

```bash
rg -n "subList|stream\(\)\.skip|stream\(\)\.limit|records\.stream\(\)\.limit" enterprise-service/src/main/java/com/parttime/enterprise/service/impl
```

发现的不合规类型：

- `ApplicationServiceImpl` 报名列表先查全量，再在 Service 层 `subList` 分页。
- `OperationServiceImpl` 默认待办列表聚合后使用 `stream().limit` 截断。

### N+1 查询规范

发现的不合规类型：

- `CompanyWorkerServiceImpl` 兼职列表曾在 `stream().map` 中逐条查询姓名、电话、性别、生日、实名状态。
- 其他 Service 中存在单条详情或单业务动作查询，需区分列表 N+1 与合理单条业务查询，不应一刀切修改。

### Mapper 规范

扫描命令：

```bash
rg -n "@Select|@Update|@Insert|@Delete" enterprise-service/src/main/java/com/parttime/enterprise/mapper
```

剩余关注项：

- `EnterpriseMapper` 仍使用注解 SQL。
- `WorkerSyncMapper` 仍使用部分单条注解 SQL，但该 Mapper 属于历史遗留只读跨域查询，规范允许保留；如要严格统一，可后续迁移到 XML。

## Architecture

```text
Enterprise PC / Enterprise UniApp
        │
        │  POST JSON cmd / GET query
        ▼
Controller
  - Auth context
  - Request cmd / query params
  - ApiResponse<VO>
        │
        ▼
Service
  - Business rules
  - Transactions
  - Batch loading orchestration
        │
        ▼
Mapper Interface + XML
  - LIMIT/OFFSET pagination
  - COUNT queries
  - Batch select by ids
        │
        ▼
MySQL
```

## Design Decisions

- 写操作统一使用 `POST`，原因是项目规范明确禁止 Controller 使用 `PUT/DELETE/PATCH`。
- 写操作统一使用 cmd 入参，原因是 cmd 能表达字段语义、便于 OpenAPI 文档和测试维护。
- Controller 禁止返回 Map，统一使用 VO，避免前端契约不清和敏感字段误暴露。
- 列表分页必须在 Mapper XML 完成，避免大数据量时 Service 内存分页造成性能问题。
- 列表关联数据使用批量加载 + Map 合并，避免 N+1；单条详情/单业务动作可保留必要的 `findById`。
- 前端 API 封装与后端接口一起变更，避免“后端规范化但前端仍 PUT/DELETE”的联调风险。

## Data Flow

### 写操作

```text
UI Action
  -> frontend api.post('/module/action', cmd)
  -> Controller @PostMapping + @RequestBody Cmd
  -> Service business method
  -> Mapper XML update/insert/delete
  -> ApiResponse<VO|Void>
```

### 分页列表

```text
UI list query
  -> GET query params 或 POST list cmd（按现有模块风格）
  -> Service normalize page/pageSize
  -> Mapper count query
  -> Mapper page query LIMIT/OFFSET
  -> PageVO(records,total)
```

### 批量关联

```text
Primary records page
  -> collect distinct ids
  -> batch query related data
  -> Map<Long, Related>
  -> fill VO in memory without DB calls inside loop
```

## Risk and Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| 前端仍调用旧 PUT/DELETE | 页面操作 404/405 | 同步修改 `enterprise-pc/src/api` 和 `enterprise-uniapp/src/api`，构建验证 |
| POST body 字段遗漏 | 后端收到 null 导致业务失败 | cmd 字段命名与旧 query 参数一致，补编译和关键页面构建 |
| 大量 Controller 同时改动 | 回归范围扩大 | 按模块分批提交，先处理硬性规范违规，再处理性能债务 |
| Mapper 分页 SQL 条件与旧逻辑不一致 | 列表数据变化 | 保留原筛选语义，新增 count 与 page 查询使用同一 WHERE 条件 |
| 注解 Mapper 全量迁移成本高 | 影响遗留模块稳定性 | 标为剩余债务，单独排期迁移 |

## Verification Strategy

- 静态扫描：Controller 硬性违规、内存分页、前端 PUT/DELETE 调用。
- 后端：`cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests clean compile`
- 测试编译：`cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn -DskipTests test`
- 小程序：`cd enterprise-uniapp && npm run build:mp-weixin`
- PC：`cd enterprise-pc && npm run build`
- 通用：`git diff --check`
