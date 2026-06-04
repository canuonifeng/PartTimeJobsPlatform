SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS job_tag_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '标签组名称',
    code VARCHAR(50) NOT NULL COMMENT '标签组编码',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_tag_groups_code (code),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位标签组';

CREATE TABLE IF NOT EXISTS job_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL COMMENT '标签组ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    code VARCHAR(50) NOT NULL COMMENT '标签编码',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_tags_group_code (group_id, code),
    KEY idx_group_id (group_id),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位标签';

CREATE TABLE IF NOT EXISTS job_tag_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_tag_relations_job_tag (job_id, tag_id),
    KEY idx_job_id (job_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位标签关系';

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'description') > 0,
    'ALTER TABLE jobs MODIFY COLUMN description LONGTEXT NULL COMMENT ''岗位职责富文本''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'requirements') = 0,
    'ALTER TABLE jobs ADD COLUMN requirements LONGTEXT NULL COMMENT ''任职要求富文本''',
    'ALTER TABLE jobs MODIFY COLUMN requirements LONGTEXT NULL COMMENT ''任职要求富文本'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'contact_phone') = 0,
    'ALTER TABLE jobs ADD COLUMN contact_phone VARCHAR(30) NULL COMMENT ''岗位联系方式''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO job_tag_groups (name, code, sort_order, status)
SELECT '结算周期', 'settlement_cycle', 10, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM job_tag_groups WHERE code = 'settlement_cycle');

INSERT INTO job_tag_groups (name, code, sort_order, status)
SELECT '结算方式', 'settlement_method', 20, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM job_tag_groups WHERE code = 'settlement_method');

INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '日结', 'daily', 10, 'ACTIVE'
FROM job_tag_groups g
WHERE g.code = 'settlement_cycle'
  AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'daily');

INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '周结', 'weekly', 20, 'ACTIVE'
FROM job_tag_groups g
WHERE g.code = 'settlement_cycle'
  AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'weekly');

INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '月结', 'monthly', 30, 'ACTIVE'
FROM job_tag_groups g
WHERE g.code = 'settlement_cycle'
  AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'monthly');

INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '计件', 'piecework', 10, 'ACTIVE'
FROM job_tag_groups g
WHERE g.code = 'settlement_method'
  AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'piecework');

INSERT INTO job_tags (group_id, name, code, sort_order, status)
SELECT g.id, '按时', 'hourly', 20, 'ACTIVE'
FROM job_tag_groups g
WHERE g.code = 'settlement_method'
  AND NOT EXISTS (SELECT 1 FROM job_tags t WHERE t.group_id = g.id AND t.code = 'hourly');

UPDATE job_tag_groups SET name = '结算周期' WHERE code = 'settlement_cycle';
UPDATE job_tag_groups SET name = '结算方式' WHERE code = 'settlement_method';
UPDATE job_tags SET name = '日结' WHERE code = 'daily';
UPDATE job_tags SET name = '周结' WHERE code = 'weekly';
UPDATE job_tags SET name = '月结' WHERE code = 'monthly';
UPDATE job_tags SET name = '计件' WHERE code = 'piecework';
UPDATE job_tags SET name = '按时' WHERE code = 'hourly';
