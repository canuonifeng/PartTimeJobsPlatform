-- Phase 1: Add gender and birthday to worker_profiles
ALTER TABLE worker_profiles ADD COLUMN gender VARCHAR(10) NULL COMMENT 'MALE/FEMALE/OTHER';
ALTER TABLE worker_profiles ADD COLUMN birthday DATE NULL;
