## 架构设计

### 数据层级

```
标注任务包（jobs 表，task_type=ANNOTATION）
  └── 批次（job_schedules 表，每个批次=N条数据）
        └── 抢单/报名单（schedule_applications 表）
              └── 任务单（annotation_task_orders 表，跟踪进度和状态）
```

### 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ enterprise-pc │ │ worker-uniapp │ │ enterprise-uniapp │ │
│  │ 任务包管理 │ │ 标注任务列表 │ │ 任务包管理 │ │
│  │ 批次管理   │ │ 任务单列表 │ │ 批次管理   │ │
│  │ 任务单管理 │ │ 任务单详情 │ │ 任务单管理 │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    后端服务                              │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ enterprise-service│  │ c-service        │            │
│  │ JobController     │  │ JobController    │            │
│  │ TaskOrderController│  │ TaskOrderController│           │
│  │ ExternalWorkerMapping│  │                  │            │
│  └──────────────────┘  └──────────────────┘            │
│                           │                             │
│  ┌──────────────────────────────────────┐              │
│  │ ExternalCallbackController           │              │
│  │ /api/external/annotation/*           │              │
│  └──────────────────────────────────────┘              │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    数据库                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ jobs     │ │ job_schedules │ │ schedule_applications │ │
│  │ +task_type│ │ +total_items │ │ (复用)    │ │
│  │ +pricing │ │ +external_batch│ │              │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│  ┌──────────────────┐  ┌──────────────────┐            │
│  │ annotation_task_orders│  │ external_worker_mapping│  │
│  │ (新增)             │  │ (新增)             │            │
│  └──────────────────┘  └──────────────────┘            │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                外部标注系统                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐              │
│  │ 数据源管理│ │ 标注操作  │ │ 质量审核  │              │
│  └──────────┘ └──────────┘ └──────────┘              │
│         │             │             │                   │
│         └─────────────┴─────────────┘                   │
│                       │                                 │
│                       ▼                                 │
│              HTTP API 回调本平台                         │
└─────────────────────────────────────────────────────────┘
```

### 核心流程

```
企业创建标注任务包 DRAFT
  → 发布 PUBLISHED
  → 工人抢单（选择批次）→ schedule_applications PENDING
  → 企业审核通过 → schedule_applications ACCEPTED
  → 生成 annotation_task_orders PENDING（不生成排班、不考勤打卡）
  → 工人在外部平台标注 → 外部系统回调标注完成 → SUBMITTED
  → 外部系统质检 → 质检通过回调 → COMPLETED
  → 自动结算 → worker_balances 入账
```

### 与零工岗位的关键差异

| 特性 | 零工岗位 | 标注任务 |
|---|---|---|
| 任务类型 | task_type=WORK | task_type=ANNOTATION |
| 计价模式 | HOURLY/DAILY/PER_SHIFT | PER_ITEM/PER_PACKAGE |
| 详情页 | 含地点/图片/班次 | 不含地点/图片/班次 |
| 审核通过后 | 生成排班+考勤 | 仅生成任务单 |
| 考勤打卡 | 需要 | 不需要 |
| 完成确认 | 打卡签退 | 外部系统回调 |
| 结算触发 | 企业手动结算 | 质检通过自动结算 |

### 状态流转

#### 任务包状态（jobs.status）
```
DRAFT → publish → PUBLISHED → close → CLOSED
PUBLISHED → expire → EXPIRED
```

#### 批次状态（job_schedules.status）
```
ACTIVE → CANCELLED（取消）
```

#### 报名单状态（schedule_applications.status）
```
PENDING → ACCEPTED / REJECTED
```

#### 任务单状态（annotation_task_orders.status）⭐ 核心
```
PENDING（待标注）
  → IN_PROGRESS（标注中）
  → SUBMITTED（已提交，外部系统回调）
  → COMPLETED（已完成，质检通过回调）→ 自动结算
  → REJECTED（质检不通过）→ 重新提交 → SUBMITTED
```

### 数据库设计

#### jobs 表扩展
```sql
ALTER TABLE jobs ADD COLUMN task_type VARCHAR(20) DEFAULT 'WORK' AFTER id;
ALTER TABLE jobs ADD COLUMN pricing_mode VARCHAR(20) DEFAULT NULL;
ALTER TABLE jobs ADD COLUMN price_per_unit DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE jobs ADD COLUMN total_items INT DEFAULT NULL;
ALTER TABLE jobs ADD COLUMN external_task_id VARCHAR(100) DEFAULT NULL;
ALTER TABLE jobs ADD COLUMN external_system_type VARCHAR(50) DEFAULT NULL;
ALTER TABLE jobs ADD COLUMN auto_settle TINYINT(1) DEFAULT 1;
```

#### job_schedules 表扩展
```sql
ALTER TABLE job_schedules ADD COLUMN total_items INT DEFAULT NULL;
ALTER TABLE job_schedules ADD COLUMN external_batch_id VARCHAR(100);
```

#### 新增 annotation_task_orders 表
```sql
CREATE TABLE annotation_task_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    items_completed INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    external_submission_id VARCHAR(100),
    submitted_at DATETIME,
    completed_at DATETIME,
    settled_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_application (application_id),
    UNIQUE KEY uk_external_submission (external_submission_id)
);
```

#### 新增 external_worker_mapping 表
```sql
CREATE TABLE external_worker_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    external_system_type VARCHAR(50) NOT NULL,
    external_worker_id VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_external (external_system_type, external_worker_id),
    UNIQUE KEY uk_system_worker (external_system_type, worker_id)
);
```

### API 设计

#### 企业端新增/修改接口
| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/enterprise/jobs` | POST | 创建标注任务包 |
| `/api/enterprise/jobs/update` | POST | 更新标注任务 |
| `/api/enterprise/jobs/publish` | POST | 发布 |
| `/api/enterprise/jobs/close` | POST | 关闭任务 |
| `/api/enterprise/jobs/schedules` | GET/POST | 批次管理 |
| `/api/enterprise/jobs/task-orders` | GET | 任务单列表 |
| `/api/enterprise/jobs/task-orders/review` | POST | 审核报名单 |
| `/api/enterprise/external-worker-mapping` | POST/GET | 人员映射管理 |

#### C端新增/修改接口
| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/worker/jobs` | GET | 搜索（支持task_type筛选） |
| `/api/worker/jobs/detail` | GET | 标注任务详情 |
| `/api/worker/jobs/apply` | POST | 抢批次 |
| `/api/worker/jobs/task-orders/my` | GET | 我的任务单列表 |

#### 外部系统回调接口
| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/external/annotation/submit` | POST | 标注完成回调 |
| `/api/external/annotation/quality-check` | POST | 质检结果回调 |
| `/api/external/annotation/progress` | POST | 进度回调（可选） |

### 风险与缓解

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| 外部系统回调延迟 | 任务单状态不更新 | 进度回调机制 + 超时自动关闭 |
| 回调API Key泄露 | 伪造回调 | 环境变量存储 + 定期轮换 |
| 人员映射不一致 | 回调找不到工人 | 唯一约束 + 映射管理界面 |
| 自动结算金额错误 | 工人收入异常 | 结算前校验 + 企业确认环节 |
