-- 新建排班报名表（替代job_applications + application_schedules）
CREATE TABLE IF NOT EXISTS schedule_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    worker_id BIGINT NOT NULL COMMENT '兼职ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/REJECTED',
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_worker_id (worker_id),
    UNIQUE KEY uk_schedule_worker (schedule_id, worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班报名记录';

-- 岗位表加关闭原因
ALTER TABLE jobs ADD COLUMN close_reason VARCHAR(500) NULL COMMENT '关闭原因';

-- 迁移已有数据（如果有的话）
-- INSERT INTO schedule_applications (schedule_id, worker_id, status, applied_at, updated_at)
-- SELECT as2.schedule_id, ja.worker_id, ja.status, ja.applied_at, ja.updated_at
-- FROM job_applications ja
-- JOIN application_schedules as2 ON ja.id = as2.application_id;

-- 删除旧表
DROP TABLE IF EXISTS application_schedules;
DROP TABLE IF EXISTS job_applications;
