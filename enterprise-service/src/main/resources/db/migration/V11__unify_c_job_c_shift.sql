-- Add columns to jobs for c_job merge
ALTER TABLE jobs
    ADD COLUMN company_name VARCHAR(200) DEFAULT NULL,
    ADD COLUMN company_logo VARCHAR(500) DEFAULT NULL,
    ADD COLUMN category_name VARCHAR(100) DEFAULT NULL,
    ADD COLUMN rate_type VARCHAR(20) DEFAULT NULL,
    ADD COLUMN rate_amount DECIMAL(10,2) DEFAULT NULL,
    ADD COLUMN schedule_info JSON DEFAULT NULL,
    ADD COLUMN published_at DATETIME DEFAULT NULL,
    ADD COLUMN accepted_count INT DEFAULT 0;

-- Add company_id to schedule_shifts for c_shift merge
ALTER TABLE schedule_shifts
    ADD COLUMN company_id BIGINT DEFAULT NULL,
    ADD INDEX idx_company_id (company_id);

-- Add worker_id and remark to attendance_records for c_attendance_record merge
ALTER TABLE attendance_records
    ADD COLUMN worker_id BIGINT DEFAULT NULL,
    ADD COLUMN remark VARCHAR(500) DEFAULT NULL,
    ADD INDEX idx_worker_id (worker_id);

-- Backfill jobs data from c_job
INSERT INTO jobs (id, company_id, title, description, location, province, city, district, address, latitude, longitude, category_id, headcount, status, deadline, created_at, updated_at, company_name, company_logo, category_name, rate_type, rate_amount, schedule_info, published_at, accepted_count)
SELECT cj.id, cj.company_id, cj.title, cj.description, cj.location, cj.province, cj.city, cj.district, cj.address, cj.latitude, cj.longitude, cj.category_id, COALESCE(cj.headcount, 0), cj.status, cj.deadline, cj.created_at, cj.updated_at, cj.company_name, cj.company_logo, cj.category_name, cj.rate_type, cj.rate_amount, cj.schedule_info, cj.published_at, 0
FROM c_job cj
ON DUPLICATE KEY UPDATE
    company_name = VALUES(company_name),
    company_logo = VALUES(company_logo),
    category_name = VALUES(category_name),
    rate_type = VALUES(rate_type),
    rate_amount = VALUES(rate_amount),
    schedule_info = VALUES(schedule_info),
    published_at = VALUES(published_at);

-- Backfill schedule_shifts data from c_shift
INSERT INTO schedule_shifts (job_id, worker_id, shift_date, start_time, end_time, location_name, location_lat, location_lng, location_radius, status, company_id, created_at, updated_at)
SELECT cs.job_id, cs.worker_id, cs.shift_date, cs.start_time, cs.end_time, cs.location_name, cs.location_lat, cs.location_lng, cs.location_radius, cs.status, cs.company_id, cs.created_at, cs.updated_at
FROM c_shift cs;

-- Backfill attendance_records data from c_attendance_record
INSERT INTO attendance_records (shift_id, worker_id, check_in_time, check_out_time, total_hours, status, remark, created_at, updated_at)
SELECT car.shift_id, car.worker_id, car.check_in_time, car.check_out_time, car.total_hours, car.status, car.remark, car.created_at, car.updated_at
FROM c_attendance_record car;

-- Drop old tables
DROP TABLE IF EXISTS c_attendance_record;
DROP TABLE IF EXISTS c_job;
DROP TABLE IF EXISTS c_shift;
