ALTER TABLE c_withdrawal_record
    ADD COLUMN completed_at DATETIME DEFAULT NULL AFTER processed_at,
    ADD COLUMN third_party_serial_no VARCHAR(128) DEFAULT NULL COMMENT '第三方支付流水号',
    ADD COLUMN third_party_platform VARCHAR(50) DEFAULT NULL COMMENT '第三方支付平台';
