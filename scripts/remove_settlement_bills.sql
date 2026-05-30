ALTER TABLE balance_transactions
    ADD COLUMN related_attendance_record_id BIGINT DEFAULT NULL AFTER related_bill_id;

CREATE INDEX idx_related_attendance_record_id ON balance_transactions (related_attendance_record_id);

DROP TABLE IF EXISTS settlement_bills;
