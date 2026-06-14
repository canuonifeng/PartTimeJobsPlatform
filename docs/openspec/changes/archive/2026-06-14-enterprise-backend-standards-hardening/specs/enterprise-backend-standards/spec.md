# enterprise-backend-standards Specification

## 需求：Controller 接口契约符合企业后端规范

### 场景：写操作使用 POST 和 cmd 请求体

Given 企业后端存在创建、更新、删除、状态变更或业务动作接口
When 开发者检查 Controller 方法注解和入参
Then 写操作必须使用 `@PostMapping`
And 写操作必须使用 `@RequestBody` cmd 对象承载入参
And 写操作不得使用 `@PutMapping`、`@DeleteMapping`、`@PatchMapping`
And 写操作不得混用 `@RequestParam` 与 `@RequestBody`

### 场景：查询操作使用 GET 和 query 参数

Given 企业后端存在列表、详情或统计查询接口
When 开发者检查 Controller 方法注解和入参
Then 查询操作可以使用 `@GetMapping`
And 查询条件可以使用 `@RequestParam`
And 查询接口不得直接返回数据库 Entity

### 场景：Controller 不使用 Map 作为业务契约

Given 企业后端 Controller 接收或返回业务数据
When 开发者检查方法签名
Then 请求体必须使用明确的 `cmd` 类型
And 响应必须使用明确的 `vo` 类型并包裹在 `ApiResponse` 中
And Controller 不得使用 `Map` 作为业务请求体或业务响应体

### 场景：Controller 不使用路径参数

Given 企业后端 Controller 需要接收资源 ID 或动作 ID
When 开发者检查方法参数
Then 查询接口应使用 query 参数
And 写接口应使用 cmd 请求体字段
And Controller 不得使用 `@PathVariable`

## 需求：列表分页在数据库层完成

### 场景：分页列表使用 LIMIT/OFFSET 和 COUNT

Given 企业后端存在分页列表接口
When Service 调用 Mapper 获取分页数据
Then Mapper XML 必须提供当前页查询并使用 `LIMIT/OFFSET`
And Mapper XML 必须提供同条件 `COUNT(*)`
And Service 不得先查询全量后使用 `subList`、`stream().skip` 或 `stream().limit` 做分页

### 场景：聚合列表按查询额度获取记录

Given 企业后端存在聚合多个待办来源的列表接口
When 接口需要限制返回数量
Then Service 应按剩余数量调用各来源 Mapper 查询
And 不得先聚合超量记录后在内存中截断

## 需求：列表关联数据避免 N+1 查询

### 场景：列表 VO 需要关联字段

Given 企业后端列表记录需要展示关联对象字段
When Service 组装 VO
Then Service 应先收集记录中的关联 ID
And Service 应通过批量 Mapper 方法一次性读取关联数据
And Service 应使用内存 Map 填充 VO
And Service 不得在循环或 `stream().map` 中执行数据库读操作

### 场景：单条详情允许必要单条查询

Given 企业后端单条详情或单个业务动作需要加载单条记录
When Service 执行业务流程
Then 可以使用必要的 `findById` 或单条查询
And 不应误判为列表 N+1 问题

## 需求：Mapper 实现集中在 XML

### 场景：新增 Mapper 查询

Given 企业后端需要新增数据访问方法
When 开发者添加 Mapper 方法
Then SQL 应优先放在 `src/main/resources/mapper/*.xml`
And Mapper 接口与 XML 的 namespace 和方法 ID 应一一对应
And 用户输入必须使用 `#{}` 参数绑定

### 场景：历史只读 Mapper 迁移

Given 历史遗留只读 Mapper 使用注解 SQL
When 本次变更不涉及其业务行为
Then 可以记录为剩余规范债务
And 后续应按模块迁移到 XML，避免一次性大范围改动

## 需求：前端 API 与后端契约同步

### 场景：后端写接口改为 POST body

Given 企业后端写接口从旧 method/query 改为 POST cmd
When 企业 PC 或企业小程序触发对应操作
Then 前端 API 封装必须使用 POST
And 请求体字段必须与后端 cmd 字段一致
And 页面业务行为保持不变

## 需求：规范治理必须可验证

### 场景：提交前执行规范扫描和构建

Given 企业后端规范治理改动完成
When 开发者准备提交
Then 必须执行 Controller 硬性违规扫描
And 必须执行内存分页扫描
And 必须执行 `enterprise-service` 编译和测试编译
And 涉及前端 API 时必须执行对应前端构建
And 必须执行 `git diff --check`
