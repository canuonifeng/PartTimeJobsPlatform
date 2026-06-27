## Context

当前企业端登录使用 `enterprise_accounts` 表的 `username` 字段进行身份验证，登录格式为纯用户名（如 `admin`）。企业希望使用带后缀的格式登录（如 `admin@acme`），其中 `acme` 为企业专属后缀，由运营平台统一配置。

现有登录流程：
```
前端输入 username + password
  → POST /api/auth/login
  → EnterpriseUserDetailsService.loadUserByUsername(username)
  → EnterpriseAccountMapper.findByUsername(username)
  → BCrypt 比对密码
  → 生成 JWT
```

## Goals / Non-Goals

**Goals:**
- 运营平台可为企业设置账号后缀
- 登录必须使用 `前缀@后缀` 格式，系统验证后缀匹配企业，前缀查询账号
- 后缀不一定是邮箱格式，纯标识符即可

**Non-Goals:**
- 不涉及企业邮箱收发功能
- 不涉及邮箱验证/激活流程
- 企业端不能自行修改后缀

## Decisions

### 1. 后缀存储位置：`enterprises` 表

**选择**: 在 `enterprises` 表新增 `email_suffix` 字段

**理由**: 后缀是企业级属性，同一企业的所有账号共享同一后缀。存储在企业表而非账号表，避免数据冗余和不一致。

### 2. 登录解析策略

**选择**: 在 `EnterpriseUserDetailsService.loadUserByUsername()` 中解析 `前缀@后缀` 格式

**流程**:
```
输入: username (必须包含 @)
  → 检测是否包含 @，不包含则拒绝
  → 提取 localPart 和 suffix
  → EnterpriseMapper.findIdByEmailSuffix(suffix)
  → 验证后缀匹配企业
  → 使用 localPart 作为用户名查询账号
  → 验证账号属于该企业
```

**理由**: 在 Service 层解析，Controller 层无需修改，保持登录接口签名不变。

### 3. 唯一性约束

**选择**: `email_suffix` 字段加 UNIQUE 约束

**理由**: 后缀是企业标识，不能重复。若企业 A 使用 `acme`，企业 B 不能再使用。

### 4. 企业端不可修改

**选择**: 企业端（uniapp/pc）不提供后缀修改入口，仅运营平台可设置

**理由**: 后缀是平台分配的企业标识，企业不应自行修改，避免混乱。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 后缀修改后，旧格式登录失效 | 修改后缀时通知管理员 |
| 后缀冲突 | 数据库 UNIQUE 约束 + Service 层校验 |
| 不带后缀登录 | 登录逻辑强制要求 `@` 格式 |

## Migration Plan

1. Flyway 迁移脚本：`enterprises` 表新增 `email_suffix` VARCHAR(100) UNIQUE DEFAULT NULL
2. 现有企业后缀为 NULL，运营平台需为每个企业设置后缀后才能登录
3. 新创建的企业必须设置后缀
