# Implementation Plan: oss-storage-refactor (2026-09-24)

## Overview
- 目标：引入阿里云 OSS 双 bucket，前端直传（STS）+ 私有文件签名 URL 访问，落地权限矩阵
- 相关工件：proposal / design / tasks：`docs/openspec/changes/oss-storage-refactor/`
- 本计划范围：Task 4.4 – 6.3（platform-pc 直传、URL 持久化与兼容、测试与收尾）

## Task 16: platform-pc 直传改造
<!-- openspec-task: 4.4 -->
### Task 16: platform-pc 直传改造
**文件**: `platform-pc/src/api/training.js`、`platform-pc/src/utils/ossUpload.js`、`platform-pc/src/views/training/TrainingCourseList.vue`

**步骤**:
1. `training.js` 新增 getSts/getSignedUrl
2. `ossUpload.js` 直传封装（ali-oss）
3. 培训课件（TrainingCourseList.vue）、运营素材上传改直传

**验证**:
- [ ] platform-pc build 通过

**提交**: feat: platform-pc oss direct upload

## Task 17: 私有文件字段存对象 key 约定
<!-- openspec-task: 5.1 -->
### Task 17: 私有文件字段存对象 key 约定
**文件**: `c-service/.../service/impl/ProfileServiceImpl.java`（实名提交）、`enterprise-service/.../service/impl/EnterpriseRealNameAuthServiceImpl.java`、相关 Mapper XML 无字段变更

**步骤**:
1. 实名认证提交时 `idCardFrontUrl/idCardBackUrl/businessLicenseUrl` 存 OSS 对象 key（直传返回的 key）
2. 读取 VO 时：若为 OSS key（realname/license 前缀），转为签名 URL 或返回 key 由前端调签名接口
3. 保持字段结构不变（URL/key 均可容纳）

**验证**:
- [ ] 新实名提交后字段为 OSS key，读取路径正常
- [ ] 单测/契约测试通过

**提交**: feat: store private file object keys

## Task 18: 公有文件完整 URL + 旧路径兼容
<!-- openspec-task: 5.2 -->
### Task 18: 公有文件完整 URL + 旧路径兼容
**文件**: 涉及职位图片/Logo/头像/课件的 VO 与服务（c-service JobSummaryVO/JobDetailVO、enterprise-service、platform-service training）

**步骤**:
1. 公有上传后存完整公开 URL（`https://{public-bucket}.{endpoint}/{key}`）
2. 读取兼容：字段为 `/uploads/...` 时透出原样；为 OSS key 时拼公开 URL

**验证**:
- [ ] 新公有文件 URL 完整可访问
- [ ] 旧 `/uploads/` 数据展示不变（dev）

**提交**: feat: public file full URL persistence

## Task 19: 平台审核页接入签名 URL
<!-- openspec-task: 5.3 -->
### Task 19: 平台审核页接入签名 URL
**文件**: `platform-service/.../controller/WorkerRealNameAuthReviewController.java`、`EnterpriseRealNameAuthReviewController.java`、`platform-pc` 对应审核页面/API

**步骤**:
1. 审核详情接口返回签名 URL（或前端持 key 调 `/api/admin/files/signed-url`）
2. platform-pc 审核页图片 src 使用签名 URL

**验证**:
- [ ] 平台审核页可正常显示身份证/营业执照图片
- [ ] platform-pc build 通过

**提交**: feat: admin review pages with signed urls

## Task 20: 三服务 mvn test 全绿
<!-- openspec-task: 6.1 -->
### Task 20: 三服务 mvn test 全绿
**文件**: 三服务测试（更新 + 新增）

**步骤**:
1. `cd c-service && mvn test`、`cd enterprise-service && mvn test`、`cd platform-service && mvn test`
2. 修复新增测试与既有测试冲突
3. 记录既有基线失败（如有，与本次改动无关的单独说明）

**验证**:
- [ ] 三服务测试全绿（或基线失败单独说明）

**提交**: test: oss refactor full test pass

## Task 21: 前端四端构建
<!-- openspec-task: 6.2 -->
### Task 21: 前端四端构建
**文件**: worker-uniapp / enterprise-uniapp / enterprise-pc / platform-pc

**步骤**:
1. `worker-uniapp`: `npm run build:h5` + `npm run build:mp-weixin`
2. `enterprise-uniapp`: `npm run build:mp-weixin`
3. `enterprise-pc`: `npm run build`
4. `platform-pc`: `npm run build`

**验证**:
- [ ] 四端构建通过

**提交**: build: frontend builds pass

## Task 22: 文档与交付
<!-- openspec-task: 6.3 -->
### Task 22: 文档与交付
**文件**: `docs/上传与OSS存储设计.md`

**步骤**:
1. 编写：bucket 划分、前缀规划、权限矩阵、STS/签名接口说明、环境变量清单、直传接入示例
2. 同步 tasks.md 勾选状态（openspec-task 全部 [x]）

**验证**:
- [ ] 文档与实际实现一致
- [ ] tasks.md 全勾选

**提交**: docs: oss storage design
