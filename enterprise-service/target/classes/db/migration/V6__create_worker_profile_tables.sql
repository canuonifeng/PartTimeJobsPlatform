CREATE TABLE worker_blacklists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    reason VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_company_worker (company_id, worker_id),
    INDEX idx_company_id (company_id),
    INDEX idx_worker_id (worker_id)
);

CREATE TABLE worker_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    rating INT NOT NULL COMMENT '1-5',
    comment VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_company_id (company_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_job_id (job_id)
);
