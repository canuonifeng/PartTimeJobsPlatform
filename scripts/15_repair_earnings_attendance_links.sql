UPDATE balance_transactions bt
JOIN attendance_records ar ON ar.id = (
    SELECT ar2.id
    FROM attendance_records ar2
    LEFT JOIN schedule_shifts ss2 ON ar2.shift_id = ss2.id
    WHERE COALESCE(ar2.worker_id, ss2.worker_id) = bt.worker_id
      AND ss2.shift_date = REGEXP_SUBSTR(bt.description, '[0-9]{4}-[0-9]{2}-[0-9]{2}')
    ORDER BY ar2.settlement_status = 'PAID' DESC, ar2.id DESC
    LIMIT 1
)
SET bt.related_attendance_record_id = ar.id
WHERE bt.type = 'EARNINGS'
  AND bt.related_attendance_record_id IS NULL
  AND REGEXP_SUBSTR(bt.description, '[0-9]{4}-[0-9]{2}-[0-9]{2}') IS NOT NULL;
