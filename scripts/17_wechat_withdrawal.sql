-- scripts/17_wechat_withdrawal.sql

-- 添加提现方式字段
ALTER TABLE c_withdrawal_record ADD COLUMN withdrawal_method VARCHAR(20) NOT NULL DEFAULT 'WECHAT' COMMENT '提现方式: WECHAT-微信零钱, BANK_CARD-银行卡';

-- 添加银行账户信息字段
ALTER TABLE c_withdrawal_record ADD COLUMN bank_account VARCHAR(100) COMMENT '银行账户信息';

-- 添加微信OpenID字段
ALTER TABLE c_withdrawal_record ADD COLUMN open_id VARCHAR(100) COMMENT '微信OpenID';

-- 添加索引
CREATE INDEX idx_withdrawal_worker_method ON c_withdrawal_record(worker_id, withdrawal_method);
CREATE INDEX idx_withdrawal_created_at ON c_withdrawal_record(created_at);
