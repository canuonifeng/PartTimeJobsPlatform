# OSS 部署说明（双 Bucket + STS RAM 角色）

本目录是零工平台 OSS 存储改造（OpenSpec change: `oss-storage-refactor`）的基础设施部署文档。
按本说明操作后，你将拿到后端三服务所需的 `RoleArn` 与 bucket 配置值。

## 文件清单

| 文件 | 内容 |
|---|---|
| `buckets.md` | 双 bucket 规划：私有/公有 bucket 名、Region、endpoint、对象前缀、ACL 约定 |
| `ram-policy.json` | STS AssumeRole 用的权限策略模板（前缀粒度，禁止整 bucket 越权） |
| `README.md` | 本文件：从零到拿到 RoleArn 的完整部署步骤 |

## 前置条件

- 已有阿里云账号，且账号下已完成企业实名认证。
- 已开通对象存储 OSS 服务与访问控制 RAM 服务。
- 后端调用 STS 所用的长期 RAM 用户 AK/SK（在 `oss.properties` / 环境变量中配置）需已具备 `sts:AssumeRole` 权限——这是长期凭证侧的配置，不在本目录策略模板内。

## 约定值（全平台统一）

| 配置 | 值 |
|---|---|
| Region | `oss-cn-hangzhou` |
| Endpoint | `oss-cn-hangzhou.aliyuncs.com` |
| 私有 bucket | `parttime-private` |
| 公有 bucket | `parttime-public` |

> 文档中所有 `${OSS_ROLE_ARN}`、`${OSS_ACCOUNT_ID}` 均为占位符，部署时替换为控制台真实值；仓库内不得提交真实账号 ID。

## 部署步骤

### 第 1 步：创建两个 bucket

1. 登录 OSS 控制台，确认地域为**华东 2（杭州）**（即 `oss-cn-hangzhou`）。
2. 创建 bucket `parttime-private`：
   - 读写 ACL 选择**私有**。
   - 开启"阻止公共访问"（Block Public Access），防止误配成公共读。
3. 创建 bucket `parttime-public`：
   - 读写 ACL 选择**公共读**（匿名可 GET，写入只走 STS）。
   - **不要**选公共读写。
4. 其余选项（版本控制、日志、加密）本阶段默认即可，按 `buckets.md` 第五节后续再配。

### 第 2 步：创建 RAM 角色（供 STS AssumeRole 扮演）

1. 进入 RAM 控制台 → **身份管理 → 角色 → 创建角色**。
2. 信任实体类型选择**阿里云账号**（用于后端服务用长期 AK 调 AssumeRole 扮演此角色）。
3. 角色名建议：`parttime-oss-sts-role`。
4. 信任策略保持默认（信任当前账号）即可，完成创建。

### 第 3 步：为角色附加权限策略

1. 进入刚创建的角色详情 → **权限管理 → 新增授权**。
2. 选择**自定义权限策略 → 创建自定义权限策略**。
3. 将本目录 `ram-policy.json` 的内容完整粘贴进策略文档（JSON 标签页）。
4. 策略名称建议：`ParttimeOssStsUploadPolicy`。
5. 回到角色详情，把该自定义策略授权给 `parttime-oss-sts-role`。

策略要点（见 `ram-policy.json`）：

- 写权限精确到对象前缀：
  - 私有 bucket：`realname/*`、`license/*`
  - 公有 bucket：`job/*`、`logo/*`、`avatar/*`、`training/*`、`material/*`
- 只包含写相关 Action（PutObject 及分片上传系列），**不包含** `oss:*`、`oss:GetObject`、`oss:ListBucket`，也不对整 bucket 做 `PutObject` 通配。
- 每次 AssumeRole 时，后端还会再传入一条**会话级内联策略**，把前缀进一步收敛到本次登录身份（如 `realname/123/`），做到工人只能写自己的目录。

### 第 4 步：复制 RoleArn

1. 在 RAM 角色详情页，复制**角色 ARN**，形如：

   ```
   acs:ram::${OSS_ACCOUNT_ID}:role/parttime-oss-sts-role
   ```

2. 此值即后端配置中的 `oss.sts-role-arn`。

### 第 5 步：后端配置注入

将以下值通过环境变量注入三服务（`c-service` / `enterprise-service` / `platform-service`），**不要**写进仓库：

| 环境变量 | 示例值 |
|---|---|
| `OSS_ENDPOINT` | `oss-cn-hangzhou.aliyuncs.com` |
| `OSS_PRIVATE_BUCKET` | `parttime-private` |
| `OSS_PUBLIC_BUCKET` | `parttime-public` |
| `OSS_STS_ROLE_ARN` | `acs:ram::${OSS_ACCOUNT_ID}:role/parttime-oss-sts-role` |
| `OSS_ACCESS_KEY_ID` | 后端长期 RAM 用户 AK（保密） |
| `OSS_ACCESS_KEY_SECRET` | 后端长期 RAM 用户 SK（保密） |

## 验收清单

- [ ] 两个 bucket 均在 `oss-cn-hangzhou` 创建，私有/公有 ACL 正确。
- [ ] `parttime-private` 已开启阻止公共访问。
- [ ] RAM 角色 `parttime-oss-sts-role` 已创建，并附加 `ParttimeOssStsUploadPolicy`。
- [ ] 策略内无 `oss:*`、无整 bucket 的 `PutObject` 通配，全部精确到前缀。
- [ ] RoleArn 已复制并配置到后端环境变量。
- [ ] 仓库内文档只出现占位符，无真实 AK/SK/账号 ID。

## 安全红线

- 严禁把长期 AK/SK 或真实 RoleArn 提交进仓库。
- 私有 bucket 任何时候都不要开放公共写；前端写私有 bucket 一律走 STS 临时凭证。
- STS 会话有效期建议 15 分钟，后端按登录身份动态收敛前缀，不向下游暴露越权目录。
