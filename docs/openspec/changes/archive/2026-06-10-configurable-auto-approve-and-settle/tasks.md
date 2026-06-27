# 报名自动审核 + 自动结算 + 签到距离配置 — 任务清单

## 1. 数据库迁移

- [x] 1.1 创建 `scripts/22_auto_approve_and_auto_settle.sql`，插入 `auto_approve_applications`、`auto_settle_attendance`、`check_in_radius_meters` 配置项
- [x] 1.2 `jobs` 表新增 `auto_approve` 字段（TINYINT(1) DEFAULT NULL）

## 2. c-service — 报名自动审核

- [x] 2.1 Job 实体新增 `autoApprove` 字段
- [x] 2.2 JobMapper.xml 新增 `auto_approve` 字段映射
- [x] 2.3 JobServiceImpl.applyForJob() 支持自动审核：读取职位级/平台级配置，自动创建 ACCEPTED 报名 + ScheduleShift + 通知
- [x] 2.4 编写自动审核单元测试（平台开/关 × 职位开/关/跟随 默认 组合）

## 3. c-service — 打卡自动结算

- [x] 3.1 AttendanceVO 新增 `autoSettled` 字段
- [x] 3.2 AttendanceServiceImpl.checkOut() 支持自动结算：读取配置，入账工人余额，扣减企业余额，余额不足时降级
- [x] 3.3 编写自动结算单元测试（开启/关闭/余额不足）

## 4. c-service — 签到距离全局配置

- [x] 4.1 AttendanceServiceImpl 签到/签退距离校验读取 `check_in_radius_meters` 配置作为 fallback

## 5. worker-uniapp — 签退弹窗 + 签到距离

- [x] 5.1 创建 `api/config.js`，封装全局配置读取（含缓存）
- [x] 5.2 index.vue 签退成功后根据 `autoSettled` 弹窗提示薪资到账并引导提现
- [x] 5.3 clockIn.vue 签退成功后根据 `autoSettled` 弹窗提示薪资到账并引导提现
- [x] 5.4 index.vue 签到距离校验读取全局配置替代硬编码 100m

## 6. enterprise-pc — 职位表单

- [x] 6.1 职位创建/编辑表单新增"自动审核"开关（跟随平台默认/开启/关闭）

## 7. 验证

- [x] 7.1 c-service 全部测试通过
- [x] 7.2 enterprise-service 全部测试通过
- [x] 7.3 platform-service 全部测试通过
- [x] 7.4 worker-uniapp 构建通过
- [x] 7.5 enterprise-pc 构建通过
- [x] 7.6 platform-pc 构建通过
