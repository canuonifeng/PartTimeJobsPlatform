CREATE TABLE IF NOT EXISTS company_locations (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id  BIGINT       NOT NULL,
  name        VARCHAR(100) COMMENT '地点名称',
  province    VARCHAR(50),
  city        VARCHAR(50),
  district    VARCHAR(50),
  address     VARCHAR(255) COMMENT '详细地址',
  latitude    DECIMAL(10,7),
  longitude   DECIMAL(10,7),
  status      VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED / DISABLED',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
