## Why

平台当前报名审核和结算流程均为纯手动操作：工人报名后企业必须逐一审核通过/拒绝，打卡签退后企业必须手动点击"批量结算"。当企业用工量大时，手动操作成为瓶颈。同时，签到距离校验硬编码为前端 100m，无法灵活调整。

平台需要将这两个环节做成可配置的自动化能力，并支持签到距离的全局配置，让运营根据实际情况灵活开关和调整。

## What Changes

- 新增报名自动审核：平台级开关 + 职位级覆盖，开启后工人报名直接通过并自动生成排班
- 新增打卡自动结算：平台级开关，开启后工人签退时自动完成薪资结算（入账工人余额、扣减企业余额）
- 新增签退成功弹窗：自动结算成功时弹窗提示"已收到 ¥X 薪资，去提现"
- 新增签到最小距离全局配置：复用已有 `check_in_radius_meters` 配置项，后端和前端均读取该值

## Capabilities

### New Capabilities

- `auto-approve-applications`: 报名自动审核，支持平台级默认 + 职位级覆盖
- `auto-settle-attendance`: 打卡签退后自动结算，含企业余额不足时降级为 UNPAID
- `auto-settle-popup`: 签退自动结算成功弹窗，引导工人去提现
- `configurable-check-in-radius`: 签到最小距离全局可配置

### Modified Capabilities

- `job-management`: 职位创建/编辑新增"自动审核"开关（跟随平台默认/开启/关闭）
- `schedule-attendance`: 签到/签退距离校验读取全局配置而非硬编码

## Impact

- **数据库**：
  - `system_configs` 新增 `auto_approve_applications`、`auto_settle_attendance`、`check_in_radius_meters` 配置项
  - `jobs` 表新增 `auto_approve` 字段（nullable BOOLEAN）
- **后端 API**：
  - c-service：`applyForJob()` 支持自动审核（直接创建 ACCEPTED + ScheduleShift）
  - c-service：`checkOut()` 支持自动结算（入账工人余额、扣减企业余额）
  - c-service：`checkOut()` 返回 `autoSettled` 字段供前端弹窗
  - c-service：签到/签退距离校验读取 `check_in_radius_meters` 配置
- **前端页面**：
  - worker-uniapp：签退成功后根据 `autoSettled` 弹窗提示薪资到账并引导提现
  - worker-uniapp：首页签到距离校验读取全局配置
  - enterprise-pc：职位表单新增"自动审核"开关
  - platform-pc：系统配置列表自动展示新配置项
