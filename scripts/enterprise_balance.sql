CREATE TABLE IF NOT EXISTS enterprise_balances (
    company_id    BIGINT PRIMARY KEY,
    balance       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_top_up  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_spent   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    credit_limit  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at    DATETIME,
    updated_at    DATETIME
);

CREATE TABLE IF NOT EXISTS enterprise_balance_transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id       BIGINT NOT NULL,
    amount           DECIMAL(12,2) NOT NULL COMMENT 'positive=income, negative=expense',
    type             VARCHAR(20) NOT NULL COMMENT 'TOP_UP / SETTLEMENT / SETTLEMENT_REFUND',
    related_bill_id  BIGINT,
    related_top_up_id BIGINT,
    description      VARCHAR(255),
    created_at       DATETIME,
    INDEX idx_company_id (company_id),
    INDEX idx_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS enterprise_top_up_records (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id           BIGINT NOT NULL,
    amount               DECIMAL(12,2) NOT NULL,
    status               VARCHAR(20) DEFAULT 'PROCESSING' COMMENT 'PROCESSING/COMPLETED/FAILED',
    serial_number        VARCHAR(64),
    third_party_serial_no VARCHAR(128),
    third_party_platform  VARCHAR(50),
    completed_at         DATETIME,
    created_at           DATETIME,
    updated_at           DATETIME,
    INDEX idx_company_id (company_id)
);
