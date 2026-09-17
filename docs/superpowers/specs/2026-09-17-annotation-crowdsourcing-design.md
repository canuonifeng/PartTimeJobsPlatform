# 数据标注众包功能设计

## 1. 概述

在现有零工平台基础上，新增数据标注众包能力。企业端发布标注任务包，C端工人抢单，外部标注系统完成后回调通知，平台进行结算。

### 1.1 核心流程

```
企业发布标注任务包 → 工人浏览/抢单 → 外部标注系统执行标注 → 外部系统回调确认完成 → 企业结算 → 工人提现
```

### 1.2 系统边界

| 职责 | 本平台 | 外部标注系统 |
|---|---|---|
| 任务包管理 | ✅ 发布/编辑/关闭 | ❌ |
| 子任务管理 | ✅ 批次拆分/抢单 | ❌ |
| 数据源管理 | ❌ | ✅ 上传/管理待标注数据 |
| 标注操作 | ❌ | ✅ 具体标注界面 |
| 质量审核 | ❌ | ✅ 标注结果审核 |
| 抢单/结算 | ✅ 抢单/回调/结算 | ❌ |

### 1.3 MVP 范围

**做**：发包、抢单、外部回调确认、结算
**不做**（后续迭代）：课程学习系统、技能证书系统、标注质量审核

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

ALTER TABLE jobs ADD COLUMN callback_url VARCHAR(500) DEFAULT NULL;
-- 外部系统回调地址（可选，也可统一配置）

ALTER TABLE jobs ADD COLUMN external_system_type VARCHAR(50) DEFAULT NULL;
-- 外部系统类型（如 ANNOTATION_SYSTEM），用于人员映射
```

### 2.2 新增 annotation_submissions 表

```sql
CREATE TABLE annotation_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL COMMENT '关联 job_schedules.id（子任务）',
    worker_id BIGINT NOT NULL COMMENT '工人ID',
    job_id BIGINT NOT NULL COMMENT '关联 jobs.id（任务包）',
    items_completed INT DEFAULT 0 COMMENT '完成的数据条数',
    status VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/CONFIRMED/REJECTED',
    external_submission_id VARCHAR(100) COMMENT '外部系统的提交ID',
    submitted_at DATETIME COMMENT '工人提交时间',
    confirmed_at DATETIME COMMENT '外部系统确认时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schedule_worker (schedule_id, worker_id)
) COMMENT '标注完成记录';
```

### 2.3 新增 external_worker_mapping 表（外部系统人员映射）

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

### 2.4 job_schedules 表扩展

```sql
ALTER TABLE job_schedules ADD COLUMN total_items INT DEFAULT NULL COMMENT '该子任务数据条数';
ALTER TABLE job_schedules ADD COLUMN items_completed INT DEFAULT 0 COMMENT '已标注条数';
ALTER TABLE job_schedules ADD COLUMN external_batch_id VARCHAR(100) COMMENT '外部系统批次ID';
```

## 3. 后端 API 设计

### 3.1 企业端（enterprise-service）

复用现有 Controller 结构，在 `JobController` 中扩展：

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/enterprise/jobs` | POST | 创建标注任务包（task_type=ANNOTATION） |
| `/api/enterprise/jobs/update` | POST | 更新标注任务 |
| `/api/enterprise/jobs/publish` | POST | 发布（DRAFT→PUBLISHED） |
| `/api/enterprise/jobs/close` | POST | 关闭任务 |
| `/api/enterprise/jobs/schedules` | GET | 查看子任务列表（含抢单情况和完成进度） |
| `/api/enterprise/jobs/schedules` | POST | 新增子任务（批次） |
| `/api/enterprise/jobs/submissions` | GET | 查看标注完成记录 |
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

创建排班（子任务）时同步写入 `total_items` 和 `external_batch_id`。

### 3.2 C 端（c-service）

复用现有 Controller 结构：

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/worker/jobs` | GET | 搜索岗位（含标注任务，支持 task_type 筛选） |
| `/api/worker/jobs/detail` | GET | 岗位详情（标注任务展示计价信息和子任务进度） |
| `/api/worker/jobs/apply` | POST | 抢子任务（复用现有报名逻辑） |
| `/api/worker/jobs/applications/my` | GET | 我的抢单（含标注任务） |

#### 搜索改造

`JobMapper.search` SQL 增加 `task_type` 筛选条件：
```xml
<if test="taskType != null and taskType != ''">
    AND task_type = #{taskType}
