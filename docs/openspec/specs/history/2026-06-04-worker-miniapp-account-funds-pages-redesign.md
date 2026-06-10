# 工人端微信小程序 账号与资金页面视觉改造设计

## 背景

工人端已完成主 Tab、详情报名、排班打卡核心闭环。本次继续改造账号与资金相关页面，补齐设计稿中登录、收入、设置等剩余体验。

## 范围

改造：

- `pages/login/login.vue`
- `pages/earnings/earnings.vue`
- `pages/earnings/withdraw.vue`
- `pages/auth/realName.vue`
- `pages/bank/bankCard.vue`
- `pages/profile/edit.vue`

新增：

- `pages/settings/settings.vue`

修改：

- `pages.json` 注册设置页
- `pages/profile/profile.vue` 中设置菜单跳转真实设置页

## 页面设计

### 登录页

按设计稿登录页重做视觉：绿色渐变背景、应用 logo、手机号登录卡片、验证码输入、微信登录入口、用户协议。保留现有微信登录、手机号登录、验证码发送、redirect 逻辑。

### 收入明细

按设计稿收入明细页重做：橙色渐变头部、累计收入、本月/待结算/已提现统计、筛选 tabs、按日期分组的收入与提现记录。保留 `getEarningsSummary`、`getEarningsTransactions`、`getMyAttendance` 和分页加载；接口失败时显示兜底交易。

### 提现

重做提现页视觉：可提现金额卡、实名认证/银行卡前置提示、金额输入、快捷金额、确认提现。保留 `createWithdrawal`、`getEarningsSummary`、实名和银行卡 gate。

### 实名认证

重做为状态卡 + 表单卡：姓名、身份证、证件照片 URL、提交按钮；审核中/已通过/已拒绝状态分别展示提示。保留 `getRealNameStatus`、`submitRealName`。

### 银行卡

保留银行卡绑定/更新/解绑逻辑，优化银行卡预览卡、表单卡、解绑按钮。接口失败时展示空表单。

### 个人资料编辑

优化头像、姓名、手机号、性别、生日、技能、可工作日期表单。保留 `getProfile`、`updateProfile`、`authStore.setWorkerInfo`。

### 设置页

新增设计稿设置页：账号与安全、通知与隐私、通用、其他、退出登录。设置项中已有页面可跳转；暂无页面显示“功能建设中”；开关本地切换。退出登录复用 `authStore.logout()`。

## 约束

- 不改后端接口
- 不改 TabBar
- 微信小程序优先，避免 H5 专属能力
- 所有接口失败保留 toast 或兜底视觉
- 完成后运行 `npm run build:mp-weixin`
- 验证通过后提交并推送
