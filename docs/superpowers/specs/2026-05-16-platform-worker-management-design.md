# 平台后台 — 兼职管理模块

## 背景

平台运营人员需要在后台查看、管理已注册的兼职用户（Worker）。目前平台端完全没有兼职管理能力，兼职数据存储在 c-service 的 `c_worker` 表中。

由于 platform-service 和 c-service 共享同一 MySQL 实例但有不同 schema，采用跨 schema 直连方案（与 enterprise-service 的 CWorkerMapper 做法一致）。

## 功能范围

- 查看兼职列表（分页、按状态/关键词筛选）
- 查看兼职详情（基础信息 + 个人资料）
- 编辑兼职信息（姓名、电话）
- 封禁/启用兼职账号（修改 c_worker.status）

## API 设计（platform-service，POST + JSON body）

| 端点 | 说明 | 请求体 |
|------|------|--------|
| `/api/admin/workers/list` | 兼职列表 | `{ status?, keyword? }` |
| `/api/admin/workers/detail` | 兼职详情 | `{ id }` |
| `/api/admin/workers/update` | 编辑兼职 | `{ id, name, phone }` |
| `/api/admin/workers/ban` | 封禁 | `{ id }` |
| `/api/admin/workers/unban` | 解封 | `{ id }` |

## 数据来源

- `c_worker` 表（c_service schema）—— 核心身份信息
- `worker_profiles` 表（c_service schema）—— 技能、可用时间等扩展信息
- 不需要新增 Flyway 迁移（读已有表）

## 前端

- 新建 `WorkerList.vue`（复用 EnterpriseList 的 el-card + el-table + el-dialog 模式）
- 添加路由 `/admin/workers`
- 侧边栏添加"兼职管理"菜单项

## 涉及修改

| 文件 | 变更 |
|------|------|
| `platform-service/.../pojo/vo/WorkerVO.java` | 新增 |
| `platform-service/.../pojo/cmd/WorkerUpdateCmd.java` | 新增 |
| `platform-service/.../mapper/WorkerMapper.java` | 新增，跨 schema 查询 |
| `platform-service/.../mapper/WorkerMapper.xml` | 新增 |
| `platform-service/.../service/WorkerService.java` | 新增 |
| `platform-service/.../service/impl/WorkerServiceImpl.java` | 新增 |
| `platform-service/.../controller/WorkerController.java` | 新增 |
| `platform-pc/src/api/workers.js` | 新增 |
| `platform-pc/src/views/workers/WorkerList.vue` | 新增 |
| `platform-pc/src/router/index.js` | 添加路由 |
| `platform-pc/src/App.vue` | 添加菜单 |
