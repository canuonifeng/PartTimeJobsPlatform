-- scripts/18_add_requested_at_to_withdrawal_record.sql

-- 添加申请时间字段
ALTER TABLE c_withdrawal_record ADD COLUMN requested_at DATETIME COMMENT '申请时间';
