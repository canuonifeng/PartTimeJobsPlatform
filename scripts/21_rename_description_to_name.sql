-- 21_rename_description_to_name.sql
-- 将 system_configs 表的 description 字段重命名为 name

ALTER TABLE system_configs CHANGE COLUMN description name VARCHAR(500) DEFAULT NULL COMMENT '配置名称';
