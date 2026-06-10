-- 企业邮箱后缀：enterprises 表新增 email_suffix 字段
ALTER TABLE enterprises ADD COLUMN email_suffix VARCHAR(100) DEFAULT NULL COMMENT '企业邮箱后缀' AFTER status;
CREATE UNIQUE INDEX uk_enterprises_email_suffix ON enterprises (email_suffix);
