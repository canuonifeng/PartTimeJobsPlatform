## Why

当前零工平台仅支持企业发布岗位、工人找活报名的模式，缺少数据标注众包能力。数据标注行业需要企业发布标注任务包，工人按批次抢单完成标注，标注在外部系统进行，平台负责发包、抢单、回调确认和结算。

新增数据标注众包功能后，将实现：
1. 平台支持多种任务类型（零工 + 数据标注），扩大业务覆盖范围
2. 企业可发布标注任务包，按批次拆分，灵活管理标注需求
3. 工人可浏览标注任务，按批次抢单，赚取标注收入
4. 外部标注系统与本平台解耦，通过 API 回调完成闭环
5. 标注任务完成后自动结算，提升资金流转效率

## What Changes

### 新增模块

#### 数据标注众包
1. **任务包管理** - 企业发布/编辑/关闭标注任务包，配置计价模式（按件/按包）
2. **批次管理** - 任务包下拆分批次，每批次含数据条数和外部批次ID
3. **抢单与报单审核** - 工人按批次抢单，企业审核报单
4. **任务单管理** - 审核通过后生成任务单，跟踪标注进度和状态
5. **外部系统回调** - 标注完成回调、质检结果回调、进度回调
6. **自动结算** - 质检通过后自动计算报酬，入账工人余额
7. **人员映射** - 本系统与外部系统的工人ID映射

### Capabilities

#### New Capabilities
- `annotation-task-management`: 企业端标注任务包CRUD、批次管理、计价配置
- `annotation-order-grabbing`: C端工人浏览标注任务、按批次抢单、报单审核
- `annotation-task-order`: 任务单生成、进度跟踪、状态流转
- `annotation-callback`: 外部系统回调接口（标注完成、质检结果、进度更新）
- `annotation-settlement`: 标注任务自动结算、余额入账
- `external-worker-mapping`: 本系统与外部系统的人员ID映射管理

#### Modified Capabilities
- `job-management`: jobs表新增task_type字段，支持零工和标注任务类型区分
- `worker-user`: C端找活页面支持标注任务展示和筛选

## Impact

### Frontend
- `enterprise-pc/src/views/jobs/JobForm.vue` - 表单增加任务类型选择、计价模式、外部任务ID等字段
- `enterprise-pc/src/views/jobs/JobList.vue` - 列表增加任务类型列和筛选
- `enterprise-pc/src/views/jobs/TaskOrderList.vue` - 新增任务单管理页面
- `worker-uniapp/src/pages/jobs/jobList.vue` - 搜索增加task_type参数，标注任务卡片展示
- `worker-uniapp/src/pages/jobs/jobDetail.vue` - 标注任务详情页（不含地点/图片/班次）
- `worker-uniapp/src/pages/jobs/taskOrderList.vue` - 新增我的任务单列表页
- `worker-uniapp/src/pages/jobs/taskOrderDetail.vue` - 新增任务单详情页

### Backend
- `enterprise-service` - 新增TaskOrderController、ExternalWorkerMappingController，扩展JobController
- `c-service` - 扩展JobController支持task_type筛选，新增TaskOrderController
- 新增ExternalCallbackController（外部系统回调接口）

### Database
- jobs表扩展：task_type、pricing_mode、price_per_unit、total_items、external_task_id、external_system_type、auto_settle
- job_schedules表扩展：total_items、external_batch_id
- 新增表：annotation_task_orders（任务单）、external_worker_mapping（人员映射）

### Infrastructure
- 回调接口需配置API Key（application.yml + 环境变量）
- 外部系统需实现回调逻辑（标注完成、质检结果）
