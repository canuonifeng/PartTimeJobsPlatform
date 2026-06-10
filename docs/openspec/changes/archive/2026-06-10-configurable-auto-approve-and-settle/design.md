# 报名自动审核 + 自动结算 + 签到距离配置 — 设计文档

## 架构概览

```
                    system_configs
                    ┌──────────────────────────────────┐
                    │ auto_approve_applications (bool)  │
                    │ auto_settle_attendance (bool)     │
                    │ check_in_radius_meters (int)      │
                    └──────┬───────────────┬────────────┘
                           │               │
              ┌────────────▼──┐     ┌──────▼────────────┐
              │   c-service   │     │   worker-uniapp   │
              │               │     │                   │
              │ applyForJob() │     │ checkDistance()    │
              │  ↓ auto-approve│    │  ↓ 读取全局半径    │
              │ checkOut()    │     │ handleCheckOut()   │
              │  ↓ auto-settle│     │  ↓ autoSettled弹窗 │
              │  ↓ autoSettled│     │                   │
              └───────────────┘     └───────────────────┘
                           │
              ┌────────────▼──────────────┐
              │        jobs 表             │
              │  auto_approve (nullable)   │
              │  null → 跟随平台默认       │
              │  true → 强制自动审核       │
              │  false → 强制手动审核      │
              └───────────────────────────┘
```

## 数据流

### 报名自动审核

```
工人点击报名 → applyForJob()
  → 读取 job.autoApprove（职位级）
  → 若 null，读取 system_configs.auto_approve_applications（平台级）
  → 若 autoApprove=true：
      创建 ScheduleApplication(status=ACCEPTED)
      创建 ScheduleShift(status=SCHEDULED)
      发送通知"报名已通过"
  → 若 autoApprove=false：
      创建 ScheduleApplication(status=PENDING)（现有流程）
```

### 打卡自动结算

```
工人签退 → checkOut()
  → 计算工时和薪资（现有逻辑）
  → 读取 system_configs.auto_settle_attendance
  → 若开启：
      检查企业余额
      → 余额充足：
          入账工人余额
          插入 balance_transactions
          更新 settlement_status=PAID
          扣减企业余额
          插入 enterprise_balance_transactions
          发送通知"收入到账"
          返回 autoSettled=true
      → 余额不足：
          settlement_status 保持 UNPAID
          返回 autoSettled=false
  → 前端收到 autoSettled=true → 弹窗"已收到 ¥X 薪资，去提现？"
```

### 签到距离校验

```
工人签到/签退 → checkDistance() / 后端校验
  → 读取 shift.locationRadius（班次级）
  → 若为空，读取 system_configs.check_in_radius_meters（平台级）
  → 若仍为空，默认 100m
  → 校验 GPS 距离是否在半径内
```

## 设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 自动审核粒度 | 平台级 + 职位级覆盖 | 灵活：大企业可全局关闭，小企业可按职位开启 |
| 自动结算触发时机 | 签退时立即结算 | 简单直接，无需定时任务 |
| 企业余额不足处理 | 降级为 UNPAID | 安全：不阻止签退，不影响工人体验 |
| 签到距离配置 | 复用已有 check_in_radius_meters | 该配置已存在但从未被使用，激活它 |
| autoSettled 传递方式 | 响应 VO 新增字段 | 前端无需额外请求，签退响应直接携带 |

## 风险与缓解

| 风险 | 缓解 |
|------|------|
| 自动结算时企业余额不足 | 降级为 UNPAID，不影响签退 |
| 自动审核后企业想拒绝 | 企业端仍可手动拒绝 ACCEPTED 状态的报名 |
| 全局半径配置被误改 | 默认值 100m 兜底 |
| 并发签退导致重复结算 | WorkerBalanceMapper.upsert 使用 ON DUPLICATE KEY UPDATE |
