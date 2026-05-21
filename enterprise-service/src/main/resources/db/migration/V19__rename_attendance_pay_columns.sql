ALTER TABLE attendance_records CHANGE COLUMN pay_amount scheduled_pay DECIMAL(10,2) DEFAULT NULL COMMENT '排班薪资';
ALTER TABLE attendance_records CHANGE COLUMN paid_amount payable_pay DECIMAL(10,2) DEFAULT NULL COMMENT '应付薪资';
