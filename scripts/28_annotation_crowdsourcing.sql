-- scripts/28_annotation_crowdsourcing.sql
-- 数据标注众包功能：jobs表扩展字段、job_schedules表扩展字段、新建annotation_task_orders表、新建external_worker_mapping表

SET @dbname = DATABASE();

-- ============================================================
-- 1. jobs 表扩展字段
-- ============================================================

-- task_type
SET @columnname = 'task_type';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, " VARCHAR(20) DEFAULT 'WORK' COMMENT 'WORK/ANNOTATION' AFTER accepted_count;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- pricing_mode
SET @columnname = 'pricing_mode';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, " VARCHAR(20) DEFAULT NULL COMMENT 'PER_ITEM/PER_PACKAGE' AFTER task_type;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- price_per_unit
SET @columnname = 'price_per_unit';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, ' DECIMAL(10,2) DEFAULT NULL COMMENT ''单价或套餐价'' AFTER pricing_mode;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- total_items
SET @columnname = 'total_items';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, ' INT DEFAULT NULL COMMENT ''标注任务总条数'' AFTER price_per_unit;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- external_task_id
SET @columnname = 'external_task_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, " VARCHAR(100) DEFAULT NULL COMMENT '外部系统任务ID' AFTER total_items;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- external_system_type
SET @columnname = 'external_system_type';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, " VARCHAR(50) DEFAULT NULL COMMENT '外部系统类型' AFTER external_task_id;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- auto_settle
SET @columnname = 'auto_settle';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'jobs' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE jobs ADD COLUMN ', @columnname, " TINYINT(1) DEFAULT 1 COMMENT '完成后自动结算' AFTER external_system_type;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================================
-- 2. job_schedules 表扩展字段
-- ============================================================

-- total_items
SET @columnname = 'total_items';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'job_schedules' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE job_schedules ADD COLUMN ', @columnname, ' INT DEFAULT NULL COMMENT ''本批次标注条数'' AFTER slots_available;')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- external_batch_id
SET @columnname = 'external_batch_id';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE table_schema = @dbname AND table_name = 'job_schedules' AND column_name = @columnname) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE job_schedules ADD COLUMN ', @columnname, " VARCHAR(100) DEFAULT NULL COMMENT '外部系统批次ID' AFTER total_items;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ============================================================
-- 3. 新建 annotation_task_orders 表
-- ============================================================
CREATE TABLE IF NOT EXISTS annotation_task_orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  application_id BIGINT NOT NULL COMMENT '报名ID（一个报名一个订单）',
  schedule_id BIGINT NOT NULL COMMENT '排班ID',
  worker_id BIGINT NOT NULL COMMENT '工人ID',
  job_id BIGINT NOT NULL COMMENT '岗位ID',
  items_completed INT DEFAULT 0 COMMENT '已完成条数',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/IN_PROGRESS/SUBMITTED/COMPLETED/REJECTED',
  external_submission_id VARCHAR(100) DEFAULT NULL COMMENT '外部提交ID（防重放）',
  submitted_at DATETIME DEFAULT NULL COMMENT '提交时间',
  completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
  settled_at DATETIME DEFAULT NULL COMMENT '结算时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_annotation_application (application_id),
  UNIQUE KEY uk_annotation_submission_id (external_submission_id),
  INDEX idx_annotation_worker (worker_id),
  INDEX idx_annotation_job (job_id),
  INDEX idx_annotation_schedule (schedule_id),
  INDEX idx_annotation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据标注任务订单';

-- ============================================================
-- 4. 新建 external_worker_mapping 表
-- ============================================================
CREATE TABLE IF NOT EXISTS external_worker_mapping (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_id BIGINT NOT NULL COMMENT '工人ID',
  external_system_type VARCHAR(50) NOT NULL COMMENT '外部系统类型',
  external_worker_id VARCHAR(100) NOT NULL COMMENT '外部系统工人ID',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ext_system_worker (external_system_type, external_worker_id),
  UNIQUE KEY uk_ext_system_local (external_system_type, worker_id),
  INDEX idx_ext_worker_id (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部系统工人映射';
