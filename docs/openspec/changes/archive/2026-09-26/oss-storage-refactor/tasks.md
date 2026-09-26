# Tasks: oss-storage-refactor

## 1. 基础设施与配置

- [x] 1.1 编写 OSS 双 bucket + RAM 角色/策略说明与部署脚本（`scripts/oss/`：私有/公有 bucket、STS RoleArn、前缀权限策略模板）
- [x] 1.2 三服务 `application.yml` 新增 `oss.*` 配置项（endpoint/region/ak/sk/private-bucket/public-bucket/sts-role-arn），全部走环境变量，默认不落真实密钥
- [x] 1.3 新增公共 `OssProperties` + `OssConfig`（OSSClient/StsClient Bean，配置缺失时不初始化）

## 2. 后端 STS 与签名 URL

- [x] 2.1 c-service 新增 `OssStsService`（AssumeRole）与 `POST /api/worker/files/sts`，按 biz 返回直传参数与允许前缀（realname 私有 / avatar、job 公有）
- [x] 2.2 c-service 新增 `OssSignedUrlService` 与 `GET /api/worker/files/signed-url?key=`，按权限矩阵签发私有文件签名 URL
- [x] 2.3 enterprise-service 新增 STS 接口（license 私有 / logo、job 公有）与签名 URL 接口（企业资质本人可见）
- [x] 2.4 platform-service 新增 STS 接口（training、material 公有）与签名 URL 接口（平台管理员可看工人实名与企业资质）
- [x] 2.5 三服务 FileController 旧中转上传标注 deprecated（保留 dev fallback）

## 3. 权限矩阵实现

- [x] 3.1 c-service：工人实名资料签名校验（本人 workerId 匹配）
- [x] 3.2 c-service / enterprise-service：企业审核人员查看工人实名（报名/排班关联校验）
- [x] 3.3 platform-service：平台管理员查看工人实名与企业资质
- [x] 3.4 无权限统一 403，补契约测试（FileStsControllerTest / FileSignedUrlControllerTest）

## 4. 前端直传改造

- [x] 4.1 worker-uniapp：新增 `utils/ossUpload.js` 与 `api/file.js`（getSts/getSignedUrl），实名认证页与头像上传改直传
- [x] 4.2 enterprise-pc：`api/upload.js` 改直传，职位图片（JobForm.vue）、Logo（CompanySettings.vue）、营业执照改直传
- [x] 4.3 enterprise-uniapp：如有上传场景同步直传改造
- [x] 4.4 platform-pc：培训课件（TrainingCourseList.vue）与运营素材改直传

## 5. URL 持久化与兼容

- [x] 5.1 私有文件字段（`worker_real_name_auth.id_card_*_url`、`enterprise_real_name_auth.business_license_url`）存对象 key 约定，读取接口返回签名 URL
- [x] 5.2 公有文件存完整公开 URL；旧 `/uploads/` 本地路径读取兼容
- [x] 5.3 平台端实名/资质审核页接入签名 URL 展示（WorkerRealNameAuthReviewController / EnterpriseRealNameAuthReviewController 相关 VO/页面）

## 6. 测试与收尾

- [x] 6.1 三服务 `mvn test` 全绿（更新 FileControllerTest / FileStorageServiceTest 为 STS/签名契约测试）
- [x] 6.2 前端四端构建通过（worker-uniapp build:h5 + build:mp-weixin、enterprise-pc、enterprise-uniapp、platform-pc build）
- [x] 6.3 文档：`docs/上传与OSS存储设计.md`（bucket 划分、前缀规划、权限矩阵、STS/签名接口说明、环境变量清单）
