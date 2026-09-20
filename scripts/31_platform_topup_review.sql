ALTER TABLE enterprise_top_up_records
  ADD COLUMN auditor VARCHAR(64) NULL COMMENT '审核人' AFTER status,
  ADD COLUMN audit_remark VARCHAR(500) NULL COMMENT '审核备注' AFTER auditor,
  ADD COLUMN audited_at DATETIME NULL COMMENT '审核时间' AFTER audit_remark;
