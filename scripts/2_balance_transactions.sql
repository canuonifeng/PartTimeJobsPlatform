CREATE TABLE IF NOT EXISTS balance_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL COMMENT '变动金额（正=收入，负=支出）',
    type VARCHAR(20) NOT NULL COMMENT 'EARNINGS-结算收入, WITHDRAWAL-提现支出',
    related_bill_id BIGINT DEFAULT NULL COMMENT '关联结算账单ID',
    related_withdrawal_id BIGINT DEFAULT NULL COMMENT '关联提现记录ID',
    description VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
