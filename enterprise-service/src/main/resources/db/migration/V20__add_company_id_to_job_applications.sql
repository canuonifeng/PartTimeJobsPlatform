ALTER TABLE job_applications ADD COLUMN company_id BIGINT NOT NULL AFTER job_id;
ALTER TABLE job_applications ADD INDEX idx_company_id (company_id);
