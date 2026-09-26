# Implementation Plan: oss-storage-refactor (2026-09-24)

## Overview
- 目标：引入阿里云 OSS 双 bucket，前端直传（STS）+ 私有文件签名 URL 访问，落地权限矩阵
- 相关工件：proposal / design / tasks：`docs/openspec/changes/oss-storage-refactor/`
- 本计划范围：Task 1.1 – 2.5（基础设施与配置、后端 STS 与签名 URL）

## Task 1: OSS 双 bucket + RAM 角色说明与部署脚本
<!-- openspec-task: 1.1 -->
### Task 1: OSS 双 bucket + RAM 角色说明与部署脚本
**文件**: `scripts/oss/README.md`、`scripts/oss/buckets.md`、`scripts/oss/ram-policy.json`

**步骤**:
1. 编写 bucket 规划（私有 `parttime-private` / 公有 `parttime-public`，region、endpoint 约定）
2. 编写 RAM 角色与 STS 策略模板（前缀粒度：realname/license 私有写，job/logo/avatar/training/material 公有写）
3. 编写部署说明（创建 bucket、配置权限、获取 RoleArn）

**验证**:
- [ ] 文档覆盖 bucket 名、Region、RoleArn、权限策略模板
- [ ] 策略精确到前缀，无越权通配

**提交**: docs: oss bucket and RAM role deployment guide

## Task 2: 三服务 application.yml 新增 oss.* 配置
<!-- openspec-task: 1.2 -->
### Task 2: 三服务 application.yml 新增 oss.* 配置
**文件**: `c-service/src/main/resources/application.yml`、`enterprise-service/src/main/resources/application.yml`、`platform-service/src/main/resources/application.yml`

**步骤**:
1. 各服务新增 `oss.endpoint` / `oss.region` / `oss.access-key-id` / `oss.access-key-secret` / `oss.private-bucket` / `oss.public-bucket` / `oss.sts-role-arn` / `oss.sts-role-session-name`（全部 `${ENV_VAR:}` 默认空）
2. 确保无真实密钥入库

**验证**:
- [ ] 配置项齐全、走环境变量
- [ ] grep 仓库无真实 AK/SK

**提交**: chore: add oss config placeholders in three services

## Task 3: 公共 OssProperties + OssConfig
<!-- openspec-task: 1.3 -->
### Task 3: 公共 OssProperties + OssConfig
**文件**: 三服务各自 `config/OssProperties.java`、`config/OssConfig.java`

**步骤**:
1. `OssProperties` 用 `@ConfigurationProperties(prefix="oss")` 绑定配置
2. `OssConfig` 条件创建 OSSClient / StsClient Bean（配置缺失时不初始化，避免启动失败）
3. 三服务各自引入（不建公共 jar 时复制实现，保持服务独立）

**验证**:
- [ ] 无 OSS 配置时服务仍可启动
- [ ] 有配置时 Bean 创建成功

**提交**: feat: oss client config in three services

## Task 4: c-service STS 接口（worker 侧）
<!-- openspec-task: 2.1 -->
### Task 4: c-service STS 接口（worker 侧）
**文件**: `c-service/.../service/OssStsService.java`、`c-service/.../service/impl/OssStsServiceImpl.java`、`c-service/.../controller/FileStsController.java`、`c-service/.../pojo/vo/OssStsVO.java`

**步骤**:
1. `OssStsServiceImpl`：调阿里云 STS AssumeRole，返回 bucket/endpoint/临时AK/SK/Token/expiration/prefix
2. 按登录 workerId 与 biz 生成前缀：realname→`realname/{workerId}/`（私有）、avatar→`avatar/{workerId}/`（公有）、job→`job/{companyId}/`（公有）
3. `POST /api/worker/files/sts?biz=...` 鉴权（worker 登录），未登录 401/403
4. `OssStsVO` 承载直传参数

**验证**:
- [ ] 未登录返回 401/403
- [ ] 不同 biz 返回正确 bucket 与前缀
- [ ] 单测（Mock STS）通过

**提交**: feat: worker oss sts endpoint

## Task 5: c-service 签名 URL 接口
<!-- openspec-task: 2.2 -->
### Task 5: c-service 签名 URL 接口
**文件**: `c-service/.../service/OssSignedUrlService.java`、`c-service/.../service/impl/OssSignedUrlServiceImpl.java`、`c-service/.../controller/FileSignedUrlController.java`

**步骤**:
1. `OssSignedUrlServiceImpl`：解析 key（realname/license 前缀），按权限矩阵签发 5–15 分钟签名 URL
2. `GET /api/worker/files/signed-url?key=...`：工人本人可看自己的 realname 文件
3. 无权限 403；非法 key 404

**验证**:
- [ ] 本人可拿签名 URL
- [ ] 他人 403
- [ ] 单测通过

**提交**: feat: worker signed url endpoint

## Task 6: enterprise-service STS + 签名 URL
<!-- openspec-task: 2.3 -->
### Task 6: enterprise-service STS + 签名 URL
**文件**: `enterprise-service/.../service/OssStsService.java`、`.../OssSignedUrlService.java`、`.../controller/FileStsController.java`、`.../controller/FileSignedUrlController.java`

**步骤**:
1. `POST /api/enterprise/files/sts`：license→`license/{enterpriseId}/`（私有）、logo→`logo/{enterpriseId}/`（公有）、job→`job/{companyId}/`（公有）
2. `GET /api/enterprise/files/signed-url`：企业本人可看自己 license 文件
3. 企业审核人员查看工人实名：校验报名/排班关联（schedule_application 归属）

**验证**:
- [ ] 企业本人可看自己营业执照
- [ ] 有报名关联的企业可看工人实名，无关企业 403
- [ ] 单测通过

**提交**: feat: enterprise oss sts and signed url endpoints

## Task 7: platform-service STS + 签名 URL
<!-- openspec-task: 2.4 -->
### Task 7: platform-service STS + 签名 URL
**文件**: `platform-service/.../service/OssStsService.java`、`.../OssSignedUrlService.java`、`.../controller/FileStsController.java`、`.../controller/FileSignedUrlController.java`

**步骤**:
1. `POST /api/admin/files/sts`：training→`training/`（公有）、material→`material/`（公有）
2. `GET /api/admin/files/signed-url`：平台管理员可看工人实名与企业资质（任意 realname/license key）
3. 平台管理员鉴权（admin 登录）

**验证**:
- [ ] 管理员可看任意实名/资质签名 URL
- [ ] 未登录 401
- [ ] 单测通过

**提交**: feat: admin oss sts and signed url endpoints

## Task 8: 旧中转上传标注 deprecated
<!-- openspec-task: 2.5 -->
### Task 8: 旧中转上传标注 deprecated
**文件**: 三服务 `controller/FileController.java`

**步骤**:
1. 旧 `/upload` 中转接口标注 `@Deprecated`（保留 dev fallback 逻辑）
2. 注释说明由 STS 直传替代

**验证**:
- [ ] 编译通过，旧接口仍可用（兼容）

**提交**: chore: deprecate legacy upload endpoints
