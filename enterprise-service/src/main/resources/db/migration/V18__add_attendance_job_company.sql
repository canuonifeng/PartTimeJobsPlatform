ALTER TABLE attendance_records
    ADD COLUMN job_id BIGINT DEFAULT NULL COMMENT '岗位ID' AFTER shift_id,
    ADD COLUMN company_id BIGINT DEFAULT NULL COMMENT '企业ID' AFTER job_id,
    ADD INDEX idx_job_id (job_id),
    ADD INDEX idx_company_id (company_id);

-- backfill existing records
UPDATE attendance_records a
JOIN schedule_shifts s ON a.shift_id = s.id
SET a.job_id = s.job_id,
    a.company_id = s.company_id
WHERE a.job_id IS NULL;
