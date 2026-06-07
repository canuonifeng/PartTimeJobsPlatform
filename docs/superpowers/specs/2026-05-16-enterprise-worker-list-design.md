# 企业端 — 人才库（兼职列表）

## 背景

企业需要在后台查看与自己有关联的兼职人员列表，包括报名过岗位和排过班的兼职。

## 方案

新建 `company_workers` 表作为人才库，在报名、录用、排班时自动入库。

## 入库时机

| 时机 | 所在服务 | 方法 |
|------|----------|------|
| 工人报名岗位 | c-service | JobServiceImpl.applyForJob() |
| 企业录用工人 | enterprise-service | ApplicationServiceImpl.acceptApplication() |
| 企业分配排班 | enterprise-service | ScheduleServiceImpl.assignShift() |

## 表结构

```sql
CREATE TABLE company_workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    first_contact_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_contact_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_company_worker (company_id, worker_id),
    INDEX idx_company_id (company_id)
);
```

## API

`POST /api/workers/list` — 企业查看自己的兼职列表
- companyId 从 JWT 取
- 可选参数：`keyword`（搜索名称/电话）
- 返回：workerId, name, phone, avatarUrl, status, firstContactAt, lastContactAt

## 涉及修改

| 文件 | 变更 |
|------|------|
| `enterprise-service/.../db/migration/V7__create_company_workers.sql` | 新建 |
| `enterprise-service/.../entity/CompanyWorker.java` | 新建 |
| `enterprise-service/.../mapper/CompanyWorkerMapper.java + .xml` | 新建 |
| `enterprise-service/.../service/CompanyWorkerService.java + impl` | 新建 |
| `enterprise-service/.../controller/CompanyWorkerController.java` | 新建 |
| `enterprise-service/.../service/impl/ApplicationServiceImpl.java` | 加 hook |
| `enterprise-service/.../service/impl/ScheduleServiceImpl.java` | 加 hook |
| `c-service/.../mapper/CompanyWorkerMapper.xml` | 新建（insert DUPLICATE） |
| `c-service/.../service/impl/JobServiceImpl.java` | 加 hook |
| `enterprise-pc/src/api/worker.js` | 新建 |
| `enterprise-pc/src/views/workers/WorkerList.vue` | 新建 |
| `enterprise-pc/src/router/index.js` | 添加路由 |
| `enterprise-pc/src/App.vue` | 添加菜单 |
