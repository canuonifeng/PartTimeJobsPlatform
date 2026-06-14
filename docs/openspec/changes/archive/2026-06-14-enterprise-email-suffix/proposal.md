## Why

企业端当前使用纯用户名登录（如 `admin`），缺乏企业标识度，且难以区分不同企业的账号。采用邮箱格式（`xxx@yyy`）的登录方式可以：
- 提升企业品牌识别度（使用企业专属后缀）
- 为未来多租户邮箱通知、账号体系扩展奠定基础
- 符合企业用户的常见登录习惯

## What Changes

- `enterprises` 表新增 `email_suffix` 字段，存储企业专属邮箱后缀（如 `acme.com`）
- 企业端登录支持邮箱格式：用户输入 `xxx@yyy`，系统验证后缀匹配企业，`xxx` 作为用户名登录
- 企业端设置页新增"邮箱后缀"配置项，企业可自行修改
- 平台管理端企业管理详情新增"邮箱后缀"字段，运营可修改
- 登录接口兼容：同时支持纯用户名和邮箱格式登录

## Capabilities

### New Capabilities

- `enterprise-email-suffix`: 企业邮箱后缀配置与登录支持

### Modified Capabilities

- `enterprise-account`: 登录流程扩展，支持邮箱格式用户名解析

## Impact

**后端（enterprise-service）：**
- `enterprises` 表新增 `email_suffix` VARCHAR(100) 字段
- `Enterprise` 实体补充 `emailSuffix` 字段
- `EnterpriseMapper.xml` 补充字段映射
- `EnterpriseUserDetailsService` 登录逻辑扩展：解析邮箱格式，验证后缀匹配
- 新增 `EnterpriseService.getEmailSuffix()` / `updateEmailSuffix()` 方法
- `EnterpriseController` 新增获取/修改邮箱后缀接口

**后端（platform-service）：**
- `EnterpriseMapper` 新增 `updateEmailSuffix()` 方法
- 平台企业管理接口支持修改邮箱后缀

**前端（enterprise-uniapp）：**
- 设置页新增"邮箱后缀"配置项
- 登录页支持邮箱格式输入（可选拆分用户名+后缀输入框）

**前端（enterprise-pc）：**
- 企业设置/详情页新增"邮箱后缀"字段

**数据库：**
- Flyway 迁移脚本：`enterprises` 表新增 `email_suffix` 字段
