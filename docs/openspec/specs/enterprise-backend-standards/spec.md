## 新增需求

### 需求：Controller 接口契约符合企业后端规范
企业后端 Controller 须使用统一接口契约，并通过明确的请求和响应类型承载业务数据。

#### 场景：写操作使用 POST 和 cmd 请求体
- **当** 企业后端存在创建、更新、删除、状态变更或业务动作接口
- **则** 写操作必须使用 `@PostMapping`
- **且** 写操作必须使用 `@RequestBody` cmd 对象承载入参
- **且** 写操作不得使用 `@PutMapping`、`@DeleteMapping`、`@PatchMapping`
- **且** 写操作不得混用 `@RequestParam` 与 `@RequestBody`

#### 场景：查询操作使用 GET 和 query 参数
- **当** 企业后端存在列表、详情或统计查询接口
- **则** 查询操作可以使用 `@GetMapping`
- **且** 查询条件可以使用 `@RequestParam`
- **且** 查询接口不得直接返回数据库 Entity

#### 场景：Controller 不使用 Map 作为业务契约
- **当** 企业后端 Controller 接收或返回业务数据
- **则** 请求体必须使用明确的 `cmd` 类型
- **且** 响应必须使用明确的 `vo` 类型并包裹在 `ApiResponse` 中
- **且** Controller 不得使用 `Map` 作为业务请求体或业务响应体

#### 场景：Controller 不使用路径参数
- **当** 企业后端 Controller 需要接收资源 ID 或动作 ID
- **则** 查询接口应使用 query 参数
- **且** 写接口应使用 cmd 请求体字段
- **且** Controller 不得使用 `@PathVariable`

### 需求：列表分页在数据库层完成
企业后端分页列表须由 Mapper XML 负责当前页查询和总数统计。

#### 场景：分页列表使用 LIMIT/OFFSET 和 COUNT
- **当** Service 调用 Mapper 获取分页数据
- **则** Mapper XML 必须提供当前页查询并使用 `LIMIT/OFFSET`
- **且** Mapper XML 必须提供同条件 `COUNT(*)`
- **且** Service 不得先查询全量后使用 `subList`、`stream().skip` 或 `stream().limit` 做分页

#### 场景：聚合列表按查询额度获取记录
- **当** 接口需要限制返回数量
- **则** Service 应按剩余数量调用各来源 Mapper 查询
- **且** 不得先聚合超量记录后在内存中截断

### 需求：列表关联数据避免 N+1 查询
企业后端列表 VO 组装须优先批量读取关联数据并在内存中关联。

#### 场景：列表 VO 需要关联字段
- **当** Service 组装包含关联对象字段的列表 VO
- **则** Service 应先收集记录中的关联 ID
- **且** Service 应通过批量 Mapper 方法一次性读取关联数据
- **且** Service 应使用内存 Map 填充 VO
- **且** Service 不得在循环或 `stream().map` 中执行数据库读操作

#### 场景：单条详情允许必要单条查询
- **当** Service 执行单条详情或单个业务动作
- **则** 可以使用必要的 `findById` 或单条查询
- **且** 不应误判为列表 N+1 问题

### 需求：Mapper 实现集中在 XML
企业后端新增或整改 Mapper 方法时，SQL 应优先放在 Mapper XML 中。

#### 场景：新增 Mapper 查询
- **当** 开发者添加 Mapper 方法
- **则** SQL 应优先放在 `src/main/resources/mapper/*.xml`
- **且** Mapper 接口与 XML 的 namespace 和方法 ID 应一一对应
- **且** 用户输入必须使用 `#{}` 参数绑定

#### 场景：历史只读 Mapper 迁移
- **当** 历史遗留只读 Mapper 使用注解 SQL且本次变更不涉及其业务行为
- **则** 可以记录为剩余规范债务
- **且** 后续应按模块迁移到 XML，避免一次性大范围改动

### 需求：前端 API 与后端契约同步
企业前端须与后端写接口的 POST body 契约保持一致。

#### 场景：后端写接口改为 POST body
- **当** 企业后端写接口从旧 method/query 改为 POST cmd
- **则** 企业 PC 或企业小程序 API 封装必须使用 POST
- **且** 请求体字段必须与后端 cmd 字段一致
- **且** 页面业务行为保持不变

### 需求：规范治理必须可验证
企业后端规范治理改动须有静态扫描、编译或测试证据支撑。

#### 场景：提交前执行规范扫描和构建
- **当** 开发者准备提交企业后端规范治理改动
- **则** 必须执行 Controller 硬性违规扫描
- **且** 必须执行内存分页扫描
- **且** 必须执行 `enterprise-service` 编译和测试编译
- **且** 涉及前端 API 时必须执行对应前端构建
- **且** 必须执行 `git diff --check`
