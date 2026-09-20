-- scripts/29_worker_training.sql
-- 培训认证模块：新建 training_certifications（技能认证）、training_courses（培训课程）、
-- worker_training_records（学习记录）、worker_certifications（工人持有认证）

-- ============================================================
-- 1. 新建 training_certifications 技能认证定义表
-- ============================================================
CREATE TABLE IF NOT EXISTS training_certifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL COMMENT '认证名称',
  code VARCHAR(50) NOT NULL COMMENT '认证编码',
  task_type VARCHAR(20) NOT NULL COMMENT '适用任务类型 WORK/ANNOTATION',
  description VARCHAR(500) DEFAULT NULL COMMENT '认证描述',
  valid_days INT DEFAULT NULL COMMENT '有效期天数，NULL=永久有效',
  status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_cert_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技能认证定义';

-- ============================================================
-- 2. 新建 training_courses 培训课程表
-- ============================================================
CREATE TABLE IF NOT EXISTS training_courses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  certification_id BIGINT NOT NULL COMMENT '关联技能认证ID',
  title VARCHAR(200) NOT NULL COMMENT '课程标题',
  summary VARCHAR(500) DEFAULT NULL COMMENT '课程摘要',
  content MEDIUMTEXT COMMENT '课程内容（Markdown/HTML）',
  exam_json TEXT COMMENT '考试题目 JSON，格式见设计文档',
  pass_score INT DEFAULT 60 COMMENT '及格分（百分制）',
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  sort_order INT DEFAULT 0 COMMENT '排序号',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_course_cert (certification_id),
  KEY idx_course_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训课程';

-- ============================================================
-- 3. 新建 worker_training_records 工人学习记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS worker_training_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_id BIGINT NOT NULL COMMENT '工人ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS/COMPLETED/FAILED',
  score INT DEFAULT NULL COMMENT '考试得分（百分制）',
  started_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始学习时间',
  completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_worker_course (worker_id, course_id),
  KEY idx_record_worker (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工人培训学习记录';

-- ============================================================
-- 4. 新建 worker_certifications 工人持有认证表
-- ============================================================
CREATE TABLE IF NOT EXISTS worker_certifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_id BIGINT NOT NULL COMMENT '工人ID',
  certification_id BIGINT NOT NULL COMMENT '认证ID',
  status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXPIRED',
  granted_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  expires_at DATETIME DEFAULT NULL COMMENT '过期时间，NULL=永久',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_worker_cert (worker_id, certification_id),
  KEY idx_cert_worker (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工人技能认证';
