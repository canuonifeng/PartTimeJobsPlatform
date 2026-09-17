# 数据标注众包功能设计

## 1. 概述

在现有零工平台基础上，新增数据标注众包能力。企业端发布标注任务包，C端工人抢单批次，外部标注系统完成标注后回调，质检通过后自动结算。

### 1.1 核心流程

```
企业发布标注任务包（含N个批次）
  → 工人浏览任务包，抢某个批次
  → 报名单审核通过
  → 生成数据标注任务单
  → 工人在外部标注平台完成标注
  → 外部系统回调，任务状态改为"已提交"
  → 质检通过，任务状态改为"已完成"
  → 自动结算，金额累计到个人余额
```

### 1.2 数据层级

```
标注任务包（jobs 表，task_type=ANNOTATION）
  └── 批次（job_schedules 表，每个批次=N条数据）
        └── 抢单/报名单（schedule_applications 表）
              └── 任务单（annotation_task_orders 表，跟踪进度和状态）
```

### 1.3 系统边界

| 职责 | 本平台 | 外部标注系统 |
|---|---|---|
| 任务包管理 | ✅ 发布/编辑/关闭 | ❌ |
| 批次管理 | ✅ 批次拆分/抢单 | ❌ |
| 抢单/报单审核 | ✅ 抢单/审核 | ❌ |
| 任务单管理 | ✅ 生成任务单/跟踪进度 | ❌ |
| 数据源管理 | ❌ | ✅ 上传/管理待标注数据 |
| 标注操作 | ❌ | ✅ 具体标注界面 |
| 质量审核 | ❌ | ✅ 标注结果质检 |
| 结算 | ✅ 自动结算/余额/提现 | ❌ |

### 1.4 MVP 范围

**做**：发包、抢单、报单审核、任务单生成、外部回调、自动结算
**不做**（后续迭代）：课程学习系统、技能证书系统

## 2. 数据库设计

### 2.1 jobs 表扩展

```sql
ALTER TABLE jobs ADD COLUMN task_type VARCHAR(20) DEFAULT 'WORK' AFTER id;
-- WORK: 零工岗位（默认）
-- ANNOTATION: 数据标注任务

ALTER TABLE jobs ADD COLUMN pricing_mode VARCHAR(20) DEFAULT NULL;
-- PER_ITEM: 按件计费（每条数据单价）
-- PER_PACKAGE: 按包计费（整个任务包固定价格）

ALTER TABLE jobs ADD COLUMN price_per_unit DECIMAL(10,2) DEFAULT NULL;
-- 按件时 = 每条单价
-- 按包时 = 总价

ALTER TABLE jobs ADD COLUMN total_items INT DEFAULT NULL;
-- 标注任务总数据条数（仅标注任务有值）

ALTER TABLE jobs ADD COLUMN external_task_id VARCHAR(100) DEFAULT NULL;
-- 外部标注系统中的任务ID，用于回调关联

ALTER TABLE jobs ADD COLUMN external_system_type VARCHAR(50) DEFAULT NULL;
-- 外部系统类型（如 ANNOTATION_SYSTEM），用于人员映射

ALTER TABLE jobs ADD COLUMN auto_settle TINYINT(1) DEFAULT 1;
-- 标注任务完成后是否自动结算（1=自动, 0=手动）
```

### 2.2 job_schedules 表扩展（批次）

```sql
ALTER TABLE job_schedules ADD COLUMN total_items INT DEFAULT NULL COMMENT '该批次数据条数';
ALTER TABLE job_schedules ADD COLUMN items_completed INT DEFAULT 0 COMMENT '已标注条数';
ALTER TABLE job_schedules ADD COLUMN external_batch_id VARCHAR(100) COMMENT '外部系统批次ID';
```

### 2.3 新增 annotation_task_orders 表（任务单）

工人抢单审核通过后生成，跟踪标注任务进度和状态。

