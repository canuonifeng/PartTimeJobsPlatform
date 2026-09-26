# 上传与 OSS 存储设计

> 本文件是零工平台文件上传与存储的权威设计说明，对应 OpenSpec 变更 `oss-storage-refactor`（22 项任务已全部完成）。

## 1. 总体架构

- **双 bucket 隔离**：私有 bucket 存敏感文件（实名认证、企业资质），公有 bucket 存公开可访问文件。
- **上传链路**：前端直传（STS 临时凭证），后端不下发真实 AK/SK、不中转文件。
- **读取链路**：私有文件由后端按权限矩阵签发**签名 URL**（默认 10 分钟过期）；公有文件直接使用完整公开 URL。
- **历史数据**：历史本地 `/uploads/` 文件本次**不迁移**，各服务 WebConfig 静态映射保留，读取侧原样透出，兼容展示。

## 2. Bucket 划分与对象前缀

| Bucket | 业务 | 对象 key 前缀 | 可访问范围 |
|---|---|---|---|
| 私有 | 工人实名认证（身份证正/反面） | `realname/{workerId}/...` | 本人、关联企业审核人员、平台管理员 |
| 私有 | 企业资质（营业执照） | `license/{enterpriseId}/...` | 企业本人、平台管理员 |
| 公有 | 职位图片 | `job/{companyId}/...` | 公开 |
| 公有 | 企业 Logo | `logo/...` | 公开 |
| 公有 | 工人头像 | `avatar/{workerId}/...` | 公开 |
| 公有 | 培训课件（视频/音频/文档/图文） | `training/...` | 公开（工人端需观看） |
| 公有 | 运营素材 | `material/...` | 公开 |

> 前缀由后端 STS 接口按 `biz` 参数下发，前端直传时拼到 key 上；私有 bucket 的 key 落库存储，公有 bucket 存完整公开 URL。

## 3. 权限矩阵（私有文件签名 URL）

| 访问者 | 工人实名 `realname/{id}/` | 企业资质 `license/{id}/` |
|---|---|---|
| 文件本人 | ✅（workerId 匹配） | ✅（enterpriseId 匹配） |
| 关联企业审核人员 | ✅（报名/排班关联校验） | —（无关联场景） |
| 平台管理员 | ✅ | ✅ |
| 其他 | 403 | 403 |

实现位置：
- `c-service`：`OssSignedUrlServiceImpl`（本人 workerId 匹配）
- `enterprise-service`：`OssSignedUrlServiceImpl`（企业本人；企业审核人员经 `scheduleApplicationMapper.countCompanyWorkerRelation` 校验与工人存在报名/排班关联）
- `platform-service`：`OssSignedUrlServiceImpl`（登录管理员）

未授权访问统一返回 403；key 非法（`..`、`\`、空白、前缀不符、非数字归属）返回 400。

## 4. 接口定义

### 4.1 STS 直传凭证（三服务各一份）

```
POST /api/worker/files/sts?biz=realname|avatar|job      # c-service
POST /api/enterprise/files/sts?biz=license|logo|job      # enterprise-service
POST /api/admin/files/sts?biz=training|material          # platform-service
```

- `biz` 走 query 参数（前端 crypto-js 签名依赖，与 c-service/platform-service 的 FileStsController 契约一致；enterprise 标准测试已对此豁免）。
- 响应：`{ endpoint, region, accessKeyId, accessKeySecret, securityToken, bucket, prefix, expiresAt }`。
- 后端按 biz 映射 bucket 与允许前缀，未配置 OSS 时返回 503。

### 4.2 私有文件签名 URL（三服务各一份）

```
GET /api/worker/files/signed-url?key=realname/123/xx.jpg
GET /api/enterprise/files/signed-url?key=license/9/yy.jpg
GET /api/admin/files/signed-url?key=realname/123/xx.jpg
```

- 按 §3 权限矩阵校验，成功后返回 `{ url, expiresAt }`（默认 600 秒过期）。

### 4.3 读取接口的 URL 语义

- **工人端实名状态** `GET /api/worker/real-name/status`：`idCardFrontUrl/idCardBackUrl` 若为 `realname/` 前缀 key，返回签名 URL；否则原样透出（兼容旧 `/uploads/`）。
- **企业端资质状态**：`businessLicenseUrl` 若为 `license/` 前缀 key，返回签名 URL。
- **平台审核列表**（`/api/admin/worker-real-name`、`/api/admin/enterprise-real-name`）：同前缀规则转签名 URL，供审核页 `el-image` 直接展示。
- **公有文件**（职位图、Logo、头像、培训课件、运营素材）：前端直传后存完整公开 URL，读取原样透出。

## 5. 前端直传实现

| 端 | 文件 | 说明 |
|---|---|---|
| worker-uniapp | `utils/ossUpload.js`、`api/file.js` | crypto-js 手写 OSS 签名直传；`biz` 必须走 query；仅 503 时回退旧中转上传；实名认证页（realName.vue）与头像（profile/edit.vue）已改直传 |
| enterprise-pc | `utils/ossUpload.js` | ali-oss 直传；JobForm.vue 职位图、CompanySettings.vue Logo、营业执照改直传；仅 503 回退 |
| platform-pc | `utils/ossUpload.js`、`api/training.js` | TrainingCourseList.vue 培训课件直传（biz=training）；wangEditor 图文编辑器上传仍走旧 adminUploadUrl（已声明边界） |
| enterprise-uniapp | — | 排查无企业端上传场景，未改动 |

公有场景直传成功后取 `buildPublicUrl(bucket, endpoint, key)` 完整 URL 存表单；私有场景存 key。

## 6. 环境变量清单

各服务 `application.yml` 的 `oss.*` 全部走环境变量，仓库不落真实密钥：

| 环境变量 | 说明 |
|---|---|
| `OSS_ENDPOINT` | OSS Endpoint（如 `oss-cn-hangzhou.aliyuncs.com`） |
| `OSS_REGION` | Region（如 `cn-hangzhou`） |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | 具备 AssumeRole 权限的账号 AK/SK |
| `OSS_STS_ROLE_ARN` | STS RoleArn（RAM 角色） |
| `OSS_PRIVATE_BUCKET` | 私有 bucket 名 |
| `OSS_PUBLIC_BUCKET` | 公有 bucket 名 |

未配置 OSS 时：OSSClient/StsClient Bean 不初始化，STS/签名接口返回 503「OSS未配置」，旧 `/uploads/` 中转上传仍可用（dev 环境）。

## 7. 部署注意

- 生产环境先创建双 bucket 与 RAM 角色，参考 `scripts/oss/` 下说明与策略模板。
- 私有 bucket 关闭公共读；公有 bucket 开启公共读。
- RAM 角色信任关系与最小权限策略：STS 临时凭证仅允许对**下发前缀**内对象执行 `oss:PutObject`，可配 `oss:GetObject` 供直传后校验。
- 历史本地文件不迁移，生产若开启 OSS 后旧数据仍指向 `/uploads/`，需保证静态资源可达或做读取侧兼容。
