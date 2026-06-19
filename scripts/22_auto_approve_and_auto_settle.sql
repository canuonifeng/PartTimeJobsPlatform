-- scripts/22_auto_approve_and_auto_settle.sql
-- 报名自动审核 + 打卡自动结算 + 签到距离全局配置

INSERT IGNORE INTO system_configs (config_key, config_value, name, created_at, updated_at)
VALUES
  ('auto_approve_applications', 'false', '报名自动通过审核', NOW(), NOW()),
  ('auto_settle_attendance', 'true', '打卡签退后自动结算', NOW(), NOW()),
  ('check_in_radius_meters', '100', '签到最小距离（米）', NOW(), NOW());

ALTER TABLE jobs ADD COLUMN auto_approve TINYINT(1) DEFAULT NULL COMMENT '自动审核: null跟随平台默认, 1开启, 0关闭';
