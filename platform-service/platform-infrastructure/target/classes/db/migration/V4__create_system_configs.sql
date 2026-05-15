CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System configuration key-value store';

INSERT INTO system_configs (config_key, config_value, description) VALUES
('platform_fee_rate', '0.10', 'Platform service fee rate (e.g. 0.10 = 10%)'),
('max_job_categories', '50', 'Maximum number of job categories allowed');
