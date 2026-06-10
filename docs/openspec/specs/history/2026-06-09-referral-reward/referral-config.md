# 奖励规则配置规范

## 概述

平台运营可在后台配置奖励规则，包括审核机制、奖励金额、有效期等。

## 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| need_audit | boolean | true | 是否需要审核 |
| audit_days | int | 0 | 审核时限（天），0表示无限制 |
| reward_amount | decimal | 20 | 邀请人奖励金额（元） |
| valid_days | int | 30 | 被邀请人注册后有效天数 |
| min_work_count | int | 3 | 被邀请人最少打工次数 |
| min_income | decimal | 0 | 被邀请人最少收入（元），0表示不限制 |
| release_method | string | manual | 发放方式：auto-自动，manual-手动 |

## 接口设计

### 1. 获取配置

**请求**
```
GET /api/referral/config
```

**响应**
```json
{
  "code": 200,
  "message": "",
  "data": {
    "configs": [
      {
        "key": "need_audit",
        "value": "true",
        "description": "是否需要审核：true-需要，false-不需要"
      },
      {
        "key": "audit_days",
        "value": "0",
        "description": "审核时限（天），0表示无限制"
      },
      {
        "key": "reward_amount",
        "value": "20",
        "description": "邀请人奖励金额（元）"
      },
      {
        "key": "valid_days",
        "value": "30",
        "description": "被邀请人注册后有效天数"
      },
      {
        "key": "min_work_count",
        "value": "3",
        "description": "被邀请人最少打工次数"
      },
      {
        "key": "min_income",
        "value": "0",
        "description": "被邀请人最少收入（元），0表示不限制"
      },
      {
        "key": "release_method",
        "value": "manual",
        "description": "发放方式：auto-自动发放到余额，manual-手动提现"
      }
    ]
  }
}
```

### 2. 修改配置

**请求**
```
PUT /api/referral/config
```

```json
{
  "configs": [
    {
      "key": "need_audit",
      "value": "true"
    },
    {
      "key": "reward_amount",
      "value": "30"
    }
  ]
}
```

**响应**
```json
{
  "code": 200,
  "message": "配置已更新",
  "data": null
}
```

## 验证规则

| 配置项 | 验证规则 |
|--------|----------|
| need_audit | 只能为 true 或 false |
| audit_days | 必须为非负整数 |
| reward_amount | 必须为正数，最大 10000 |
| valid_days | 必须为正整数，最大 365 |
| min_work_count | 必须为非负整数，最大 100 |
| min_income | 必须为非负数，最大 100000 |
| release_method | 只能为 auto 或 manual |

## 前端页面（platform-pc）

### 奖励规则配置页面

- 表单展示所有配置项
- 实时验证输入
- 保存前确认
- 保存成功后刷新页面
