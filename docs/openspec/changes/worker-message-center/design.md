# Worker Message Center Design

## Overview

第一版消息中心只实现工人端站内消息，目标是让工人能在 C 端消息页看到真实的关键业务通知。通知写入保持同步、简单、可追踪；微信模板消息、短信、运营后台模板配置不纳入本次范围。

## Architecture

```text
业务事件
  ├── 报名审核结果
  ├── 排班生成/变更/取消
  ├── 工资结算到账
  ├── 提现成功/失败
  ├── 实名审核结果
  └── 开工前提醒
        ↓
NotificationService.createWorkerNotification(...)
        ↓
通知表 c_notification / notification
        ↓
GET /api/notifications/my
PUT /api/notifications/{id}/read
        ↓
worker-uniapp 消息页
```

## Message Categories

| 分类 | 前端 Tab | 触发事件 |
|---|---|---|
| 系统通知 | `system` | 实名审核结果、系统类通知 |
| 报名反馈 | `application` | 报名通过、报名拒绝 |
| 排班提醒 | `schedule` | 排班生成、变更、取消、开工前提醒 |
| 收入提现 | `finance` | 工资结算到账、提现成功、提现失败 |

## Backend Design

### Notification VO

工人端消息列表至少返回：

- `id`
- `type`
- `category`
- `title`
- `content`
- `read`
- `createdAt`
- `relatedType`
- `relatedId`

### API

- `GET /api/notifications/my`
  - 返回当前登录工人的消息列表。
  - 默认按 `created_at DESC` 排序。
- `PUT /api/notifications/{id}/read`
  - 将当前登录工人的指定消息标记为已读。
  - 如果消息不属于当前工人，返回权限错误或未找到。
- 可选：`PUT /api/notifications/read-all`
  - 将当前登录工人的所有未读消息标记为已读。
  - 如第一版时间不足可不做。

### Trigger Points

- 报名审核：企业通过或拒绝报名时，给工人写报名反馈通知。
- 排班：生成、变更、取消排班时，给对应工人写排班通知。
- 结算：工资结算交易写入后，给工人写收入到账通知。
- 提现：提现成功或失败后，给工人写提现结果通知。
- 实名：审核状态变更时，给工人写系统通知。
- 开工前提醒：基于已有排班数据实现提醒，可先通过同步查询或后续定时任务落地。

## Frontend Design

`worker-uniapp/src/pages/message/message.vue` 改为真实接口驱动：

- 页面加载时请求 `/api/notifications/my`。
- 按后端返回 `category` 分组到 Tab。
- 空列表展示空状态。
- 点击消息卡片调用标记已读接口，并本地更新红点状态。
- 时间展示使用后端 `createdAt`，前端只做简短格式化。

## Data Flow

```text
用户进入消息页
  ↓
worker-uniapp 调用 getMyNotifications
  ↓
c-service 校验 JWT 获取 workerId
  ↓
NotificationService 查询 workerId 的通知
  ↓
Mapper 按 created_at DESC 返回
  ↓
前端按 category 过滤展示
  ↓
用户点击消息
  ↓
PUT mark-read
  ↓
当前卡片红点消失
```

## Decisions

- 第一版只做站内消息，降低外部依赖和配置复杂度。
- 消息触发先硬编码业务标题和内容，不引入模板配置。
- 消息读取和标记已读只允许当前登录工人操作自己的消息。
- 前端不再保留 mock 消息数据，接口失败展示错误提示或空状态。

## Risks and Mitigations

| 风险 | 影响 | 缓解 |
|---|---|---|
| 历史业务事件没有消息 | 老数据无法展示完整通知 | 只保证新事件产生消息；需要历史补偿时另写脚本 |
| 多服务共享通知表字段不一致 | 写入或查询失败 | 先确认现有表结构，再补迁移脚本 |
| 触发点分散 | 漏写通知 | 先覆盖第一版关键事件，后续逐步补充 |
| 消息过多 | 页面加载慢 | 默认倒序分页可作为后续优化 |
