-- 班次上下架状态
ALTER TABLE job_schedules ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE:上架,CANCELLED:取消';
-- 初始化已有数据
UPDATE job_schedules SET status = 'ACTIVE' WHERE status IS NULL;
-- 索引
CREATE INDEX idx_schedule_status ON job_schedules(status);
