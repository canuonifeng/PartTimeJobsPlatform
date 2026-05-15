CREATE TABLE worker_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(100) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    avatar_url VARCHAR(500) DEFAULT NULL,
    skills TEXT DEFAULT NULL,
    available_days TEXT DEFAULT NULL COMMENT 'JSON array of available days',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id)
);

CREATE TABLE worker_resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id)
);
