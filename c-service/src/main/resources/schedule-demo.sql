-- 岗位工作时段模拟数据
-- 格式: [{"id":1,"date":"2026-05-21","startTime":"09:00","endTime":"18:00","slotsAvailable":5}]

UPDATE jobs SET schedule_info = '[{"id":1,"date":"2026-05-21","startTime":"09:00","endTime":"14:00","slotsAvailable":3},{"id":2,"date":"2026-05-21","startTime":"17:00","endTime":"22:00","slotsAvailable":2},{"id":3,"date":"2026-05-22","startTime":"09:00","endTime":"14:00","slotsAvailable":3},{"id":4,"date":"2026-05-22","startTime":"17:00","endTime":"22:00","slotsAvailable":2},{"id":5,"date":"2026-05-23","startTime":"09:00","endTime":"14:00","slotsAvailable":3}]' WHERE id = 1;

UPDATE jobs SET schedule_info = '[{"id":6,"date":"2026-05-21","startTime":"08:00","endTime":"12:00","slotsAvailable":2},{"id":7,"date":"2026-05-22","startTime":"08:00","endTime":"12:00","slotsAvailable":2},{"id":8,"date":"2026-05-23","startTime":"08:00","endTime":"12:00","slotsAvailable":2},{"id":9,"date":"2026-05-24","startTime":"08:00","endTime":"12:00","slotsAvailable":2}]' WHERE id = 2;

UPDATE jobs SET schedule_info = '[{"id":10,"date":"2026-05-21","startTime":"10:00","endTime":"14:00","slotsAvailable":3},{"id":11,"date":"2026-05-21","startTime":"17:00","endTime":"21:00","slotsAvailable":2},{"id":12,"date":"2026-05-22","startTime":"10:00","endTime":"14:00","slotsAvailable":3},{"id":13,"date":"2026-05-22","startTime":"17:00","endTime":"21:00","slotsAvailable":2}]' WHERE id = 3;

UPDATE jobs SET schedule_info = '[{"id":14,"date":"2026-05-21","startTime":"10:00","endTime":"14:00","slotsAvailable":5},{"id":15,"date":"2026-05-21","startTime":"16:00","endTime":"20:00","slotsAvailable":5},{"id":16,"date":"2026-05-22","startTime":"10:00","endTime":"14:00","slotsAvailable":5},{"id":17,"date":"2026-05-22","startTime":"16:00","endTime":"20:00","slotsAvailable":5},{"id":18,"date":"2026-05-23","startTime":"10:00","endTime":"14:00","slotsAvailable":5}]' WHERE id = 4;

UPDATE jobs SET schedule_info = '[{"id":19,"date":"2026-05-21","startTime":"06:00","endTime":"10:00","slotsAvailable":3},{"id":20,"date":"2026-05-21","startTime":"18:00","endTime":"22:00","slotsAvailable":3},{"id":21,"date":"2026-05-22","startTime":"06:00","endTime":"10:00","slotsAvailable":3},{"id":22,"date":"2026-05-22","startTime":"18:00","endTime":"22:00","slotsAvailable":3},{"id":23,"date":"2026-05-23","startTime":"06:00","endTime":"10:00","slotsAvailable":3}]' WHERE id = 5;

UPDATE jobs SET schedule_info = '[{"id":24,"date":"2026-05-24","startTime":"09:00","endTime":"12:00","slotsAvailable":1},{"id":25,"date":"2026-05-24","startTime":"14:00","endTime":"17:00","slotsAvailable":1},{"id":26,"date":"2026-05-25","startTime":"09:00","endTime":"12:00","slotsAvailable":1}]' WHERE id = 6;

UPDATE jobs SET schedule_info = '[{"id":27,"date":"2026-05-24","startTime":"09:00","endTime":"12:00","slotsAvailable":1},{"id":28,"date":"2026-05-24","startTime":"14:00","endTime":"17:00","slotsAvailable":1},{"id":29,"date":"2026-05-25","startTime":"09:00","endTime":"12:00","slotsAvailable":1}]' WHERE id = 7;

UPDATE jobs SET schedule_info = '[{"id":30,"date":"2026-05-24","startTime":"09:00","endTime":"11:00","slotsAvailable":1},{"id":31,"date":"2026-05-24","startTime":"14:00","endTime":"16:00","slotsAvailable":1},{"id":32,"date":"2026-05-25","startTime":"09:00","endTime":"11:00","slotsAvailable":1},{"id":33,"date":"2026-05-25","startTime":"14:00","endTime":"16:00","slotsAvailable":1}]' WHERE id = 8;

UPDATE jobs SET schedule_info = '[{"id":34,"date":"2026-05-24","startTime":"09:00","endTime":"12:00","slotsAvailable":3},{"id":35,"date":"2026-05-24","startTime":"13:00","endTime":"18:00","slotsAvailable":3},{"id":36,"date":"2026-05-25","startTime":"09:00","endTime":"12:00","slotsAvailable":3},{"id":37,"date":"2026-05-25","startTime":"13:00","endTime":"18:00","slotsAvailable":3}]' WHERE id = 9;

UPDATE jobs SET schedule_info = '[{"id":38,"date":"2026-05-24","startTime":"08:00","endTime":"12:00","slotsAvailable":2},{"id":39,"date":"2026-05-24","startTime":"13:00","endTime":"17:00","slotsAvailable":2},{"id":40,"date":"2026-05-25","startTime":"08:00","endTime":"12:00","slotsAvailable":2},{"id":41,"date":"2026-05-25","startTime":"13:00","endTime":"17:00","slotsAvailable":2}]' WHERE id = 10;
