CREATE TABLE schedule_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company_id (company_id)
);

CREATE TABLE schedule_template_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '1=MON, 7=SUN',
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_workers INT DEFAULT NULL,
    location_lat DECIMAL(10,7) DEFAULT NULL,
    location_lng DECIMAL(10,7) DEFAULT NULL,
    location_radius INT DEFAULT NULL COMMENT 'meters',
    location_name VARCHAR(255) DEFAULT NULL,
    FOREIGN KEY (template_id) REFERENCES schedule_templates(id),
    INDEX idx_template_id (template_id)
);

CREATE TABLE schedule_shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    template_slot_id BIGINT DEFAULT NULL,
    worker_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location_lat DECIMAL(10,7) DEFAULT NULL,
    location_lng DECIMAL(10,7) DEFAULT NULL,
    location_radius INT DEFAULT NULL,
    location_name VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/CHECKED_IN/CHECKED_OUT/ABSENT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_shift_date (shift_date)
);

CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    check_in_time DATETIME DEFAULT NULL,
    check_in_lat DECIMAL(10,7) DEFAULT NULL,
    check_in_lng DECIMAL(10,7) DEFAULT NULL,
    check_out_time DATETIME DEFAULT NULL,
    check_out_lat DECIMAL(10,7) DEFAULT NULL,
    check_out_lng DECIMAL(10,7) DEFAULT NULL,
    total_hours DECIMAL(5,2) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CHECKED_IN/CHECKED_OUT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shift_id) REFERENCES schedule_shifts(id),
    INDEX idx_shift_id (shift_id)
);
