# backend-standards-audit Specification

## ADDED Requirements

### Requirement: Global Backend Controller Contract Compliance

所有后端 Controller SHALL 遵守统一接口契约：查询使用 `@GetMapping`，创建和业务动作使用 `@PostMapping`，不得使用 `@PutMapping`、`@DeleteMapping`、`@PatchMapping`。

#### Scenario: Write action accepts typed cmd

- **GIVEN** 一个写操作需要 ID、备注或状态等参数
- **WHEN** Controller 接收请求
- **THEN** 参数 SHALL 通过 `@RequestBody` 的 typed cmd 承载
- **AND** Controller SHALL NOT 混用 `@RequestParam`、`@PathVariable` 与 `@RequestBody`

#### Scenario: Controller response is explicit

- **GIVEN** Controller 返回业务数据
- **WHEN** 返回给前端
- **THEN** 响应 SHALL 使用 `ApiResponse<VO>` 或 `PageVO<VO>`
- **AND** SHALL NOT 直接返回 `Map` 或数据库 Entity

### Requirement: Database-level Pagination

分页接口 SHALL 在 Mapper XML 中使用 `LIMIT/OFFSET` 和独立 `COUNT(*)` 完成分页与总数查询。

#### Scenario: Service returns page

- **GIVEN** Service 需要返回分页数据
- **WHEN** 调用 Mapper 查询
- **THEN** Service SHALL 只计算 page、pageSize、offset
- **AND** SHALL NOT 先查询全量数据后通过 `subList`、`stream().skip()`、`stream().limit()` 做内存分页

### Requirement: No N+1 Reads in List Assembly

Service 列表组装 SHALL 使用批量查询和内存 Map 关联，不得在循环或 `stream` 列表组装中执行数据库读操作。

#### Scenario: List requires associated data

- **GIVEN** 列表 VO 需要工人、企业、岗位、分类或标签等关联数据
- **WHEN** Service 组装列表
- **THEN** Service SHALL 先收集 ID 并批量查询关联数据
- **AND** SHALL 在循环中仅使用内存 Map 填充 VO

### Requirement: Entity Field Boundaries

Entity SHALL 只包含数据库表字段，关联展示字段 SHALL 放入 VO。

#### Scenario: Entity has display-only field

- **GIVEN** Entity 字段不是当前表真实列
- **WHEN** 返回前端需要该字段
- **THEN** 字段 SHALL 移到 VO
- **AND** Service SHALL 通过批量查询填充

### Requirement: Sensitive Logging Control

后端 SHALL NOT 打印 token、密码、银行卡完整号、验证码、openId 等敏感信息。

#### Scenario: Payment or login flow logs events

- **GIVEN** 支付、转账、登录、短信验证码流程产生日志
- **WHEN** 写入日志
- **THEN** 日志 SHALL 使用脱敏字段或只记录非敏感业务 ID
- **AND** SHALL NOT 使用 `System.out.println` 打印验证码或敏感数据

### Requirement: Mapper SQL Alignment

新增或整改的数据访问 SQL SHOULD 放入 Mapper XML；遗留注解 SQL SHALL 被迁移或在整改清单中标注为延期债务。

#### Scenario: Existing annotation SQL remains

- **GIVEN** 存在 `@Select`、`@Update`、`@Insert`、`@Delete`
- **WHEN** 本轮整改未迁移
- **THEN** SHALL 记录保留原因、风险级别和后续迁移计划
