-- Phase 3: Worker bank cards (single card per worker)
CREATE TABLE IF NOT EXISTS worker_bank_cards (
  id BIGINT NOT NULL AUTO_INCREMENT,
  worker_id BIGINT NOT NULL,
  card_holder VARCHAR(100) NOT NULL,
  card_number VARCHAR(32) NOT NULL,
  bank_name VARCHAR(100) NOT NULL,
  bank_branch VARCHAR(200),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_worker_id (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
