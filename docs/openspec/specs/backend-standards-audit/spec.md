## 新增需求

### 需求：全局后端 Controller 契约合规
所有后端 Controller 须遵守统一接口契约：查询使用 `@GetMapping`，创建和业务动作使用 `@PostMapping`，不得使用 `@PutMapping`、`@DeleteMapping`、`@PatchMapping`。

#### 场景：写操作接收明确 cmd
- **当** 写操作需要 ID、备注或状态等参数
- **则** 参数须通过 `@RequestBody` 的明确 cmd 承载
- **且** Controller 不得混用 `@RequestParam`、`@PathVariable` 与 `@RequestBody`

#### 场景：Controller 响应明确
- **当** Controller 返回业务数据给前端
- **则** 响应须使用 `ApiResponse<VO>` 或 `PageVO<VO>`
- **且** 不得直接返回 `Map` 或数据库 Entity

### 需求：数据库层分页
分页接口须在 Mapper XML 中使用 `LIMIT/OFFSET` 和独立 `COUNT(*)` 完成分页与总数查询。

#### 场景：Service 返回分页
- **当** Service 需要返回分页数据
- **则** Service 只计算 page、pageSize、offset
- **且** 不得先查询全量数据后通过 `subList`、`stream().skip()`、`stream().limit()` 做内存分页

### 需求：列表组装避免 N+1 查询
Service 列表组装须使用批量查询和内存 Map 关联，不得在循环或 `stream` 列表组装中执行数据库读操作。

#### 场景：列表需要关联数据
- **当** 列表 VO 需要工人、企业、岗位、分类或标签等关联数据
- **则** Service 须先收集 ID 并批量查询关联数据
- **且** 在循环中仅使用内存 Map 填充 VO

### 需求：Entity 字段边界清晰
Entity 须只包含数据库表字段，关联展示字段须放入 VO。

#### 场景：Entity 存在展示字段
- **当** Entity 字段不是当前表真实列
- **则** 字段须移到 VO
- **且** Service 须通过批量查询填充

### 需求：敏感日志控制
后端不得打印 token、密码、银行卡完整号、验证码、openId 等敏感信息。

#### 场景：支付或登录流程记录日志
- **当** 支付、转账、登录、短信验证码流程产生日志
- **则** 日志须使用脱敏字段或只记录非敏感业务 ID
- **且** 不得使用 `System.out.println` 打印验证码或敏感数据

### 需求：Mapper SQL 对齐
新增或整改的数据访问 SQL 应放入 Mapper XML；遗留注解 SQL 须被迁移或在整改清单中标注为延期债务。

#### 场景：遗留注解 SQL 保留
- **当** 存在 `@Select`、`@Update`、`@Insert`、`@Delete`
- **则** 须记录保留原因、风险级别和后续迁移计划
