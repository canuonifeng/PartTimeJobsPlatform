-- =============================================
-- Phase3 评价管理：为工人与企业账号补充信用分字段
-- =============================================

ALTER TABLE c_worker ADD COLUMN credit_score INT NOT NULL DEFAULT 100 COMMENT '信用分，默认100';
ALTER TABLE enterprise_accounts ADD COLUMN credit_score INT NOT NULL DEFAULT 100 COMMENT '信用分，默认100';
