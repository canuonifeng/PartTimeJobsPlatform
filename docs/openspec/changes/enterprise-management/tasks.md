## 企业管理模块

对应 spec: `openspec/specs/platform-operations/spec.md` — 需求5~需求8

### 需求5：企业管理（后端）

- [x] 5.1 创建 Flyway 迁移 V6__create_enterprise_tables.sql（enterprises + enterprise_accounts 表）
- [x] 5.2 追加 enterprises / enterprise_accounts 种子数据到 seed-data.sql
- [x] 5.3 创建 Enterprise / EnterpriseAccount 实体类
- [x] 5.4 创建 EnterpriseUpdateCmd、EnterpriseAccountCreateCmd、EnterpriseAccountUpdateCmd、ResetPasswordCmd
- [x] 5.5 创建 EnterpriseVO、EnterpriseAccountVO
- [x] 5.6 创建 EnterpriseMapper（Java + XML）—— 列表查询（分页+状态筛选）、根据ID查询、更新、更新状态
- [x] 5.7 创建 EnterpriseAccountMapper（Java + XML）—— 按企业ID查询列表、根据ID查询、新增、更新、删除
- [x] 5.8 创建 EnterpriseService（interface + impl）—— 列表、详情、编辑、停用、启用
- [x] 5.9 创建 EnterpriseAccountService（interface + impl）—— 列表、新增、编辑、重置密码、删除
- [x] 5.10 创建 EnterpriseController —— list / detail / update / suspend / activate（5个 POST 端点）
- [x] 5.11 创建 EnterpriseAccountController —— list / create / update / resetPassword / delete（5个 POST 端点）

### 需求6：企业账号管理（前端）

- [x] 6.1 创建 platform-pc/src/api/enterprises.js（10 个 API 函数：企业 + 账号）
- [x] 6.2 创建 EnterpriseList.vue —— 企业表格列表 + 搜索/状态筛选 + 编辑/停用/启用对话框 + 账号管理对话框（CRUD + 重置密码）
- [x] 6.3 修改 router/index.js —— 注册企业列表路由
- [x] 6.4 修改 App.vue —— 添加"企业管理"菜单项到侧边栏

### 需求7：注册审核自动创建企业

- [x] 7.1 RegistrationServiceImpl 注入 EnterpriseMapper
- [x] 7.2 审核通过时自动插入 enterprise 记录（状态 ACTIVE，关联 registration_id）

### 需求8：企业端 DB 认证

- [x] 8.1 enterprise-service 创建 EnterpriseAccount 实体类
- [x] 8.2 创建 EnterpriseAccountMapper（Java + XML）
- [x] 8.3 创建 EnterpriseUserDetailsService —— 从 enterprise_accounts 表查询 + admin 账号 fallback
- [x] 8.4 修改 SecurityConfig —— 注入 DataSource，改用 DB 认证

### 编译验证

- [x] 9.1 platform-service 编译通过
- [x] 9.2 enterprise-service 编译通过

### 数据库

- [x] 10.1 enterprises / enterprise_accounts 表已建（手动执行 V6 migration SQL）
- [x] 10.2 种子数据已导入，/api/admin/enterprises/list 接口验证通过
