# Design

## Audit Basis

依据 `docs/后端技术规范.md`，重点检查以下规则：

- Controller 只能使用 `@GetMapping` 和 `@PostMapping`。
- POST 接口只能使用 `@RequestBody` 作为入参；复杂参数优先 cmd。
- 查询参数使用 `@RequestParam`，不要使用 `@PathVariable`。
- Controller 不返回 Entity，优先返回 VO，禁止用 `Map` 返回，用 `ApiResponse` 包装。
- Controller 不直接注入或调用 Mapper。
- 分页必须在数据库层完成，禁止 Service 内存分页。
- Service 禁止在循环体或 `stream` 列表组装中执行数据库读操作。
- Entity 仅保留数据库表字段，关联数据放 VO。
- 涉及金额使用 `BigDecimal`，敏感信息不得打印。
- Mapper SQL 优先放 XML，注解 SQL 需治理或标记遗留。

## Static Scan Commands

```bash
rg -n "@(PutMapping|DeleteMapping|PatchMapping)|@PathVariable|ApiResponse<.*Map|@RequestBody\s+Map|@RequestBody\s+List|Map<" c-service/src/main/java/com/parttime/*/controller enterprise-service/src/main/java/com/parttime/*/controller platform-service/src/main/java/com/parttime/*/controller
rg -n "subList|stream\(\)\.skip|stream\(\)\.limit|\.skip\(|\.limit\(" c-service/src/main/java/com/parttime/*/service/impl enterprise-service/src/main/java/com/parttime/*/service/impl platform-service/src/main/java/com/parttime/*/service/impl
rg -n "@(Select|Update|Insert|Delete)" c-service/src/main/java/com/parttime/*/mapper enterprise-service/src/main/java/com/parttime/*/mapper platform-service/src/main/java/com/parttime/*/mapper
rg -n "System\.out|System\.err|printStackTrace|bankAccount|openId|token|password" c-service/src/main/java enterprise-service/src/main/java platform-service/src/main/java
```

## Non-compliance Inventory

### 1. `c-service` Controller Contract

- `c-service/src/main/java/com/parttime/cservice/controller/AuthController.java:105` 使用 `@PutMapping("/profile")`。
- `c-service/src/main/java/com/parttime/cservice/controller/ProfileController.java:74` 使用 `@PutMapping("/profile")`。
- `c-service/src/main/java/com/parttime/cservice/controller/NotificationController.java:38` 使用 `@PutMapping("/read")`。
- `c-service/src/main/java/com/parttime/cservice/controller/ReferralController.java:81` 使用 `@PutMapping("/config")`。
- `c-service/src/main/java/com/parttime/cservice/controller/WorkerBankCardController.java:38` 使用 `@PutMapping`。
- `c-service/src/main/java/com/parttime/cservice/controller/WorkerBankCardController.java:50` 使用 `@DeleteMapping`。
- `c-service/src/main/java/com/parttime/cservice/controller/WorkerSettingsController.java:29` 使用 `@PutMapping`。
- `c-service/src/main/java/com/parttime/cservice/controller/JobController.java:64` POST 报名接口混用 `@RequestParam Long id` 与 `@RequestBody ApplyJobCmd`。
- `c-service/src/main/java/com/parttime/cservice/controller/CorrectionController.java:31` 使用 `@RequestBody Map<String,Object>`。
- `c-service/src/main/java/com/parttime/cservice/controller/WithdrawalController.java:41` 使用 `@RequestBody Map<String,Object>`。
- `c-service/src/main/java/com/parttime/cservice/controller/ReferralController.java:82` 使用 `@RequestBody List<ReferralConfig>`，缺少 cmd 包装。
- `c-service/src/main/java/com/parttime/cservice/controller/PublicConfigController.java:25` 返回 `ApiResponse<Map<String,String>>`。
- `c-service/src/main/java/com/parttime/cservice/controller/ReferralController.java:43` 直接返回 `Map<String,String>`，且未用 `ApiResponse` 包装。

### 2. `enterprise-service` Controller Contract

当前工作区已有部分企业端规范化改动，仍需确认以下项：

- `enterprise-service/src/main/java/com/parttime/enterprise/controller/FileController.java:25` 文件上传 POST 使用 `@RequestParam("file") MultipartFile`，与“POST 只能 `@RequestBody`”存在冲突；该类接口需要定义例外或改造上传协议。
- `enterprise-service/src/main/java/com/parttime/enterprise/controller/WorkerProfileController.java:33` POST 评价接口混用 `@RequestParam workerId` 与 `@RequestBody EvaluationCmd`。
- 已改造中的企业端 Controller 需复扫确认不再存在 `@PutMapping`、`@DeleteMapping`、`Map` 入参/返回、写操作 query/body 混用。

### 3. `platform-service` Controller Contract

