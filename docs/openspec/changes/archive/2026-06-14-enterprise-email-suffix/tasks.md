## 1. 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本：`enterprises` 表新增 `email_suffix` VARCHAR(100) UNIQUE DEFAULT NULL
- [x] 1.2 执行本地数据库验证字段生效

## 2. enterprise-service 后端

- [x] 2.1 `Enterprise` 实体新增 `emailSuffix` 字段（c-service）
- [x] 2.2 `EnterpriseMapper.xml` 补充 `email_suffix` 字段映射（SELECT/INSERT）
- [x] 2.3 `EnterpriseMapper` 新增 `findByEmailSuffix(String suffix)` 方法（c-service）
- [x] 2.4 `EnterpriseMapper` 新增 `findIdByEmailSuffix(String suffix)` 方法（enterprise-service）
- [x] 2.5 `EnterpriseUserDetailsService.loadUserByUsername()` 扩展：解析 `前缀@后缀` 格式，验证后缀匹配企业，用前缀查询账号
- [x] 2.6 编写单元测试（邮箱登录 + 纯用户名拒绝 + 后缀不匹配 + 后缀唯一性）

## 3. platform-service 后端

- [x] 3.1 `EnterpriseMapper`（platform）新增 `updateEmailSuffix(Long enterpriseId, String suffix)` 方法
- [x] 3.2 平台企业管理接口支持修改邮箱后缀（通过 EnterpriseUpdateCmd）

## 4. enterprise-uniapp 前端

- [x] 4.1 登录页支持 `前缀@后缀` 格式输入（单输入框，placeholder 提示）

## 5. enterprise-pc 前端

- [x] 5.1 登录页支持 `前缀@后缀` 格式输入（单输入框，placeholder 提示）

## 6. platform-pc 前端

- [x] 6.1 企业管理列表新增"账号后缀"列
- [x] 6.2 新增/编辑企业对话框新增"账号后缀"字段

## 7. 验证

- [x] 7.1 enterprise-service 全部测试通过
- [x] 7.2 platform-service 全部测试通过
- [x] 7.3 platform-pc 构建通过
