## ADDED Requirements

### Requirement: 微信手机号授权登录

系统 SHALL 提供微信小程序手机号授权登录接口，接收前端传递的微信 code、加密手机号数据（encryptedData 和 iv），完成以下流程：
1. 使用 code 换取 openId
2. 解密获取手机号
3. 根据 openId 或手机号匹配现有用户，或创建新用户
4. 签发 JWT token 返回

#### Scenario: 新用户微信手机号登录

- **WHEN** 前端发送 POST /api/auth/wechat-phone-login，携带 code、encryptedData、iv，且该 openId 和手机号均不存在
- **THEN** 系统创建新用户（phone = 解密手机号，openId = code 换取的 openId），返回 token 和 workerId

#### Scenario: 已有 openId 的用户再次登录

- **WHEN** 前端发送微信手机号登录请求，且该 openId 已存在
- **THEN** 系统找到该用户，如果用户手机号为空则更新手机号，返回该用户的 token

#### Scenario: 手机号已存在但 openId 不同

- **WHEN** 前端发送微信手机号登录请求，openId 不存在但手机号已存在
- **THEN** 系统将 openId 绑定到该手机号对应的现有用户，返回该用户的 token

#### Scenario: 微信登录参数缺失

- **WHEN** 前端发送请求时 code、encryptedData、iv 任一为空
- **THEN** 系统返回错误，提示参数不完整

### Requirement: 微信登录响应包含手机号

微信手机号登录成功后，响应 SHALL 包含手机号信息。

#### Scenario: 登录成功响应

- **WHEN** 微信手机号登录成功
- **THEN** 返回 LoginVO 包含 token、workerId、phone 字段
