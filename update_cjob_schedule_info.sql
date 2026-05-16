UPDATE c_job
SET schedule_info = JSON_ARRAY(
    JSON_OBJECT('date', DATE_FORMAT(CURDATE(), '%Y-%m-%d'), 'startTime', '08:00', 'endTime', '12:00', 'slotsAvailable', 3),
    JSON_OBJECT('date', DATE_FORMAT(CURDATE(), '%Y-%m-%d'), 'startTime', '12:00', 'endTime', '16:00', 'slotsAvailable', 2),
    JSON_OBJECT('date', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 'startTime', '08:00', 'endTime', '12:00', 'slotsAvailable', 3),
    JSON_OBJECT('date', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 'startTime', '12:00', 'endTime', '16:00', 'slotsAvailable', 2)
)
WHERE job_id = 1;

UPDATE c_job
SET schedule_info = JSON_ARRAY(
    JSON_OBJECT('date', DATE_FORMAT(CURDATE(), '%Y-%m-%d'), 'startTime', '07:00', 'endTime', '11:00', 'slotsAvailable', 3),
    JSON_OBJECT('date', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), 'startTime', '07:00', 'endTime', '11:00', 'slotsAvailable', 3)
)
WHERE job_id = 2;
