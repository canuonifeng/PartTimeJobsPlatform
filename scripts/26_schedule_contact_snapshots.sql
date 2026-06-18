SET @schema_name = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'contact_name') = 0,
  'ALTER TABLE jobs ADD COLUMN contact_name VARCHAR(64) NULL COMMENT ''岗位联系人姓名'' AFTER requirements',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'job_schedules' AND COLUMN_NAME = 'schedule_name') = 0,
  'ALTER TABLE job_schedules ADD COLUMN schedule_name VARCHAR(128) NULL COMMENT ''班次名称'' AFTER end_time',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'job_schedules' AND COLUMN_NAME = 'contact_name') = 0,
  'ALTER TABLE job_schedules ADD COLUMN contact_name VARCHAR(64) NULL COMMENT ''联系人姓名快照'' AFTER slots_available',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'job_schedules' AND COLUMN_NAME = 'contact_phone') = 0,
  'ALTER TABLE job_schedules ADD COLUMN contact_phone VARCHAR(32) NULL COMMENT ''联系人电话快照'' AFTER contact_name',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'schedule_shifts' AND COLUMN_NAME = 'contact_name') = 0,
  'ALTER TABLE schedule_shifts ADD COLUMN contact_name VARCHAR(64) NULL COMMENT ''联系人姓名快照'' AFTER location_name',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'schedule_shifts' AND COLUMN_NAME = 'contact_phone') = 0,
  'ALTER TABLE schedule_shifts ADD COLUMN contact_phone VARCHAR(32) NULL COMMENT ''联系人电话快照'' AFTER contact_name',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE job_schedules js
JOIN jobs j ON j.id = js.job_id
SET js.contact_name = COALESCE(js.contact_name, j.contact_name),
    js.contact_phone = COALESCE(js.contact_phone, j.contact_phone),
    js.schedule_name = COALESCE(js.schedule_name, j.title);

UPDATE schedule_shifts ss
JOIN jobs j ON j.id = ss.job_id
SET ss.contact_name = COALESCE(ss.contact_name, j.contact_name),
    ss.contact_phone = COALESCE(ss.contact_phone, j.contact_phone);
