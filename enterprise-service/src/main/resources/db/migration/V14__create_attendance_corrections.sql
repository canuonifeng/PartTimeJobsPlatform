CREATE TABLE IF NOT EXISTS attendance_corrections (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id      BIGINT       NOT NULL,
    worker_id     BIGINT       NOT NULL,
    reason        VARCHAR(500) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(500) DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at  DATETIME     DEFAULT NULL,
    processor_id  BIGINT       DEFAULT NULL,
    INDEX idx_shift_id (shift_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
