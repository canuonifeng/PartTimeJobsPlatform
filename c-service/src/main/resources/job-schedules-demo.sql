-- 岗位排班模拟数据 (job_schedules)
-- 先清空已有数据再插入

DELETE FROM job_schedules WHERE job_id IN (1,2,3,4,5,6,7,8,9,10);

-- Job 1: 餐厅服务员 (午/晚班)
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(1, '2026-05-21', '09:00', '14:00', 3),
(1, '2026-05-21', '17:00', '22:00', 2),
(1, '2026-05-22', '09:00', '14:00', 3),
(1, '2026-05-22', '17:00', '22:00', 2),
(1, '2026-05-23', '09:00', '14:00', 3);

-- Job 2: 洗碗工A (早班)
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(2, '2026-05-21', '08:00', '12:00', 2),
(2, '2026-05-22', '08:00', '12:00', 2),
(2, '2026-05-23', '08:00', '12:00', 2),
(2, '2026-05-24', '08:00', '12:00', 2);

-- Job 3: 传菜员 (午/晚班)
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(3, '2026-05-21', '10:00', '14:00', 3),
(3, '2026-05-21', '17:00', '21:00', 2),
(3, '2026-05-22', '10:00', '14:00', 3),
(3, '2026-05-22', '17:00', '21:00', 2);

-- Job 4: 外卖配送员 (灵活时段)
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(4, '2026-05-21', '10:00', '14:00', 5),
(4, '2026-05-21', '16:00', '20:00', 5),
(4, '2026-05-22', '10:00', '14:00', 5),
(4, '2026-05-22', '16:00', '20:00', 5),
(4, '2026-05-23', '10:00', '14:00', 5);

-- Job 5: 仓库分拣员 (早/晚班)
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(5, '2026-05-21', '06:00', '10:00', 3),
(5, '2026-05-21', '18:00', '22:00', 3),
(5, '2026-05-22', '06:00', '10:00', 3),
(5, '2026-05-22', '18:00', '22:00', 3),
(5, '2026-05-23', '06:00', '10:00', 3);

-- Job 6: 家庭保洁员
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(6, '2026-05-24', '09:00', '12:00', 1),
(6, '2026-05-24', '14:00', '17:00', 1),
(6, '2026-05-25', '09:00', '12:00', 1);

-- Job 7: 家电清洗师
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(7, '2026-05-24', '09:00', '12:00', 1),
(7, '2026-05-24', '14:00', '17:00', 1),
(7, '2026-05-25', '09:00', '12:00', 1);

-- Job 8: 兼职家教(小学)
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(8, '2026-05-24', '09:00', '11:00', 1),
(8, '2026-05-24', '14:00', '16:00', 1),
(8, '2026-05-25', '09:00', '11:00', 1),
(8, '2026-05-25', '14:00', '16:00', 1);

-- Job 9: 课程顾问
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(9, '2026-05-24', '09:00', '12:00', 3),
(9, '2026-05-24', '13:00', '18:00', 3),
(9, '2026-05-25', '09:00', '12:00', 3),
(9, '2026-05-25', '13:00', '18:00', 3);

-- Job 10: 周末帮厨
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(10, '2026-05-24', '08:00', '12:00', 2),
(10, '2026-05-24', '13:00', '17:00', 2),
(10, '2026-05-25', '08:00', '12:00', 2),
(10, '2026-05-25', '13:00', '17:00', 2);