</if>
```

#### 详情改造

`JobDetailVO` 新增字段：
- `taskType`：任务类型
- `pricingMode`：计价模式
- `pricePerUnit`：单价
- `totalItems`：总条数
- `itemsCompleted`：已完成条数
- `externalTaskId`：外部任务ID

### 3.3 外部系统回调 API（新增）

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
5. 插入 `annotation_submissions` 记录（`external_submission_id` 唯一约束防重放）
6. 更新 `job_schedules.items_completed += items_completed`
7. 更新 `jobs.accepted_count`

#### 安全

- 回调接口需携带 API Key（Header: `X-Callback-Key`）
- API Key 存储在本地配置文件 `application.yml` 中：`external.callback.api-key`
- 同时支持环境变量覆盖：`EXTERNAL_CALLBACK_API_KEY`
- 防重放：`external_submission_id` UNIQUE 约束，重复回调返回成功但不重复处理
- 回调接口放在 `/api/external/**` 路径下，不走 JWT 认证，仅校验 API Key

## 4. 前端设计

### 4.1 enterprise-pc 管理后台

#### JobForm.vue 修改

表单增加"任务类型"选择：
- 选择"零工"：显示现有字段
- 选择"标注任务"：额外显示
  - 计价模式（按件/按包）
  - 单价/总价
  - 总数据条数
  - 外部任务ID
  - 外部系统类型（用于人员映射）

#### JobList.vue 修改

- 列表增加"任务类型"列
- 增加任务类型筛选下拉框

#### ScheduleList.vue 修改

- 子任务列表增加"已标注条数"、"外部批次ID"列

### 4.2 worker-uniapp 小程序

#### jobList.vue 修改

- 搜索接口增加 `taskType` 参数
- 标注任务卡片展示：
  - 岗位名称
  - 计价信息（如"按件计费 ¥0.5/条"）
  - 进度（如"已完成 3200/10000"）
  - 企业名称

#### jobDetail.vue 修改

- 标注任务展示：计价模式、单价、总条数、子任务进度
- 子任务列表显示每个批次的完成情况

#### 标注任务卡片样式

```
📦 图片分类标注任务
💰 按件计费 ¥0.5/条 | 共10000条
📊 已完成 3200/10000
🏢 数据科技有限公司
```

## 5. 结算流程

### 5.1 状态流转

```
企业创建 DRAFT → 发布 PUBLISHED → 工人抢单 ACCEPTED → 外部回调 CONFIRMED → 企业结算 PAID
                                                                ↘ 超时未完成 → EXPIRED
```

### 5.2 结算金额计算

| 计价模式 | 计算公式 |
|---|---|
| 按件（PER_ITEM） | `items_completed × price_per_unit` |
| 按包（PER_PACKAGE） | `price_per_unit`（固定金额） |

### 5.3 结算流程

1. 外部系统回调确认完成 → `annotation_submissions.status = 'CONFIRMED'`
2. 企业端发起结算 → 根据 `CONFIRMED` 的 submission 计算应付金额
3. 写入 `worker_balances`（复用现有 `SettlementServiceImpl` 逻辑）
4. 工人提现（完全复用现有提现流程）

### 5.4 状态枚举扩展

```java
// JobStatus 新增
ANNOTATION_DRAFT("标注草稿"),
ANNOTATION_PUBLISHED("标注已发布"),
ANNOTATION_CLOSED("标注已关闭"),
ANNOTATION_EXPIRED("标注已过期")

// 新增枚举
AnnotationSubmissionStatus:
    SUBMITTED("已提交"),
    CONFIRMED("已确认"),
    REJECTED("已拒绝")
```

## 6. 安全考虑

- 回调接口需 API Key 验证
- `external_submission_id` 唯一约束防重放
- 标注任务的 `job_schedules` 抢单逻辑复用现有名额控制
- 企业只能操作自己的标注任务（复用现有权限校验）

## 7. 测试策略

- 企业端：标注任务 CRUD、子任务管理、完成记录查看
- C 端：标注任务搜索、详情、抢单
- 回调：完成回调、进度回调、防重放
- 结算：金额计算、余额变动、提现
- 集成：标注任务全流程（发布→抢单→回调→结算）
