SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'enterprise_accounts'
      AND COLUMN_NAME = 'phone'
);

SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE enterprise_accounts ADD COLUMN phone VARCHAR(30) NULL COMMENT ''联系电话'' AFTER display_name',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
