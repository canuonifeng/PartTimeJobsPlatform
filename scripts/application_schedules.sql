CREATE TABLE IF NOT EXISTS application_schedules (
    application_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    PRIMARY KEY (application_id, schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
