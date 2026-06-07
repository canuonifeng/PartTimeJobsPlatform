# Worker Message Center Implementation Tasks

## 1. 数据结构与接口确认

- [x] 1.1 检查现有通知表结构，确认 `c_notification` 或通知表字段是否支持接收人、分类、标题、内容、已读状态、创建时间、业务关联信息。
- [x] 1.2 如字段不足，新增迁移脚本，补充消息分类、已读状态、业务关联字段。
- [x] 1.3 检查 `c-service` 现有 `NotificationController`、`NotificationService`、`NotificationMapper` 和 XML，确认当前 `/api/notifications/my` 返回字段。

## 2. C 端通知查询 API

- [x] 2.1 扩展 `NotificationVO`，返回 `id`、`type`、`category`、`title`、`content`、`read`、`createdAt`、`relatedType`、`relatedId`。
- [x] 2.2 修改 Mapper XML，按当前登录工人查询消息并按 `created_at DESC` 排序。
- [x] 2.3 新增或完善 `GET /api/notifications/my`，只返回当前登录工人的消息。
- [x] 2.6 将 `GET /api/notifications/my` 改为分页返回 `PageVO<NotificationVO>`，支持 `page`、`pageSize`，按 `sent_at DESC, id DESC` 排序。
- [x] 2.4 新增 `PUT /api/notifications/{id}/read`，只允许当前登录工人标记自己的消息已读。
- [x] 2.5 补 Controller 或 Service 测试，覆盖查询、标记已读、拒绝操作他人消息。

## 3. 业务事件写通知

- [x] 3.1 在报名审核通过/拒绝流程中写入报名反馈通知。
- [x] 3.2 在排班生成/变更/取消流程中写入排班提醒通知。
- [x] 3.3 在工资结算到账流程中写入收入提现通知。
- [x] 3.4 在提现成功/失败流程中写入收入提现通知。
- [x] 3.5 在实名审核通过/拒绝流程中写入系统通知。
- [ ] 3.6 设计开工前提醒落地方式；后续接入定时任务时再实现。
- [x] 3.7 补关键触发点测试或最小 Service 测试，确认业务事件会写通知。

## 4. 工人端消息页

- [x] 4.1 新增或完善 `worker-uniapp/src/api/notifications.js`，封装获取我的消息和标记已读接口。
- [x] 4.2 修改 `worker-uniapp/src/pages/message/message.vue`，移除静态 mock 消息。
- [x] 4.3 页面加载时请求真实消息列表，接口失败显示提示，空列表展示空状态。
- [x] 4.9 消息页首屏加载第一页，触底按下一页追加消息，全部加载完后停止请求。
- [x] 4.4 Tab 调整为系统通知、报名反馈、排班提醒、收入提现。
- [x] 4.5 点击消息调用标记已读接口，并本地更新未读红点。
- [x] 4.6 底部消息 tabBar 在存在任一未读消息时显示红点，无未读时隐藏红点。
- [x] 4.7 消息页分类 Tab 在对应分类存在未读消息时显示红点，无未读时隐藏红点。
- [x] 4.8 保持微信小程序兼容样式，使用 `rpx`，避免 H5 专属 API。

## 5. 验证

- [x] 5.1 运行 `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`。
- [x] 5.2 运行 `cd worker-uniapp && npm run build:h5`。
- [x] 5.3 运行 `cd worker-uniapp && npm run build:mp-weixin`。
- [x] 5.4 运行 `git diff --check`。
- [x] 5.5 如新增 SQL 脚本，本地执行迁移并验证接口返回真实消息。
