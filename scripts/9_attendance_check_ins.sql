-- 签到记录表（排班 1:N 签到）
CREATE TABLE IF NOT EXISTS attendance_check_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL COMMENT '排班ID',
    worker_id BIGINT NOT NULL COMMENT '兼职ID',
    check_in_time DATETIME NOT NULL COMMENT '签到时间',
    check_in_lat DECIMAL(10,7) COMMENT '签到纬度',
    check_in_lng DECIMAL(10,7) COMMENT '签到经度',
    late_seconds INT DEFAULT 0 COMMENT '迟到秒数，0表示未迟到',
    early_leave_seconds INT DEFAULT 0 COMMENT '早退秒数，0表示未早退',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_shift_id (shift_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_shift_worker (shift_id, worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表（支持多次签到签退）';