- `platform-service/src/main/java/com/parttime/platform/controller/JobCategoryController.java:41` 使用 `@PutMapping`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobCategoryController.java:47` 使用 `@DeleteMapping`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobReportController.java:40` 使用 `@PutMapping("/dismiss")`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobReportController.java:48` 使用 `@PutMapping("/ban")`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobTagController.java:43` 使用 `@PutMapping("/job-tag-groups")`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobTagController.java:49` 使用 `@DeleteMapping("/job-tag-groups")`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobTagController.java:61` 使用 `@PutMapping("/job-tags")`。
- `platform-service/src/main/java/com/parttime/platform/controller/JobTagController.java:67` 使用 `@DeleteMapping("/job-tags")`。
- `platform-service/src/main/java/com/parttime/platform/controller/ReferralController.java:29` 使用 `@PutMapping("/config")`。
- `platform-service/src/main/java/com/parttime/platform/controller/SystemConfigController.java:33` 使用 `@PutMapping`。
- `platform-service/src/main/java/com/parttime/platform/controller/EnterpriseAccountController.java:28`、`:52` 使用 `@RequestBody Map`。
- `platform-service/src/main/java/com/parttime/platform/controller/EnterpriseController.java:27`、`:34`、`:52`、`:58` 使用 `@RequestBody Map`。
- `platform-service/src/main/java/com/parttime/platform/controller/WorkerController.java:26`、`:34`、`:46`、`:52` 使用 `@RequestBody Map`。
- `platform-service/src/main/java/com/parttime/platform/controller/EnterpriseRealNameAuthReviewController.java:29`、`:40` POST 使用 `@RequestParam id`，`:41` 使用 `@RequestBody Map`。
- `platform-service/src/main/java/com/parttime/platform/controller/WorkerRealNameAuthReviewController.java:29`、`:40` POST 使用 `@RequestParam id`，`:41` 使用 `@RequestBody Map`。
- `platform-service/src/main/java/com/parttime/platform/controller/ReferralController.java:44`、`:51` POST 使用 `@RequestParam id` 且 body 为 `Map`。
- 平台实名认证审核 Controller 内存在 try/catch 包裹业务异常，建议统一交给全局异常处理或现有异常约定。

### 4. Mapper Annotation SQL

- `c-service/src/main/java/com/parttime/cservice/mapper/CompanyWorkerInsertMapper.java:9` 使用 `@Insert` 注解 SQL。
- `enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseMapper.java:11`、`:14`、`:17`、`:20` 使用 `@Select` / `@Update` 注解 SQL。
- `enterprise-service/src/main/java/com/parttime/enterprise/mapper/WorkerSyncMapper.java:13`、`:16`、`:19`、`:22`、`:25` 使用单条注解 SQL；该 Mapper 还存在单条查询方法，若用于列表循环会形成 N+1，应优先使用批量方法。

### 5. Entity Association Fields

需结合表结构确认是否为真实表字段：

- `platform-service/src/main/java/com/parttime/platform/pojo/entity/WorkerRealNameAuth.java:11`、`:12` 包含 `workerName`、`workerPhone`。
- `platform-service/src/main/java/com/parttime/platform/pojo/entity/WithdrawalRecord.java:12`、`:13` 包含 `workerName`、`workerPhone`。
- `platform-service/src/main/java/com/parttime/platform/pojo/entity/EnterpriseRealNameAuth.java:11` 包含 `companyName`。
- `platform-service/src/main/java/com/parttime/platform/pojo/entity/Enterprise.java:12` 包含 `companyName`，如果数据库字段名确为 `company_name` 则可保留。
- `c-service/src/main/java/com/parttime/cservice/pojo/entity/Enterprise.java:13` 包含 `companyName`，如果数据库字段名确为 `company_name` 则可保留。
- `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/ScheduleShift.java:42`、`c-service/src/main/java/com/parttime/cservice/pojo/entity/ShiftEntity.java:35` 包含 `locationName`，若对应 `location_name` 表字段则可保留。

### 6. Sensitive Logging and Development Stub

- `c-service/src/main/java/com/parttime/cservice/service/impl/WeChatPayServiceImpl.java:24` 日志打印 `openId`，应脱敏或移除。
- `c-service/src/main/java/com/parttime/cservice/service/impl/WeChatPayServiceImpl.java:56` 日志打印完整 `bankAccount`，违反敏感信息日志规范。
- `c-service/src/main/java/com/parttime/cservice/service/impl/WorkerServiceImpl.java:145` 使用 `System.out.println` 打印短信验证码，生产风险高，应替换为短信服务或受环境控制的脱敏日志。

### 7. Pagination and N+1

- 当前静态扫描未发现明确 `subList`、`stream().skip()`、`stream().limit()` 内存分页命中。
- 仍需对列表组装中的数据库读操作做人工复核，尤其是 `stream().map` 或 `for` 中调用 `mapper.find...` 的场景。
- 企业端此前已修复 `ApplicationServiceImpl` 报名列表内存分页和部分 `OperationServiceImpl` 截断逻辑，后续应以复扫结果为准。

## Design Decisions

- Controller 硬性违规优先处理，因为会直接影响接口契约一致性。
- `MultipartFile` 上传接口作为规范冲突项单独决策：要么补充文件上传例外规则，要么改为前端先获取上传凭证再 JSON 提交文件元数据。
- Entity 字段先与数据库表结构核对后再迁移，避免误删真实字段。
- Mapper 注解 SQL 优先迁移新增和高频接口，低风险遗留可先登记债务。
- 涉及前端调用的方法变更必须同步前端 API 封装，并做对应构建验证。

## Verification Strategy

- 三个后端静态扫描无硬性 Controller 违规。
- 三个后端静态扫描无内存分页命中。
- 三个后端静态扫描无敏感日志命中。
- `c-service`、`enterprise-service`、`platform-service` 均通过 Maven 编译或测试。
- 受影响前端应用通过构建验证。
