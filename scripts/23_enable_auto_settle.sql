-- scripts/23_enable_auto_settle.sql
-- 将 auto_settle_attendance 默认改为 true，确保新老环境自动结算开启

UPDATE system_configs
SET config_value = 'true', updated_at = NOW()
WHERE config_key = 'auto_settle_attendance' AND config_value != 'true';
