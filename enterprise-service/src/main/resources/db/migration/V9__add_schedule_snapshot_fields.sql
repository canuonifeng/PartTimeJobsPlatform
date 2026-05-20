ALTER TABLE schedule_shifts
    ADD COLUMN application_id BIGINT DEFAULT NULL COMMENT '报名ID快照',
    ADD COLUMN salary_type VARCHAR(20) DEFAULT NULL COMMENT '薪资类型快照',
    ADD COLUMN salary_amount DECIMAL(10,2) DEFAULT NULL COMMENT '薪资金额快照',
    ADD COLUMN salary_currency VARCHAR(10) DEFAULT NULL COMMENT '薪资币种快照';

ALTER TABLE attendance_records
    ADD COLUMN pay_amount DECIMAL(10,2) DEFAULT NULL COMMENT '应付金额',
    ADD COLUMN calculated_at DATETIME DEFAULT NULL COMMENT '结算计算时间';