```sql
CREATE TABLE annotation_task_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '关联 schedule_applications.id（报名单）',
    schedule_id BIGINT NOT NULL COMMENT '关联 job_schedules.id（批次）',
    worker_id BIGINT NOT NULL COMMENT '工人ID',
    job_id BIGINT NOT NULL COMMENT '关联 jobs.id（任务包）',
    items_completed INT DEFAULT 0 COMMENT '已标注条数',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务单状态',
    -- PENDING: 待标注（审核通过后生成）
    -- IN_PROGRESS: 标注中（工人在外部平台开始标注）
    -- SUBMITTED: 已提交（外部系统回调确认完成）
    -- QUALITY_CHECK: 质检中
    -- COMPLETED: 已完成（质检通过，自动结算）
    -- REJECTED: 质检不通过（需重新标注）
    external_submission_id VARCHAR(100) COMMENT '外部系统的提交ID',
    submitted_at DATETIME COMMENT '外部系统回调时间',
    completed_at DATETIME COMMENT '质检通过时间',
    settled_at DATETIME COMMENT '结算时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_application (application_id),
    UNIQUE KEY uk_external_submission (external_submission_id)
) COMMENT '数据标注任务单';
```

### 2.4 新增 external_worker_mapping 表（外部系统人员映射）

```sql
CREATE TABLE external_worker_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL COMMENT '本系统工人ID（c_worker.id）',
    external_system_type VARCHAR(50) NOT NULL COMMENT '外部系统类型（如 ANNOTATION_SYSTEM）',
    external_worker_id VARCHAR(100) NOT NULL COMMENT '外部系统中的工人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_external (external_system_type, external_worker_id),
    UNIQUE KEY uk_system_worker (external_system_type, worker_id)
) COMMENT '本系统与外部系统的人员映射';
```

## 3. 状态流转

### 3.1 任务包状态（jobs.status）

```
DRAFT → publish → PUBLISHED → close → CLOSED
PUBLISHED → expire → EXPIRED
```

### 3.2 批次状态（job_schedules.status）

```
ACTIVE → 抢单 → CANCELLED（取消）
```

### 3.3 报名单状态（schedule_applications.status）

```
PENDING → 审核 → ACCEPTED / REJECTED
```

### 3.4 任务单状态（annotation_task_orders.status）⭐ 核心

```
审核通过 → PENDING（待标注）
  → 工人在外部平台开始标注 → IN_PROGRESS（标注中）
  → 外部系统回调完成 → SUBMITTED（已提交）
  → 质检通过 → COMPLETED（已完成）→ 自动结算
  → 质检不通过 → REJECTED（需重新标注）→ 重新提交 → SUBMITTED
```

### 3.5 完整流程图

```
企业创建任务包 DRAFT
  → 发布 PUBLISHED
  → 工人抢单（选择批次）→ schedule_applications PENDING
  → 企业审核通过 → schedule_applications ACCEPTED
  → 自动生成 annotation_task_orders PENDING
  → 工人在外部平台标注 → IN_PROGRESS
  → 外部系统回调 → SUBMITTED
  → 质检通过 → COMPLETED
  → 自动结算 → worker_balances 入账
```

## 4. 后端 API 设计

