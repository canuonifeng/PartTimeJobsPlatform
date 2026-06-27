## Context

当前平台缺乏有效的用户增长机制。通过邀请奖励系统，可以激励老用户主动邀请新用户，实现低成本获客。

现有系统：
- 用户表 `worker` 已有 `id`、`phone`、`name` 等字段
- 无邀请相关表和逻辑

## Goals / Non-Goals

**Goals:**
- 实现邀请链接和海报生成
- 实现邀请关系自动绑定
- 实现奖励规则可配置
- 实现奖励自动发放
- 支持邀请统计查询

**Non-Goals:**
- 不实现多级邀请（只支持一级）
- 不实现企业端邀请（仅工人端）
- 不实现实时到账（走现有提现流程）

## Decisions

### 1. 邀请方式设计

**决策**：通过分享专属链接或海报进行邀请

**理由**：
- 链接分享便捷，支持微信、短信等渠道
- 海报更具传播性，适合朋友圈分享
- 无需手动填写邀请码，用户体验更好

**实现方式**：
- 邀请链接：`https://worker.example.com/invite?code=ABC123`
- 邀请海报：生成带用户昵称和二维码的图片

### 2. 奖励规则设计

**决策**：奖励规则可配置，由运营后台设置

**理由**：
- 灵活调整运营策略
- 不同阶段可设置不同奖励金额
- 便于 A/B 测试和效果评估

**配置项**：
- 有效天数：被邀请人注册后多少天内有效
- 奖励金额：邀请人获得的现金奖励
- 打工次数：被邀请人需要完成的最少打工次数
- 收入：被邀请人需要达到的最少收入（可选）
- 审核机制：是否需要平台审核（可配置）
- 发放方式：自动发放到余额或手动提现（可配置）

### 3. 奖励发放时机

**决策**：被邀请人在有效天数内完成规定打工次数后发放

**理由**：
- 确保新用户真正使用平台
- 避免注册即刷奖励
- 与现有考勤流程集成

**实现方式**：
- 每次签退时检查是否满足奖励条件
- 满足条件后自动创建奖励记录

### 4. 数据库设计

**决策**：新增 4 张表

```sql
-- 邀请码表
CREATE TABLE referral_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    code VARCHAR(8) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_worker (worker_id)
);

-- 邀请记录表
CREATE TABLE referral_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referrer_id BIGINT NOT NULL,
    referee_id BIGINT NOT NULL,
    referral_code VARCHAR(8) NOT NULL,
    bound_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_referee (referee_id)
);

-- 邀请奖励表
CREATE TABLE referral_reward (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referral_record_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'AUDITING', 'GRANTED', 'REJECTED', 'FAILED') DEFAULT 'PENDING',
    audit_remark VARCHAR(200),
    granted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 奖励规则配置表
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

### 5. 海报生成

**决策**：使用服务端生成海报图片

**理由**：
- 服务端生成保证图片质量
- 支持自定义海报模板
- 二维码使用第三方库生成

**备选方案**：
- 前端生成：兼容性差，小程序不支持
- 第三方服务：增加成本和依赖

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 邀请链接被恶意传播 | 限制每人最多邀请 50 人 |
| 奖励被重复发放 | 使用数据库事务 + 乐观锁 |
| 海报生成性能问题 | 使用缓存，避免重复生成 |
| 配置被误修改 | 配置修改记录日志，支持回滚 |
| 审核延迟 | 设置审核时限提醒 |

## Migration Plan

1. 执行数据库迁移脚本创建新表
2. 插入默认奖励规则配置
3. 部署后端服务
4. 部署前端应用
5. 监控奖励发放情况

## Open Questions

- 海报模板是否需要运营可配置？
- 奖励发放方式是否需要支持配置？
- 审核时限是否需要配置？
