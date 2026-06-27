## ADDED Requirements

### Requirement: 运营平台可配置企业账号后缀
系统 SHALL 允许运营平台为企业设置专属账号后缀。

#### Scenario: 运营平台设置企业后缀
- **WHEN** 运营人员在企业管理页面输入账号后缀（如 `acme`）并保存
- **THEN** 系统 SHALL 将后缀存储到 `enterprises.email_suffix` 字段

#### Scenario: 后缀唯一性校验
- **WHEN** 运营人员提交的账号后缀已被其他企业使用
- **THEN** 系统 SHALL 拒绝保存并返回错误

#### Scenario: 新增企业时设置后缀
- **WHEN** 运营人员新增企业时填写账号后缀
- **THEN** 系统 SHALL 在创建企业时同时保存后缀

### Requirement: 企业端必须使用后缀格式登录
系统 SHALL 要求企业端登录时使用 `前缀@后缀` 格式的用户名。

#### Scenario: 使用正确格式登录
- **WHEN** 用户输入 `admin@acme` 和密码进行登录
- **THEN** 系统 SHALL 解析后缀 `acme`，找到对应企业，使用 `admin` 作为用户名验证账号密码

#### Scenario: 不带后缀登录被拒绝
- **WHEN** 用户输入不包含 `@` 的纯用户名进行登录
- **THEN** 系统 SHALL 返回"用户名格式不正确，须使用 前缀@后缀 格式"

#### Scenario: 后缀不匹配任何企业
- **WHEN** 用户输入的后缀在 `enterprises` 表中不存在
- **THEN** 系统 SHALL 返回"账号后缀不正确"

#### Scenario: 账号不属于该企业
- **WHEN** 用户输入的前缀对应的账号不属于后缀匹配的企业
- **THEN** 系统 SHALL 返回"账号不属于该企业"
