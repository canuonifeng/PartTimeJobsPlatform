CREATE TABLE IF NOT EXISTS c_worker (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    wechat_code VARCHAR(100) UNIQUE,
    open_id VARCHAR(200) UNIQUE,
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_wechat_code (wechat_code),
    INDEX idx_open_id (open_id)
);

CREATE TABLE IF NOT EXISTS c_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL COMMENT 'References enterprise job',
    company_id BIGINT NOT NULL,
    company_name VARCHAR(200),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(200),
    category_id BIGINT,
    category_name VARCHAR(100),
    rate_type VARCHAR(20),
    rate_amount DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    published_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS c_job_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_worker_job (worker_id, job_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_job_id (job_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS c_shift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    company_id BIGINT,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location_name VARCHAR(200),
    location_lat DECIMAL(10,7),
    location_lng DECIMAL(10,7),
    location_radius INT,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_job_id (job_id),
    INDEX idx_status (status),
    INDEX idx_date (shift_date)
);

CREATE TABLE IF NOT EXISTS c_attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    check_in_time DATETIME,
    check_out_time DATETIME,
    total_hours DECIMAL(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_shift_id (shift_id),
    INDEX idx_worker_id (worker_id)
);

CREATE TABLE IF NOT EXISTS c_withdrawal_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    bank_info VARCHAR(500),
    remark VARCHAR(500),
    processed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS c_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    recipient_type VARCHAR(20) NOT NULL DEFAULT 'WORKER',
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recipient (recipient_id, recipient_type)
);
