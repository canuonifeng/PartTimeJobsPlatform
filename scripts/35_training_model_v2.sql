-- scripts/35_training_model_v2.sql
-- 培训模块 v2 数据库迁移：课程 -> 多课时 -> 题库/考试规则 三层模型
-- 新建 training_lessons、question_banks、question_bank_questions、worker_lesson_records
-- 废弃 v1 课程旧列（content/exam_json/pass_score）与旧学习记录表 worker_training_records

-- ============================================================
-- 1. 新建 training_lessons 课时表
-- ============================================================
CREATE TABLE IF NOT EXISTS training_lessons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT NOT NULL COMMENT '所属课程ID',
  lesson_type VARCHAR(20) NOT NULL COMMENT 'VIDEO/AUDIO/DOCUMENT/IMAGE_TEXT/EXAM',
  title VARCHAR(200) NOT NULL COMMENT '课时标题',
  content MEDIUMTEXT COMMENT '文档/图文正文',
  media_url VARCHAR(500) DEFAULT NULL COMMENT '音视频/图片地址',
  duration_minutes INT DEFAULT NULL COMMENT '预计时长（分钟）',
  exam_config_json TEXT COMMENT '考试配置 JSON',
  sort_order INT DEFAULT 0 COMMENT '排序号',
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_lesson_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训课时';

-- ============================================================
-- 2. 新建 question_banks 题库表
-- ============================================================
CREATE TABLE IF NOT EXISTS question_banks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL COMMENT '题库名称',
  description VARCHAR(500) DEFAULT NULL COMMENT '题库描述',
  status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_bank_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试题库';

-- ============================================================
-- 3. 新建 question_bank_questions 题目表
-- ============================================================
CREATE TABLE IF NOT EXISTS question_bank_questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bank_id BIGINT NOT NULL COMMENT '所属题库ID',
  question_type VARCHAR(20) NOT NULL COMMENT 'SINGLE_CHOICE/MULTIPLE_CHOICE/JUDGE',
  stem TEXT NOT NULL COMMENT '题干',
  options_json TEXT COMMENT '选项 JSON',
  answer TEXT NOT NULL COMMENT '正确答案',
  analysis TEXT COMMENT '解析',
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  sort_order INT DEFAULT 0 COMMENT '排序号',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_q_bank (bank_id),
  KEY idx_q_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库题目';

-- ============================================================
-- 4. 新建 worker_lesson_records 工人课时学习记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS worker_lesson_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_id BIGINT NOT NULL COMMENT '工人ID',
  lesson_id BIGINT NOT NULL COMMENT '课时ID',
  status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS/COMPLETED/FAILED',
  progress INT DEFAULT 0 COMMENT '学习进度（百分比）',
  score INT DEFAULT NULL COMMENT '考试得分',
  exam_snapshot_json MEDIUMTEXT COMMENT '考试快照 JSON',
  exam_attempts INT DEFAULT 0 COMMENT '考试尝试次数',
  started_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始学习时间',
  completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_worker_lesson (worker_id, lesson_id),
  KEY idx_record_worker (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工人课时学习记录';

-- ============================================================
-- 5. 废弃 v1 课程旧列（可重复执行，列不存在时跳过）
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'part_time_work' AND TABLE_NAME = 'training_courses' AND COLUMN_NAME = 'content');
SET @sql = IF(@col_exists > 0,
  'ALTER TABLE training_courses DROP COLUMN content',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'part_time_work' AND TABLE_NAME = 'training_courses' AND COLUMN_NAME = 'exam_json');
SET @sql = IF(@col_exists > 0,
  'ALTER TABLE training_courses DROP COLUMN exam_json',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'part_time_work' AND TABLE_NAME = 'training_courses' AND COLUMN_NAME = 'pass_score');
SET @sql = IF(@col_exists > 0,
  'ALTER TABLE training_courses DROP COLUMN pass_score',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 6. 废弃 v1 旧学习记录表
-- ============================================================
DROP TABLE IF EXISTS worker_training_records;
