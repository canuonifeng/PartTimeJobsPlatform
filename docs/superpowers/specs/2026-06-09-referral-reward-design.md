# 邀请奖励系统设计文档

## 1. 功能概述

### 1.1 目标
通过邀请奖励机制激励老用户邀请新用户注册，实现低成本获客。

### 1.2 核心功能

| 功能 | 说明 |
|------|------|
| 邀请方式 | 分享专属链接或海报 |
| 奖励对象 | 仅邀请人 |
| 奖励条件 | 打工次数 + 收入（可配置） |
| 奖励金额 | 全局统一，可配置 |
| 有效期 | 被邀请人注册后 N 天内（可配置） |
| 审核机制 | 可配置是否需要审核 |
| 发放方式 | 可配置自动发放或手动提现 |
| 邀请上限 | 不限制 |
| 数据展示 | 被邀请人列表（分页）、收入页合并显示 |

## 2. 用户角色

| 角色 | 权限 |
|------|------|
| 工人（邀请人） | 生成邀请链接/海报、查看被邀请人列表、查看奖励状态 |
| 工人（被邀请人） | 通过链接/海报注册、完成打工任务 |
| 平台运营 | 配置奖励规则、审核奖励发放 |

## 3. 数据库设计

### 3.1 邀请码表（referral_code）

```sql
CREATE TABLE referral_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    code VARCHAR(8) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_worker (worker_id)
);
```

### 3.2 邀请记录表（referral_record）

```sql
CREATE TABLE referral_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referrer_id BIGINT NOT NULL,
    referee_id BIGINT NOT NULL,
    referral_code VARCHAR(8) NOT NULL,
    bound_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_referee (referee_id)
);
```

### 3.3 邀请奖励表（referral_reward）

```sql
CREATE TABLE referral_reward (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referral_record_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'AUDITING', 'GRANTED', 'REJECTED', 'FAILED') DEFAULT 'PENDING',
    audit_remark VARCHAR(200),
    granted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 奖励规则配置表（referral_config）

```sql
CREATE TABLE referral_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value VARCHAR(200) NOT NULL,
    description VARCHAR(200),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 默认配置
INSERT INTO referral_config (config_key, config_value, description) VALUES
('need_audit', 'true', '是否需要审核：true-需要，false-不需要'),
('audit_days', '0', '审核时限（天），0表示无限制'),
('reward_amount', '20', '邀请人奖励金额（元）'),
('valid_days', '30', '被邀请人注册后有效天数'),
('min_work_count', '3', '被邀请人最少打工次数'),
('min_income', '0', '被邀请人最少收入（元），0表示不限制'),
('release_method', 'manual', '发放方式：auto-自动发放到余额，manual-手动提现');
```

## 4. 业务流程

### 4.1 邀请流程

```
邀请人分享链接/海报
        ↓
新用户点击链接/扫描二维码
        ↓
新用户注册，自动绑定邀请关系
        ↓
新用户打工，完成签退
        ↓
系统检查是否满足奖励条件：
1. 是否在有效期内
2. 打工次数是否达标
3. 收入是否达标
        ↓
条件满足，创建奖励记录
        ↓
如果需要审核：
    等待平台审核
    审核通过 → 发放奖励
    审核拒绝 → 通知用户
如果不需要审核：
    自动发放奖励
```

### 4.2 审核流程

```
奖励记录状态：PENDING → AUDITING → GRANTED/REJECTED
```

- PENDING：待审核
- AUDITING：审核中
- GRANTED：已发放
- REJECTED：已拒绝
- FAILED：发放失败

## 5. API 设计

### 5.1 c-service（工人端）

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/referral/link | GET | 获取邀请链接 |
| /api/referral/poster | GET | 获取邀请海报 |
| /api/referral/stats | GET | 邀请统计概览 |
| /api/referral/referees | GET | 被邀请人列表（分页） |
| /api/referral/rewards | GET | 我的邀请奖励列表 |
| /api/earnings/summary | GET | 收入汇总（包含邀请奖励） |

### 5.2 platform-service（平台端）

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/referral/config | GET | 获取奖励规则配置 |
| /api/referral/config | PUT | 修改奖励规则配置 |
| /api/referral/audit/list | GET | 待审核奖励列表 |
| /api/referral/audit/{id}/approve | POST | 审核通过 |
| /api/referral/audit/{id}/reject | POST | 审核拒绝 |

## 6. 前端页面

### 6.1 worker-uniapp

| 页面 | 功能 |
|------|------|
| 邀请页面 | 显示邀请链接、生成海报、分享功能 |
| 邀请记录页面 | 被邀请人列表（分页）、出勤状态、奖励状态 |
| 收入页 | 合并显示工资和邀请奖励 |

### 6.2 platform-pc

| 页面 | 功能 |
|------|------|
| 奖励规则配置页面 | 配置各项参数 |
| 奖励审核页面 | 待审核列表、审核操作 |

## 7. 配置项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| need_audit | true | 是否需要审核 |
| audit_days | 0 | 审核时限（天），0表示无限制 |
| reward_amount | 20 | 邀请人奖励金额（元） |
| valid_days | 30 | 被邀请人注册后有效天数 |
| min_work_count | 3 | 被邀请人最少打工次数 |
| min_income | 0 | 被邀请人最少收入（元），0表示不限制 |
| release_method | manual | 发放方式：auto-自动，manual-手动 |

## 8. 风险与缓解措施

| 风险 | 缓解措施 |
|------|----------|
| 邀请链接被恶意传播 | 限制每人最多邀请 50 人 |
| 奖励被重复发放 | 使用数据库事务 + 乐观锁 |
| 海报生成性能问题 | 使用缓存，避免重复生成 |
| 配置被误修改 | 配置修改记录日志，支持回滚 |
| 审核延迟 | 设置审核时限提醒 |

## 9. 待确认问题

- [ ] 奖励金额是否需要支持阶梯配置？
- [ ] 被邀请人是否也需要奖励？
- [ ] 海报模板是否需要运营可配置？
