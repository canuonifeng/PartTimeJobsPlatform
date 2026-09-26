# Implementation Plan: oss-storage-refactor (2026-09-24)

## Overview
- 目标：引入阿里云 OSS 双 bucket，前端直传（STS）+ 私有文件签名 URL 访问，落地权限矩阵
- 相关工件：proposal / design / tasks：`docs/openspec/changes/oss-storage-refactor/`
- 本计划范围：Task 3.1 – 4.3（权限矩阵实现、worker-uniapp / enterprise-pc / enterprise-uniapp 前端直传）

## Task 9: 工人实名资料本人校验
<!-- openspec-task: 3.1 -->
### Task 9: 工人实名资料本人校验
**文件**: `c-service/.../service/impl/OssSignedUrlServiceImpl.java`

**步骤**:
1. key 前缀 `realname/{workerId}/` 时，校验当前登录工人 == workerId
2. 不等返回 403

**验证**:
- [ ] 本人可看，他人 403（单测）

**提交**: feat: worker realname self-access check

## Task 10: 企业审核人员查看工人实名
<!-- openspec-task: 3.2 -->
### Task 10: 企业审核人员查看工人实名
**文件**: `enterprise-service/.../service/impl/OssSignedUrlServiceImpl.java`、`c-service/.../service/impl/OssSignedUrlServiceImpl.java`（如涉及）

**步骤**:
1. 企业端签名接口收到 `realname/{workerId}/` key 时，校验当前企业与该工人存在报名/排班关联
2. 关联来源：schedule_application / job_schedule / annotation_task_orders 归属校验
3. 无关联 403

**验证**:
- [ ] 有报名关联企业可看，无关企业 403（单测）

**提交**: feat: enterprise reviewer access to worker realname

## Task 11: 平台管理员查看实名与企业资质
<!-- openspec-task: 3.3 -->
### Task 11: 平台管理员查看实名与企业资质
**文件**: `platform-service/.../service/impl/OssSignedUrlServiceImpl.java`

**步骤**:
1. 管理员签名接口接受任意 `realname/*`、`license/*` key
2. 仅平台 admin 角色可访问，否则 403

**验证**:
- [ ] 管理员可看，非管理员 403（单测）

**提交**: feat: admin access to realname and license

## Task 12: 权限矩阵契约测试
<!-- openspec-task: 3.4 -->
### Task 12: 权限矩阵契约测试
**文件**: 三服务 `src/test/java/.../controller/FileStsControllerTest.java`、`FileSignedUrlControllerTest.java`

**步骤**:
1. 覆盖：本人/关联企业/管理员 ✅，无权限 ❌ 403
2. 非法 key 404、未登录 401
3. 更新既有 FileControllerTest / FileStorageServiceTest 适配 STS/签名契约

**验证**:
- [ ] 三服务相关测试全绿

**提交**: test: oss permission matrix contract tests

## Task 13: worker-uniapp 直传封装
<!-- openspec-task: 4.1 -->
### Task 13: worker-uniapp 直传封装
**文件**: `worker-uniapp/src/utils/ossUpload.js`、`worker-uniapp/src/api/file.js`

**步骤**:
1. `file.js`：`getSts(biz)`、`getSignedUrl(key)` 接口封装
2. `ossUpload.js`：拿 STS → 组装对象 key（后端 prefix + 随机文件名）→ 直传（H5 用 ali-oss、小程序用 mini SDK）→ 返回公开 URL 或对象 key
3. 实名认证页（realName.vue）与头像上传改直传

**验证**:
- [ ] build:h5 通过
- [ ] build:mp-weixin 通过

**提交**: feat: worker-uniapp oss direct upload

## Task 14: enterprise-pc 直传改造
<!-- openspec-task: 4.2 -->
### Task 14: enterprise-pc 直传改造
**文件**: `enterprise-pc/src/api/upload.js`、`enterprise-pc/src/utils/ossUpload.js`、`enterprise-pc/src/views/jobs/JobForm.vue`、`enterprise-pc/src/views/settings/CompanySettings.vue`

**步骤**:
1. `upload.js` 新增 getSts/getSignedUrl
2. `ossUpload.js` 直传封装（ali-oss）
3. 职位图片（JobForm.vue）、Logo（CompanySettings.vue）、营业执照上传改直传

**验证**:
- [ ] enterprise-pc build 通过

**提交**: feat: enterprise-pc oss direct upload

## Task 15: enterprise-uniapp 上传场景直传
<!-- openspec-task: 4.3 -->
### Task 15: enterprise-uniapp 上传场景直传
**文件**: `enterprise-uniapp/src/api/*`、`enterprise-uniapp/src/pages/*`（按现状排查上传点）

**步骤**:
1. 排查 enterprise-uniapp 现有上传场景
2. 若有，接入直传（getSts + mini SDK）
3. 若无上传场景，标记为无改动

**验证**:
- [ ] build:mp-weixin 通过（若有改动）

**提交**: feat: enterprise-uniapp oss direct upload (if applicable)
