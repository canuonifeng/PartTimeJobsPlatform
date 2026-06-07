ALTER TABLE c_notification
    ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'system' AFTER type,
    ADD COLUMN is_read TINYINT(1) NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN related_type VARCHAR(50) DEFAULT NULL AFTER is_read,
    ADD COLUMN related_id BIGINT DEFAULT NULL AFTER related_type;

CREATE INDEX idx_c_notification_read ON c_notification (recipient_id, recipient_type, is_read);
CREATE INDEX idx_c_notification_related ON c_notification (related_type, related_id);
