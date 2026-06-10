## ADDED Requirements

### Requirement: 用户可获取自己的邀请链接
系统 SHALL 为每个已注册用户生成唯一的邀请链接，用户可通过 API 获取。

#### Scenario: 获取邀请链接
- **WHEN** 用户调用获取邀请链接接口
- **THEN** 系统返回格式为 `https://worker.example.com/invite?code=ABC123` 的链接

#### Scenario: 首次获取邀请码
- **WHEN** 用户首次调用获取邀请链接
- **THEN** 系统生成 8 位随机邀请码并存入 referral_code 表

#### Scenario: 重复获取邀请链接
- **WHEN** 用户已拥有邀请码，再次调用获取接口
- **THEN** 系统返回已存在的链接，不重复生成

### Requirement: 用户可生成邀请海报
系统 SHALL 为用户生成带二维码的邀请海报图片。

#### Scenario: 生成邀请海报
- **WHEN** 用户调用生成海报接口
- **THEN** 系统返回包含用户昵称、邀请二维码的海报图片 URL

#### Scenario: 海报包含用户信息
- **WHEN** 生成海报
- **THEN** 海报显示用户昵称和专属邀请二维码

### Requirement: 邀请码格式校验
系统 SHALL 验证邀请码格式为 8 位字母数字组合。

#### Scenario: 有效邀请码
- **WHEN** 输入 8 位字母数字组合（如 A1B2C3D4）
- **THEN** 格式校验通过

#### Scenario: 无效邀请码
- **WHEN** 输入长度不是 8 位或包含特殊字符
- **THEN** 格式校验失败，返回错误提示
