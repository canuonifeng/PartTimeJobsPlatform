# 邀请关系绑定规范

## 概述

新用户通过邀请链接或海报注册时，自动绑定邀请关系。

## 绑定流程

```
用户点击邀请链接/扫描二维码
        ↓
跳转注册页面，URL 中携带邀请码
        ↓
用户完成注册
        ↓
系统验证邀请码有效性
        ↓
创建邀请记录（referral_record）
        ↓
邀请关系绑定成功
```

## 实现要点

### 1. 邀请码传递
- 注册页面 URL 携带 `code` 参数
- 前端存储邀请码到本地（localStorage）
- 注册时将邀请码发送给后端

### 2. 邀请码验证
- 验证邀请码是否存在
- 验证邀请码是否有效（未被禁用）
- 验证被邀请人是否已绑定其他邀请人

### 3. 邀请记录创建
- 使用数据库事务确保原子性
- 防止重复绑定
- 记录绑定时间

## 数据库操作

```sql
-- 检查邀请码是否存在
SELECT id, worker_id FROM referral_code WHERE code = ?;

-- 检查是否已绑定
SELECT id FROM referral_record WHERE referee_id = ?;

-- 创建邀请记录
INSERT INTO referral_record (referrer_id, referee_id, referral_code, bound_at)
VALUES (?, ?, ?, NOW());
```

## 接口设计

**绑定邀请关系**
```
POST /api/referral/bind
```

请求：
```json
{
  "code": "ABC12345"
}
```

响应：
```json
{
  "code": 200,
  "message": "绑定成功",
  "data": null
}
```

## 异常处理

| 异常 | 处理方式 |
|------|----------|
| 邀请码不存在 | 返回错误：邀请码无效 |
| 被邀请人已绑定 | 返回错误：您已绑定邀请人 |
| 邀请人邀请自己 | 返回错误：不能邀请自己 |
| 数据库错误 | 返回错误：系统繁忙，请稍后重试 |
