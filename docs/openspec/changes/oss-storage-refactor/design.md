# Design: oss-storage-refactor

## 方案概述

引入阿里云 OSS，将三服务的上传与文件访问统一改造为"双 bucket + 前端直传 + 签名 URL"模型：

- **私有 bucket**（`parttime-private`）：实名认证、企业资质。默认拒绝匿名读，仅通过后端签发的短期签名 URL 访问。
- **公有 bucket**（`parttime-public`）：职位图片、企业 Logo、培训课件、运营素材、头像。允许匿名读，前端直接用公开 URL。

上传链路：前端登录后调各服务 STS 接口获取临时凭证（含允许写入的对象前缀），用 OSS SDK 直传；服务端不再接收大文件体。私有文件读取：前端持对象 key 调后端签名接口，后端校验权限矩阵后返回 5–15 分钟签名 URL。

## 架构与模块

### 基础设施（新增，独立于三服务）

- `oss-infra/`：OSS 配置说明 + RAM 角色与策略模板 + 部署脚本（STS RoleArn、两个 bucket、读写权限策略）
- 或由各服务共享一个公共 OSS 配置模块（三服务各自引入依赖，配置经环境变量注入）

### 后端改动（三个服务各自新增/修改）

每个服务新增：

| 文件 | 职责 |
|---|---|
| `config/OssProperties.java` | `@ConfigurationProperties(prefix="oss")`：endpoint、region、access-key-id、access-key-secret、private-bucket、public-bucket、sts-role-arn、sts-role-session-name |
| `config/OssConfig.java` | 创建 OSSClient / StsClient Bean（配置缺失时不初始化，接口返回"OSS未配置"） |
| `service/OssStsService.java` | 调阿里云 STS AssumeRole，返回：bucket、endpoint、临时AK/SK/Token、有效期、**允许写入前缀**（按登录身份生成） |
| `service/OssSignedUrlService.java` | 对私有对象生成签名 URL（expires 5–15min） |
| `controller/FileStsController.java` | `POST /api/{role}/files/sts`：鉴权后按场景（私有/公有、业务前缀）返回直传参数 |
| `controller/FileSignedUrlController.java` | `GET /api/{role}/files/signed-url?key=...`：按 REQ-4 矩阵校验后返回签名 URL |

前缀规划（后端生成，前端不可自定）：

```
私有 parttime-private/
  realname/{workerId}/idcard-front/xxxx.jpg     # 身份证正面
  realname/{workerId}/idcard-back/xxxx.jpg      # 身份证反面
  license/{enterpriseId}/xxxx.jpg               # 营业执照
公有 parttime-public/
  job/{companyId}/yyyyMMdd/xxxx.jpg             # 职位图片
  logo/{enterpriseId}/xxxx.jpg                  # 企业 Logo
  avatar/{workerId}/yyyyMMdd/xxxx.jpg           # 工人头像
  training/{yyyyMMdd}/xxxx.{mp4,pdf,...}        # 培训课件
  material/{yyyyMMdd}/xxxx.jpg                  # 运营素材
```

### 权限矩阵实现（REQ-4）

- **工人实名资料**：
  - 本人：`workerId == 当前登录工人`
  - 企业审核人员：当前登录企业 与 该工人存在报名/排班关联（`schedule_application` / `job_schedule` 归属校验）
  - 平台管理员：登录平台 admin
- **企业资质**：
  - 企业本人：`enterpriseId == 当前登录企业`
  - 平台管理员：登录平台 admin
- 拒绝 → 403（统一错误码/文案）

### 前端改动

| 端 | 改动 |
|---|---|
| worker-uniapp | `api/file.js` 新增 `getSts(bizType)`、`getSignedUrl(key)`；实名认证页（realName.vue）与头像上传改直传；`uni.uploadFile` 替换为 OSS 直传（H5 用 ali-oss SDK，小程序用 OSS SDK for mini-program） |
| enterprise-pc | `api/upload.js` 改为直传；职位图片（JobForm.vue）、企业 Logo（CompanySettings.vue）、营业执照上传改直传 |
| enterprise-uniapp | 如存在上传场景同步改造 |
| platform-pc | `api/training.js` adminUploadUrl 改为直传；培训课件（TrainingCourseList.vue）改直传 |

直传统一封装：`utils/ossUpload.js`（拿 STS → 生成对象 key（后端前缀+随机名）→ 分片/普通上传 → 返回公开 URL 或对象 key）。

### 数据库/URL 字段

- 公有文件：存完整公开 URL（`https://{public-bucket}.{endpoint}/{key}`）
- 私有文件：存**对象 key**（如 `realname/{workerId}/...`），读取接口按需转签名 URL
- 兼容：读取时若字段为 `/uploads/...`（旧本地路径），直接透出（dev 环境）；若为 OSS key/URL，按 bucket 处理

## 关键流程

### 上传（前端直传）

```
前端登录 → POST /api/{role}/files/sts?biz=realname|license|job|logo|avatar|training|material
  → 返回 { bucket, endpoint, accessKeyId, accessKeySecret, securityToken, expiration, prefix }
前端用 OSS SDK 上传 → 成功 → 将 key/URL 提交业务表单
```

### 私有文件查看

```
前端持 key → GET /api/{role}/files/signed-url?key=realname/123/...
  → 后端解析 key 前缀 → 校验权限矩阵 → 返回 { url, expiresAt }
前端 img src = url 直接展示
```

## 接口定义

### `POST /api/worker/files/sts` / `POST /api/enterprise/files/sts` / `POST /api/admin/files/sts`
请求：`biz`（业务场景枚举：realname / license / job / logo / avatar / training / material）
响应：
```json
{ "bucket": "parttime-private", "endpoint": "oss-cn-hangzhou.aliyuncs.com",
  "accessKeyId": "...", "accessKeySecret": "...", "securityToken": "...",
  "expiration": "2026-09-24T10:30:00Z", "prefix": "realname/123/" }
```

### `GET /api/{role}/files/signed-url?key={objectKey}`
响应：
```json
{ "url": "https://parttime-private.oss-cn-hangzhou.aliyuncs.com/realname/123/xxx.jpg?Expires=...&Signature=...",
  "expiresAt": 1750800000000 }
```
403：无权限；404：对象不存在/非法 key。

## 对现有代码的影响

- 三服务：删除/废弃本地落盘逻辑（保留 dev fallback 可选），`FileController.upload` 中转接口保留为兼容（或标注 deprecated）
- 前端：`upload` 调用全部替换为直传；旧的 `enterpriseUploadUrl`/`adminUploadUrl` 下线
- 配置：各服务 `application.yml` 新增 `oss.*`（环境变量注入）
- 测试：三服务 FileControllerTest / FileStorageServiceTest 更新为 STS/签名 URL 契约测试（Mock OSS）

## 备选方案

- 方案 A（选定）：签名 URL（私有查看）+ 前端直传。私密性与体验平衡，流量不穿后端。
- 方案 B：后端代理转发私有文件。控制强但占后端带宽，未选。
- 方案 C：上传仍后端中转。改动小但不满足"前端直传"要求，未选。
- 为何选 A：用户已确认方案 A + 前端直传（STS）。
