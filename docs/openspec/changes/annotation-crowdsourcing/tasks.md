## Phase 1: 数据库与后端基础

### 1. 数据库迁移

- [ ] 1.1 创建迁移脚本 `scripts/28_annotation_crowdsourcing.sql`：jobs表扩展字段、job_schedules表扩展字段、新建annotation_task_orders表、新建external_worker_mapping表
- [ ] 1.2 本地执行脚本验证表结构正确

### 2. 后端枚举与实体

- [ ] 2.1 新增 `AnnotationTaskOrderStatus` 枚举（PENDING/IN_PROGRESS/SUBMITTED/COMPLETED/REJECTED）
- [ ] 2.2 新增 `PricingMode` 枚举（PER_ITEM/PER_PACKAGE）
- [ ] 2.3 新增 `TaskType` 枚举（WORK/ANNOTATION）
- [ ] 2.4 新增 `AnnotationTaskOrder` 实体类
- [ ] 2.5 新增 `ExternalWorkerMapping` 实体类
- [ ] 2.6 扩展 `Job` 实体类，新增 task_type、pricing_mode 等字段
- [ ] 2.7 扩展 `JobSchedule` 实体类，新增 total_items、external_batch_id 字段

### 3. 后端 Mapper

- [ ] 3.1 新增 `AnnotationTaskOrderMapper` 接口和 XML（insert/update/selectByWorkerId/selectByJobId）
- [ ] 3.2 新增 `ExternalWorkerMappingMapper` 接口和 XML（insert/selectByExternalId/selectByWorkerId）
- [ ] 3.3 扩展 `JobMapper`，search 方法增加 task_type 筛选条件
- [ ] 3.4 扩展 `JobScheduleMapper`，新增 total_items、external_batch_id 字段映射

---

## Phase 2: 企业端后端

### 4. 企业端任务包管理

- [ ] 4.1 扩展 `JobServiceImpl.createJob`，支持 task_type=ANNOTATION 创建标注任务包
- [ ] 4.2 扩展 `JobServiceImpl.publishJob`，支持标注任务包发布
- [ ] 4.3 扩展 `JobServiceImpl.closeJob`，支持标注任务包关闭
- [ ] 4.4 扩展 `JobController`，创建/发布/关闭接口支持标注任务类型

### 5. 企业端批次管理

- [ ] 5.1 扩展 `JobScheduleServiceImpl`，新增批次时支持 total_items、external_batch_id
- [ ] 5.2 扩展 `JobScheduleController`，批次列表展示增加外部批次ID

### 6. 企业端报单审核与任务单

- [ ] 6.1 新增 `TaskOrderService` 接口和实现
- [ ] 6.2 实现 `createByApplication`：审核通过后生成任务单（不生成排班）
- [ ] 6.3 新增 `TaskOrderController`，任务单列表查询接口
- [ ] 6.4 扩展现有 `ApplicationController`，审核通过时触发任务单生成

### 7. 企业端人员映射

- [ ] 7.1 新增 `ExternalWorkerMappingService` 接口和实现
- [ ] 7.2 新增 `ExternalWorkerMappingController`，新增/查询人员映射

---

## Phase 3: C端后端

### 8. C端标注任务展示

- [ ] 8.1 扩展 `JobServiceImpl.searchJobs`，支持 task_type 参数筛选标注任务
- [ ] 8.2 扩展 `JobServiceImpl.getJobDetail`，标注任务返回计价信息和批次进度（不返回地点/图片/班次）
- [ ] 8.3 扩展 `JobController`，搜索接口增加 taskType 参数
- [ ] 8.4 扩展 `JobSummaryVO` 和 `JobDetailVO`，新增标注任务相关字段

### 9. C端抢单

- [ ] 9.1 扩展 `JobServiceImpl.applyForJob`，标注任务抢单复用现有逻辑
- [ ] 9.2 确保标注任务抢单不生成排班（仅零工岗位生成排班）

### 10. C端任务单

- [ ] 10.1 新增 `TaskOrderController`（c-service），我的任务单列表查询
- [ ] 10.2 新增 `TaskOrderService`（c-service），查询任务单列表和详情

---

## Phase 4: 外部系统回调

### 11. 回调接口

- [ ] 11.1 新增 `ExternalCallbackController`，标注完成回调（/api/external/annotation/submit）
- [ ] 11.2 实现回调逻辑：验证API Key、查找任务单、更新状态和进度
- [ ] 11.3 新增质检结果回调（/api/external/annotation/quality-check）
- [ ] 11.4 实现质检通过逻辑：更新状态为COMPLETED、触发自动结算
- [ ] 11.5 实现质检不通过逻辑：更新状态为REJECTED
- [ ] 11.6 新增进度回调（/api/external/annotation/progress，可选）
- [ ] 11.7 实现防重放：external_submission_id 唯一约束

### 12. 自动结算

- [ ] 12.1 实现 `AnnotationSettlementService`，质检通过后自动计算报酬
- [ ] 12.2 复用 `WorkerBalanceService` 入账逻辑
- [ ] 12.3 复用 `BalanceTransactionService` 创建交易记录

---

## Phase 5: 前端

### 13. enterprise-pc 管理后台

- [ ] 13.1 `JobForm.vue` 增加任务类型选择（零工/标注），选择标注后显示计价模式、单价、总条数、外部任务ID、外部系统类型
- [ ] 13.2 `JobList.vue` 增加任务类型列和筛选下拉框
- [ ] 13.3 `ScheduleList.vue` 增加外部批次ID列
- [ ] 13.4 新增 `TaskOrderList.vue` 任务单管理页面
- [ ] 13.5 新增人员映射管理页面

### 14. worker-uniapp 小程序

- [ ] 14.1 `jobList.vue` 搜索接口增加 taskType 参数，标注任务卡片展示计价信息和进度
- [ ] 14.2 `jobDetail.vue` 标注任务详情页（不含地点/图片/班次，展示批次列表和计价信息）
- [ ] 14.3 新增 `taskOrderList.vue` 我的任务单列表页
- [ ] 14.4 新增 `taskOrderDetail.vue` 任务单详情页
- [ ] 14.5 注册新页面到 `pages.json`
- [ ] 14.6 个人中心 `profile.vue` 增加"我的任务单"入口

---

## Phase 6: 测试与验证

### 15. 后端测试

- [ ] 15.1 标注任务包 CRUD 单元测试
- [ ] 15.2 批次管理单元测试
- [ ] 15.3 抢单与报单审核单元测试
- [ ] 15.4 任务单状态流转单元测试
- [ ] 15.5 外部回调接口单元测试（标注完成、质检通过、质检不通过、防重放）
- [ ] 15.6 自动结算单元测试

### 16. 前端测试

- [ ] 16.1 enterprise-pc 标注任务发布流程测试
- [ ] 16.2 worker-uniapp 标注任务浏览、抢单流程测试
- [ ] 16.3 任务单列表和详情展示测试

### 17. 集成测试

- [ ] 17.1 标注任务全流程（发布→抢单→审核→任务单→外部回调→质检→结算）
- [ ] 17.2 与外部标注系统联调回调接口
