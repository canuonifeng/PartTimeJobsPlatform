-- 标注平台地址配置（工人端任务单跳转引导用）
INSERT INTO system_configs (config_key, config_value, name)
VALUES ('annotation_platform_url', 'https://annotation.example.com', '外部标注PC系统地址')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