### 4.1 企业端（enterprise-service）

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/enterprise/jobs` | POST | 创建标注任务包（task_type=ANNOTATION） |
| `/api/enterprise/jobs/update` | POST | 更新标注任务 |
| `/api/enterprise/jobs/publish` | POST | 发布（DRAFT→PUBLISHED） |
| `/api/enterprise/jobs/close` | POST | 关闭任务 |
| `/api/enterprise/jobs/schedules` | GET | 查看批次列表（含抢单情况和完成进度） |
| `/api/enterprise/jobs/schedules` | POST | 新增批次 |
| `/api/enterprise/jobs/task-orders` | GET | 查看任务单列表（含进度和状态） |
| `/api/enterprise/jobs/task-orders/review` | POST | 审核报名单（通过/拒绝） |
| `/api/enterprise/external-worker-mapping` | POST | 新增外部系统人员映射 |
| `/api/enterprise/external-worker-mapping` | GET | 查询人员映射列表 |

#### 标注任务发布流程

复用现有 `JobServiceImpl.createJob`，新增字段映射：
- `taskType` → `jobs.task_type`
- `pricingMode` → `jobs.pricing_mode`
- `pricePerUnit` → `jobs.price_per_unit`
- `totalItems` → `jobs.total_items`
- `externalTaskId` → `jobs.external_task_id`
- `externalSystemType` → `jobs.external_system_type`

创建批次（子任务）时同步写入 `total_items` 和 `external_batch_id`。

#### 报单审核 → 生成任务单

`TaskOrderService.createByApplication(Long applicationId)`：
1. 查找 `schedule_applications` 记录
2. 校验状态必须为 `ACCEPTED`
3. 创建 `annotation_task_orders` 记录，状态为 `PENDING`
4. 关联 `application_id`、`schedule_id`、`worker_id`、`job_id`

### 4.2 C 端（c-service）

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/worker/jobs` | GET | 搜索岗位（含标注任务，支持 task_type 筛选） |
| `/api/worker/jobs/detail` | GET | 标注任务详情（不含地点/图片/班次） |
| `/api/worker/jobs/apply` | POST | 抢批次（复用现有报名逻辑） |
| `/api/worker/jobs/applications/my` | GET | 我的抢单（含标注任务报名单） |
| `/api/worker/jobs/task-orders/my` | GET | 我的任务单列表（含进度和状态） |

#### 标注任务详情页（与岗位详情页不同）

标注任务详情页**不需要**：
- ❌ 工作地点（location/address）
- ❌ 岗位图片（image_url）
- ❌ 班次时间选择（schedules 的 startTime/endTime）

标注任务详情页**需要**：
- ✅ 任务包标题、描述、要求
- ✅ 计价模式和单价
- ✅ 总数据条数、已完成条数
- ✅ 批次列表（每个批次的数据条数、已完成数、是否可抢）
- ✅ 企业信息
- ✅ 我的报单状态和任务单状态

#### 搜索改造

`JobMapper.search` SQL 增加 `task_type` 筛选条件：
```xml
<if test="taskType != null and taskType != ''">
    AND task_type = #{taskType}
</if>
```

### 4.3 外部系统回调 API（新增）

