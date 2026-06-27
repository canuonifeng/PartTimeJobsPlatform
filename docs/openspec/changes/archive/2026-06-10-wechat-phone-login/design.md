## Context

当前 c-service 登录体系有 4 个入口：手机号验证码登录、微信简单登录（wechatCode）、微信完整登录（code→openId）、注册。其中微信 code 换 openId 是 mock 实现（`exchangeWechatCode` 直接返回 `"openid_" + code`），未对接真实微信 API。

工人端 UniApp 当前使用手机号验证码登录，微信一键登录尚未实现。

## Goals / Non-Goals

**Goals:**
- 实现微信小程序手机号授权登录（`wx.login()` + `wx.getPhoneNumber()` 组件）
- 后端解密手机号并自动保存到用户记录
- 手机号写入后不可修改
- 已有手机号的用户微信登录时绑定 openId（合并账号）
- 保留手机号+验证码登录作为备用

**Non-Goals:**
- 不对接真实微信 code2session API（保持 mock，后续单独对接）
- 不实现微信支付相关变更
- 不修改企业端/平台端登录逻辑

## Decisions

### 1. 微信手机号获取方式：`getPhoneNumber` 组件

前端使用 `<button open-type="getPhoneNumber">` 组件，用户点击后获取 `encryptedData` 和 `iv`，连同 `wx.login()` 的 code 一起发送到后端。后端使用 AES 解密获取手机号。

**替代方案：** 前端手动输入手机号 → 体验差，放弃。

### 2. 后端解密手机号：AES-128-CBC

微信提供的 `encryptedData` 使用 AES-128-CBC 解密，密钥为 `session_key`。由于当前 code2session 是 mock，解密也将使用模拟密钥。后续对接真实 API 时只需修改 `exchangeWechatCode` 方法。

### 3. 用户匹配策略

```
微信登录请求 (code + encryptedData + iv)
    │
    ├── 1. code 换 openId
    │
    ├── 2. 解密手机号
    │
    ├── 3. 按 openId 查找用户
    │       ├── 找到 → 更新手机号（如果为空）→ 签发 token
    │       └── 未找到 → 按手机号查找
    │               ├── 找到 → 绑定 openId → 签发 token
    │               └── 未找到 → 创建新用户 → 签发 token
    │
    └── 4. 返回 LoginVO
```

### 4. 手机号不可修改

在 `WorkerServiceImpl.updateProfile()` 中，如果请求包含手机号变更，直接忽略或抛异常。注册和微信登录是唯二写入手机号的入口。

### 5. 新增接口

```
POST /api/auth/wechat-phone-login
Request: { code, encryptedData, iv }
Response: { token, workerId, phone }
```

保留现有 `/api/auth/phone-login` 作为备用。

## Risks / Trade-offs

- **Mock 解密**：当前 code2session 未对接真实 API，手机号解密也是模拟的。→ 后续对接时只需修改 `exchangeWechatCode` 和解密逻辑，不影响接口契约。
- **手机号重复**：多个微信账号绑定同一手机号 → 合并为同一用户，符合业务需求。
- **session_key 有效期**：真实微信 session_key 有过期时间 → 当前 mock 不涉及，后续对接时需处理。

## Migration Plan

1. 后端：新增 `wechat-phone-login` 接口 + 修改 `updateProfile` 禁止改手机号
2. 前端：登录页添加微信一键登录按钮，调用新接口
3. 验证：微信登录 + 手机号登录 + 修改手机号拒绝

## Open Questions

- 微信真实 API 对接时机（本次保持 mock）
