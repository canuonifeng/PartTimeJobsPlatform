-- 邀请码表
CREATE TABLE IF NOT EXISTS referral_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    code VARCHAR(8) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_worker (worker_id)
);

-- 邀请记录表
CREATE TABLE IF NOT EXISTS referral_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referrer_id BIGINT NOT NULL,
    referee_id BIGINT NOT NULL,
    referral_code VARCHAR(8) NOT NULL,
    bound_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_referee (referee_id),
    INDEX idx_referrer_id (referrer_id)
);

-- 邀请奖励表
CREATE TABLE IF NOT EXISTS referral_reward (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referral_record_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'AUDITING', 'GRANTED', 'REJECTED', 'FAILED') DEFAULT 'PENDING',
    audit_remark VARCHAR(200),
    granted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_referral_record_id (referral_record_id),
    INDEX idx_status (status)
);

-- 奖励规则配置表
CREATE TABLE IF NOT EXISTS referral_config (
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