新建 `ExternalCallbackController`：

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/external/annotation/complete` | POST | 标注完成回调 |
| `/api/external/annotation/progress` | POST | 标注进度回调（可选） |

#### 完成回调请求体

```json
{
  "external_task_id": "ext_task_123",
  "external_batch_id": "batch_001",
  "external_worker_id": "worker_456",
  "items_completed": 100,
  "external_submission_id": "sub_789"
}
```

#### 回调处理逻辑

1. 验证 API Key（Header: `X-Callback-Key`）
2. 根据 `external_task_id` 找到 `jobs.id`
3. 根据 `external_batch_id` 找到 `job_schedules.id`
4. 根据 `external_worker_id` + `external_system_type` 查找 `external_worker_mapping.worker_id`
5. 更新 `annotation_task_orders.status = 'SUBMITTED'`
6. 更新 `annotation_task_orders.items_completed`、`submitted_at`
7. 更新 `job_schedules.items_completed`
8. 如果任务包 `auto_settle = 1`，触发自动结算

#### 进度回调请求体

```json
{
  "external_task_id": "ext_task_123",
  "external_batch_id": "batch_001",
  "external_worker_id": "worker_456",
  "items_completed": 50
}
```

#### 安全

- 回调接口需携带 API Key（Header: `X-Callback-Key`）
- API Key 存储在本地配置文件 `application.yml` 中：`external.callback.api-key`
- 同时支持环境变量覆盖：`EXTERNAL_CALLBACK_API_KEY`
- 防重放：`external_submission_id` UNIQUE 约束，重复回调返回成功但不重复处理
- 回调接口放在 `/api/external/**` 路径下，不走 JWT 认证，仅校验 API Key

## 5. 前端设计

### 5.1 enterprise-pc 管理后台

#### JobForm.vue 修改

表单增加"任务类型"选择：
- 选择"零工"：显示现有字段
- 选择"标注任务"：额外显示
  - 计价模式（按件/按包）
  - 单价/总价
  - 总数据条数
  - 外部任务ID
  - 外部系统类型

#### JobList.vue 修改

- 列表增加"任务类型"列
- 增加任务类型筛选下拉框

#### 批次管理（ScheduleList.vue）修改

- 批次列表增加"已标注条数"、"外部批次ID"列

#### 任务单管理（新增 TaskOrderList.vue）

- 展示任务单列表：任务包名称、批次、工人、进度（已标注/总数）、状态、操作
- 支持按状态筛选
- 操作：查看详情、标记质检结果

### 5.2 worker-uniapp 小程序

#### jobList.vue 修改

- 搜索接口增加 `taskType` 参数
- 标注任务卡片展示：
  - 任务包名称
  - 计价信息（如"按件计费 ¥0.5/条"）
  - 进度（如"已完成 3200/10000"）
  - 企业名称

#### jobDetail.vue 修改（标注任务专用）

标注任务详情页展示（与岗位详情页不同）：
- 任务包标题、描述、要求
- 计价模式和单价
- 总数据条数、已完成条数
- 批次列表（每个批次：数据条数、已完成数、抢单按钮）
- 我的报单状态和任务单状态

**不展示**：工作地点、岗位图片、班次时间选择

#### taskOrderList.vue（新增）

- 展示我的任务单列表：任务名称、批次、进度、状态
- 支持按状态筛选
- 点击进入任务单详情

#### taskOrderDetail.vue（新增）

- 任务单详情：任务名称、批次信息、进度条、状态
- 外部标注平台跳转链接（如有）

### 5.3 标注任务卡片样式

```
📦 图片分类标注任务
💰 按件计费 ¥0.5/条 | 共10000条
📊 已完成 3200/10000
🏢 数据科技有限公司
```

## 6. 结算流程

### 6.1 结算金额计算

| 计价模式 | 计算公式 |
|---|---|
| 按件（PER_ITEM） | `items_completed × price_per_unit` |
| 按包（PER_PACKAGE） | `price_per_unit`（固定金额） |

### 6.2 自动结算流程

1. 外部系统回调确认完成 → `annotation_task_orders.status = 'SUBMITTED'`
2. 质检通过 → `annotation_task_orders.status = 'COMPLETED'`
3. 自动触发结算：
   - 根据计价模式计算应付金额
   - 写入 `worker_balances`（余额增加、累计收入增加）
   - 创建 `balance_transactions`（type=EARNINGS）
   - 更新 `annotation_task_orders.settled_at`
4. 工人可随时提现（完全复用现有提现流程）

### 6.3 结算金额计算

```
按件：items_completed × price_per_unit
按包：price_per_unit（固定金额）
```

## 7. 安全考虑

- 回调接口需 API Key 验证（本地配置 + 环境变量覆盖）
- `external_submission_id` 唯一约束防重放
- 批次抢单复用现有名额控制（`slots_available`）
- 企业只能操作自己的标注任务（复用现有权限校验）
- 任务单与报名单一对一绑定（`application_id` UNIQUE）

## 8. 测试策略

- 企业端：标注任务 CRUD、批次管理、报单审核、任务单查看
- C 端：标注任务搜索、详情（无地点/图片）、抢单、任务单列表
- 回调：完成回调、进度回调、防重放
- 结算：金额计算、自动结算、余额变动、提现
- 集成：标注任务全流程（发布→抢单→审核→任务单→回调→质检→结算）
