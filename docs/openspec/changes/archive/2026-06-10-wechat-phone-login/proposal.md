## Why

当前工人端登录方式以手机号验证码和微信简单登录为主，微信一键登录（获取手机号）尚未实现。用户需要手动输入手机号，体验不够流畅。目标是优先使用微信一键登录，用户授权后自动获取并保存手机号，同时手机号不可修改，也可作为备用登录方式。

## What Changes

- 新增微信手机号授权登录接口：前端传递微信 code + 加密手机号数据，后端解密并保存
- 微信登录时自动获取手机号并保存到用户记录
- 手机号设置后不可修改（注册/微信登录时写入，之后禁止更新）
- 已有手机号的用户微信登录时，绑定 openId 到现有账号（合并用户）
- 保留手机号+验证码登录作为备用方式
- 前端工人端适配微信一键登录流程

## Capabilities

### New Capabilities
- `wechat-phone-login`: 微信手机号授权登录，包括 code 换 openId、手机号解密、用户创建/绑定、token 签发

### Modified Capabilities
- `worker-profile`: 手机号字段增加不可修改约束

## Impact

- c-service: AuthController 新增接口、WorkerServiceImpl 新增/修改登录逻辑、WorkerMapper 增加绑定 openId 方法
- worker-uniapp: 登录页面重构，优先展示微信一键登录按钮
- 数据库: 无表结构变更（phone 字段已存在）
- 前端接口: 新增微信手机号登录端点
