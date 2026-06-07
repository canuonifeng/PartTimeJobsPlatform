## Why

兼职市场规模持续增长，但中小企业在兼职招聘和管理上缺乏高效工具。目前市场上缺少一套集岗位发布、排班管理、薪资结算、平台运营于一体的 SaaS 方案。本项目旨在构建完整的兼职 SaaS 管理平台，服务企业端、C 端用户和平台运营三方。

## What Changes

- 企业端后台系统（PC + 小程序）：岗位发布、排班管理、薪资结算、兼职管理
- C 端小程序：找活报名、打卡签到、收入提现、个人中心
- 平台管理系统（PC）：企业开户审核、岗位内容审核、数据看板、系统配置
- 底层共享内核：认证鉴权、消息通知、文件存储、位置服务

## Capabilities

### New Capabilities
- `job-management`: 岗位发布与管理（CRUD、薪资规则、应聘管理）
- `schedule-attendance`: 排班考勤（排班模板、打卡签到、考勤报表）
- `payroll`: 薪资结算（工资核算、发薪、提现）
- `worker-profile`: 兼职档案（个人资料、简历、评价、黑名单）
- `enterprise-account`: 企业账户（注册、团队管理、角色权限、资质）
- `worker-user`: C 端用户认证（微信登录、找工作、收入）
- `platform-operations`: 平台运营（开户审核、内容审核、数据、配置）
- `notification`: 消息通知（站内、微信模板、短信）

### Modified Capabilities

无

## Impact

- 后端：新建 2 套 Spring Boot 服务（企业端 + C 端），1 套平台管理服务
- 前端：企业 PC 端 Vue 3 + Element Plus，企业小程序 UniApp，C 端小程序 UniApp，平台管理端 Vue 3 + Element Plus
- 数据库：MySQL 共享实例，Redis 缓存，RocketMQ 消息，XXL-Job 定时任务
- 基础设施：微信小程序云开发 / 企业资质认证 / 支付网关接入
