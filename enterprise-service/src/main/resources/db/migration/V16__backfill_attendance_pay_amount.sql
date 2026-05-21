-- backfill total_hours and pay_amount for existing records with null values
-- formula mirrors CheckOutServiceImpl: total_hours = (end_time - start_time), pay_amount = total_hours * rate (HOURLY) | rate (DAILY)

UPDATE attendance_records a
JOIN schedule_shifts s ON a.shift_id = s.id
SET a.total_hours = ROUND(TIMESTAMPDIFF(MINUTE, s.start_time, s.end_time) / 60, 2),
    a.pay_amount = CASE
        WHEN s.salary_type = 'HOURLY' AND s.salary_amount IS NOT NULL
            THEN ROUND(ROUND(TIMESTAMPDIFF(MINUTE, s.start_time, s.end_time) / 60, 2) * s.salary_amount, 2)
        WHEN (s.salary_type IS NULL OR s.salary_type = 'DAILY') AND s.salary_amount IS NOT NULL
            THEN s.salary_amount
        ELSE 0
    END
WHERE a.total_hours IS NULL OR a.pay_amount IS NULL;
