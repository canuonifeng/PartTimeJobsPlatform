# OSS Bucket 规划

零工平台对象存储统一使用阿里云 OSS，采用**双 bucket** 模型：私有 bucket 存实名与资质材料，公有 bucket 存可公开访问的业务素材。

## 一、全局约定

| 项 | 约定值 | 说明 |
|---|---|---|
| Region | `oss-cn-hangzhou` | 杭州地域，全平台统一，不按业务拆分 |
| Endpoint | `oss-cn-hangzhou.aliyuncs.com` | 外网 endpoint，前端直传与后端签名均使用此值 |
| 私有 bucket | `parttime-private` | 默认私有，拒绝匿名读 |
| 公有 bucket | `parttime-public` | 公共读，允许匿名 GET |

bucket 访问域名：

- 私有：`https://parttime-private.oss-cn-hangzhou.aliyuncs.com`
- 公有：`https://parttime-public.oss-cn-hangzhou.aliyuncs.com`

> 注意：以上 bucket 名为规划值，创建时若与账号下已有 bucket 重名，可加环境后缀（如 `parttime-private-prod`），但必须同步修改三服务 `oss.private-bucket` / `oss.public-bucket` 配置。

## 二、私有 bucket：parttime-private

**用途**：存储敏感个人/企业资质材料，**禁止匿名读**。所有访问必须由后端签发 5–15 分钟短期签名 URL。

**Bucket ACL**：`private`（私有）。同时开启 OSS 控制台的"阻止公共访问"（Block Public Access），确保不会被误配为公共读。

**对象前缀规划**（key 由后端生成，前端不可自定）：

| 前缀 | 内容 | 上传方 | 读取方式 |
|---|---|---|---|
| `realname/{workerId}/idcard-front/` | 工人身份证正面 | 工人本人（STS 直传） | 后端签名 URL |
| `realname/{workerId}/idcard-back/` | 工人身份证反面 | 工人本人（STS 直传） | 后端签名 URL |
| `license/{enterpriseId}/` | 企业营业执照 | 企业管理员（STS 直传） | 后端签名 URL |

示例 key：

```
realname/123/idcard-front/20260924-a1b2c3.jpg
realname/123/idcard-back/20260924-d4e5f6.jpg
license/456/20260924-license.jpg
```

**权限矩阵**（读取时由后端校验，见 design.md REQ-4）：

- 工人实名资料：本人、与该工人存在报名/排班关联的企业审核人员、平台管理员。
- 企业资质：企业本人、平台管理员。
- 其余一律 403。

## 三、公有 bucket：parttime-public

**用途**：存储可公开访问的业务素材，允许匿名 GET，前端直接拼接公开 URL 展示。

**Bucket ACL**：`public-read`（公共读）。**不要**设置为 public-read-write，写入只走 STS 临时凭证。

**对象前缀规划**：

| 前缀 | 内容 | 上传方 |
|---|---|---|
| `job/{companyId}/yyyyMMdd/` | 职位图片 | 企业管理员（STS 直传） |
| `logo/{enterpriseId}/` | 企业 Logo | 企业管理员（STS 直传） |
| `avatar/{workerId}/yyyyMMdd/` | 工人头像 | 工人本人（STS 直传） |
| `training/yyyyMMdd/` | 培训课件（mp4/pdf 等） | 平台管理员（STS 直传） |
| `material/yyyyMMdd/` | 运营素材（banner、推广图等） | 平台管理员（STS 直传） |

示例 key 与公开 URL：

```
key:  job/789/20260924/cover.jpg
url:  https://parttime-public.oss-cn-hangzhou.aliyuncs.com/job/789/20260924/cover.jpg
```

## 四、数据库存储约定

- 公有文件：业务表存**完整公开 URL**（`https://parttime-public.oss-cn-hangzhou.aliyuncs.com/{key}`）。
- 私有文件：业务表只存**对象 key**（如 `realname/123/idcard-front/xxx.jpg`），读取时由后端实时转签名 URL，不落库。
- 兼容旧数据：读取时若字段为 `/uploads/...` 旧本地路径，dev 环境直接透出；生产环境旧数据需迁移脚本处理。

## 五、生命周期与清理（建议）

- `realname/`、`license/` 前缀：实名认证通过后长期保留，不设置自动删除。
- 上传失败残留的临时分片：OSS 默认 7 天自动清理未完成的 Multipart，无需额外配置。
- 公有 bucket 素材：如后续需要冷热分层，再单独配置 Lifecycle Rule，本阶段不配置。
