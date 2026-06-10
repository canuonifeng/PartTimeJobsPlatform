# 奖励发放规范

## 概述

被邀请人满足条件后，系统自动创建奖励记录。根据配置决定是否需要审核，以及发放方式。

## 奖励条件检查

### 检查时机
- 每次被邀请人完成签退时
- 系统定时任务检查（可选）

### 检查条件
1. 是否在有效期内（注册后 N 天内）
2. 打工次数是否达标
3. 收入是否达标

### 检查逻辑

```sql
-- 获取被邀请人信息
SELECT referee_id, bound_at FROM referral_record WHERE referee_id = ?;

-- 获取配置
SELECT config_value FROM referral_config WHERE config_key IN ('valid_days', 'min_work_count', 'min_income');

-- 检查是否在有效期内
SELECT DATEDIFF(NOW(), bound_at) <= valid_days FROM referral_record WHERE referee_id = ?;

-- 检查打工次数
SELECT COUNT(*) >= min_work_count 
FROM attendance_record 
WHERE worker_id = ? AND status = 'COMPLETED' 
AND clock_out_time >= bound_at;

-- 检查收入
SELECT SUM(amount) >= min_income 
FROM balance_transaction 
WHERE worker_id = ? AND type = 'WORK_INCOME' 
AND created_at >= bound_at;
```

## 奖励状态流转

```
PENDING（待审核）
    ↓
AUDITING（审核中）  ← 如果 need_audit = true
    ↓
GRANTED（已发放）/ REJECTED（已拒绝）
    ↓
FAILED（发放失败）
```

## 奖励发放方式

### 自动发放（release_method = auto）
- 审核通过后（或无需审核时），自动将奖励金额添加到用户余额
- 在 `balance_transaction` 表创建收入记录

### 手动提现（release_method = manual）
- 审核通过后，用户需要手动提现
- 奖励记录状态变为 `GRANTED`
- 用户在收入页可以看到奖励金额，点击提现

## 接口设计

### 1. 获取我的邀请奖励列表

**请求**
```
GET /api/referral/rewards?pageNum=1&pageSize=10
```

**响应**
```json
{
  "code": 200,
  "message": "",
  "data": {
    "total": 2,
    "list": [
      {
        "id": 1,
        "refereeName": "张三",
        "refereePhone": "138****1234",
        "amount": 20.00,
        "status": "GRANTED",
        "createdAt": "2026-06-09 10:00:00",
        "grantedAt": "2026-06-09 15:00:00"
      }
    ]
  }
}
```

### 2. 平台端审核接口

**审核通过**
```
POST /api/referral/audit/{id}/approve
```

**审核拒绝**
```
POST /api/referral/audit/{id}/reject
```

请求：
```json
{
  "remark": "审核通过/拒绝原因"
}
```

## 数据库操作

### 创建奖励记录

```sql
-- 检查是否已创建奖励记录
SELECT id FROM referral_reward WHERE referral_record_id = ? AND status != 'FAILED';

-- 创建奖励记录
INSERT INTO referral_reward (referral_record_id, amount, status, created_at)
VALUES (?, ?, 'PENDING', NOW());
```

### 审核通过

```sql
-- 更新奖励状态
UPDATE referral_reward 
SET status = 'GRANTED', granted_at = NOW() 
WHERE id = ? AND status = 'AUDITING';

-- 如果发放方式为 auto，添加余额
UPDATE worker SET balance = balance + ? WHERE id = ?;

-- 创建余额交易记录
INSERT INTO balance_transaction (worker_id, amount, type, related_id, created_at)
VALUES (?, ?, 'REFERRAL_REWARD', ?, NOW());
```

### 审核拒绝

```sql
-- 更新奖励状态
UPDATE referral_reward 
SET status = 'REJECTED', audit_remark = ? 
WHERE id = ? AND status = 'AUDITING';
```

## 异常处理

| 异常 | 处理方式 |
|------|----------|
| 奖励记录不存在 | 返回错误：奖励记录不存在 |
| 奖励状态不正确 | 返回错误：当前状态无法操作 |
| 余额不足 | 返回错误：系统繁忙 |
| 数据库错误 | 事务回滚，返回错误 |

## 定时任务（可选）

- 检查审核时限，超时自动拒绝
- 检查有效期，超期自动标记失败
