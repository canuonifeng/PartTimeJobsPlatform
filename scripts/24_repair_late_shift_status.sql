-- 修复旧逻辑导致的迟到排班状态丢失：迟到签退后被覆盖为 COMPLETED。
-- 执行前先运行预览 SELECT，确认影响范围后再执行 UPDATE。

-- 1. 预览将被修复的数据
SELECT
    s.id AS shift_id,
    s.worker_id,
    s.shift_date,
    s.start_time,
    s.end_time,
    s.status AS shift_status,
    a.id AS attendance_record_id,
    a.check_in_time,
    a.check_out_time,
    a.status AS attendance_status
FROM schedule_shifts s
JOIN attendance_records a ON a.shift_id = s.id
WHERE s.status = 'COMPLETED'
  AND a.status = 'COMPLETED'
  AND a.check_in_time IS NOT NULL
  AND TIMESTAMP(a.check_in_time) > TIMESTAMP(s.shift_date, s.start_time);

-- 2. 修复迟到状态
UPDATE schedule_shifts s
JOIN attendance_records a ON a.shift_id = s.id
SET
    s.status = 'LATE',
    a.status = 'LATE',
    s.updated_at = NOW(),
    a.updated_at = NOW()
WHERE s.status = 'COMPLETED'
  AND a.status = 'COMPLETED'
  AND a.check_in_time IS NOT NULL
  AND TIMESTAMP(a.check_in_time) > TIMESTAMP(s.shift_date, s.start_time);

-- 3. 验证剩余异常数量，返回 0 表示修复完成
SELECT COUNT(*) AS remaining_wrong_completed_late
FROM schedule_shifts s
JOIN attendance_records a ON a.shift_id = s.id
WHERE s.status = 'COMPLETED'
  AND a.status = 'COMPLETED'
  AND a.check_in_time IS NOT NULL
  AND TIMESTAMP(a.check_in_time) > TIMESTAMP(s.shift_date, s.start_time);
