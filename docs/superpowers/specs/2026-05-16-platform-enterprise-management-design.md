# 平台后台 — 企业管理模块设计

## 概述

在平台管理后台（platform-pc）中新增「企业管理」模块，支持对已审核通过的企业进行管理，以及管理各企业下的登录账号。

## 架构选择

统一在 platform-service 中管理企业与账号，所有服务共享同一个 MySQL 实例（part_time_work 库）。

## 数据库设计

### enterprises 表 — 已通过审核的企业

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| company_name | VARCHAR(200) NOT NULL | 企业名称 |
| contact_name | VARCHAR(100) | 联系人 |
| contact_phone | VARCHAR(20) | 联系电话 |
| company_address | VARCHAR(500) | 企业地址 |
| business_license | VARCHAR(500) | 营业执照 |
| status | VARCHAR(20) DEFAULT 'ACTIVE' | ACTIVE / SUSPENDED |
| registration_id | BIGINT | 关联 enterprise_registrations.id |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME ON UPDATE CURRENT_TIMESTAMP | |

### enterprise_accounts 表 — 企业端登录账号

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| enterprise_id | BIGINT NOT NULL | 关联 enterprises.id |
| username | VARCHAR(100) NOT NULL UNIQUE | 登录名 |
| password | VARCHAR(255) NOT NULL | BCrypt 加密 |
| display_name | VARCHAR(100) | 显示名 |
| role | VARCHAR(20) DEFAULT 'ADMIN' | ADMIN / HR / MANAGER / FINANCE |
| status | VARCHAR(20) DEFAULT 'ACTIVE' | ACTIVE / DISABLED |
| created_at | DATETIME DEFAULT CURRENT_TIMESTAMP | |
| updated_at | DATETIME ON UPDATE CURRENT_TIMESTAMP | |

## 后端 API（platform-service）

### 企业管理

| 方法 | 路径 | Body | 说明 |
|------|------|------|------|
| POST | /api/admin/enterprises/list | `{ "status": "ACTIVE" }` | 企业列表，status 可选 |
| POST | /api/admin/enterprises/detail | `{ "id": 1 }` | 企业详情 |
| POST | /api/admin/enterprises/update | `{ "id": 1, "companyName": "...", ... }` | 编辑企业信息 |
| POST | /api/admin/enterprises/suspend | `{ "id": 1 }` | 停用企业 |
| POST | /api/admin/enterprises/activate | `{ "id": 1 }` | 启用企业 |

### 企业账号管理

| 方法 | 路径 | Body | 说明 |
|------|------|------|------|
| POST | /api/admin/enterprises/accounts/list | `{ "enterpriseId": 1 }` | 企业下账号列表 |
| POST | /api/admin/enterprises/accounts/create | `{ "enterpriseId": 1, "username": "...", ... }` | 创建账号 |
| POST | /api/admin/accounts/update | `{ "id": 1, "displayName": "...", ... }` | 编辑账号 |
| POST | /api/admin/accounts/reset-password | `{ "id": 1, "newPassword": "..." }` | 重置密码 |
| POST | /api/admin/accounts/delete | `{ "id": 1 }` | 删除账号 |

## 额外改动

### 注册审核通过时自动创建企业记录

`RegistrationServiceImpl.approveRegistration()` 在状态改为 APPROVED 后，同步插入一条 enterprises 记录，携带 registration_id。

### 企业端认证改造

目前 enterprise-service 的 `SecurityConfig` 使用 `InMemoryUserDetailsManager` 硬编码 4 个用户。需要改为从 `enterprise_accounts` 表读取，实现 `UserDetailsService` 查询数据库进行认证。

## 前端页面（platform-pc）

### 新增侧边栏菜单项

在现有菜单的「职位分类」之下新增：
- **企业管理** → `/enterprises`
- 无独立菜单，账号管理以对话框形式嵌入

### 企业列表页（EnterpriseList.vue）

- `el-card` + `el-table` 展示企业列表
- 列：编号、企业名称、联系人、电话、状态（ACTIVE/SUSPENDED 标签）
- 操作：编辑 / 停用或启用 / 账号管理
- 编辑：`el-dialog` 表单
- 状态筛选：下拉选择

### 账号管理对话框

- 从企业列表行打开，传入 enterpriseId
- 内嵌 `el-table` 展示账号
- 列：用户名、显示名、角色、状态
- 操作：编辑 / 重置密码 / 删除
- 顶部「添加账号」按钮 → 新的 `el-dialog` 创建表单

### API 模块

新增 `src/api/enterprises.js`，导出命名函数，遵循现有 `request.js` 模式。
