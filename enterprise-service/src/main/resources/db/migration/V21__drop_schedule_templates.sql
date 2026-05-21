ALTER TABLE schedule_shifts DROP COLUMN template_slot_id;

DROP TABLE IF EXISTS schedule_template_slots;

DROP TABLE IF EXISTS schedule_templates;
