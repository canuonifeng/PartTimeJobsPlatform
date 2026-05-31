-- Phase 2: Real-name authentication tables
CREATE TABLE IF NOT EXISTS worker_real_name_auth (
  id BIGINT NOT NULL AUTO_INCREMENT,
  worker_id BIGINT NOT NULL,
  real_name VARCHAR(100) NOT NULL,
  id_card_no VARCHAR(32) NOT NULL,
  id_card_front_url VARCHAR(500),
  id_card_back_url VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  reject_reason VARCHAR(500),
  submitted_at DATETIME NOT NULL,
  reviewed_at DATETIME,
  reviewer_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_worker_id (worker_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enterprise_real_name_auth (
  id BIGINT NOT NULL AUTO_INCREMENT,
  enterprise_id BIGINT NOT NULL,
  legal_person_name VARCHAR(100) NOT NULL,
  legal_person_id_card VARCHAR(32) NOT NULL,
  unified_social_credit_code VARCHAR(64) NOT NULL,
  business_license_url VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  reject_reason VARCHAR(500),
  submitted_at DATETIME NOT NULL,
  reviewed_at DATETIME,
  reviewer_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_enterprise_id (enterprise_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
