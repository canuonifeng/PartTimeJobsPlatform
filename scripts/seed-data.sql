-- ========================================
-- 零工平台演示数据
-- ========================================

SET NAMES utf8mb4;

-- 清空所有业务表（按外键顺序）
DELETE FROM c_attendance_record; DELETE FROM c_withdrawal_record; DELETE FROM c_notification;
DELETE FROM c_shift; DELETE FROM c_job_application; DELETE FROM worker_resumes;
DELETE FROM worker_profiles; DELETE FROM c_worker; DELETE FROM c_job;
DELETE FROM attendance_records; DELETE FROM payroll_items; DELETE FROM payroll_batches;
DELETE FROM notification_logs; DELETE FROM withdrawal_records; DELETE FROM worker_evaluations;
DELETE FROM worker_blacklists; DELETE FROM schedule_shifts; DELETE FROM job_applications;
DELETE FROM job_schedules; DELETE FROM job_rates; DELETE FROM jobs;
DELETE FROM schedule_template_slots; DELETE FROM schedule_templates;
DELETE FROM job_categories; DELETE FROM enterprise_registrations;
DELETE FROM job_reports; DELETE FROM system_configs;

-- ========================
-- 1. 岗位分类 (job_categories)
-- ========================
INSERT INTO job_categories (id, name, parent_id, sort_order) VALUES
(1, '餐饮服务', NULL, 1),
(2, '物流配送', NULL, 2),
(3, '家政保洁', NULL, 3),
(4, '活动促销', NULL, 4),
(5, '教育培训', NULL, 5),
(6, '美容美发', NULL, 6),
(7, '其他', NULL, 7);

-- ========================
-- 2. 系统配置 (system_configs)
-- ========================
INSERT INTO system_configs (config_key, config_value, description) VALUES
('platform_fee_rate', '0.10', '平台服务费率'),
('max_job_categories', '50', '最大岗位分类数'),
('worker_min_age', '18', '工人最小年龄'),
('withdraw_min_amount', '50.00', '最低提现金额');

-- ========================
-- 3. 企业入驻 (enterprise_registrations)
-- ========================
INSERT INTO enterprise_registrations (company_name, contact_name, contact_phone, company_address, business_license, status, reviewer_id, review_remark, reviewed_at) VALUES
('美味餐饮管理有限公司', '张三', '13800138001', '北京市朝阳区建国路88号', 'BL20230001', 'APPROVED', 'admin', '审核通过', NOW()),
('极速物流配送有限公司', '李四', '13800138002', '上海市浦东新区陆家嘴路100号', 'BL20230002', 'APPROVED', 'admin', '审核通过', NOW()),
('洁新家政服务有限公司', '王五', '13800138003', '广州市天河区天河路200号', 'BL20230003', 'APPROVED', 'admin', '审核通过', NOW()),
('卓越教育培训中心', '赵六', '13800138004', '深圳市南山区科技园路300号', 'BL20230004', 'APPROVED', 'admin', '审核通过', NOW()),
('时尚美容美发连锁', '钱七', '13800138005', '杭州市西湖区文三路400号', 'BL20230005', 'PENDING', NULL, NULL, NULL);

-- ========================
-- 4. 企业岗位 (jobs)
-- ========================
INSERT INTO jobs (id, company_id, title, description, location, category_id, headcount, status, deadline) VALUES
(1, 1, '餐厅服务员', '负责餐厅日常接待、点餐、上菜等工作，工作环境好，包工作餐。', '北京市朝阳区建国路88号', 1, 10, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, 1, '洗碗工', '负责餐厅餐具清洗消毒，工作简单，时间灵活。', '北京市朝阳区建国路88号', 1, 5, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 20 DAY)),
(3, 1, '传菜员', '负责将菜品从厨房传送到餐桌，配合服务员工作。', '北京市朝阳区建国路88号', 1, 8, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 25 DAY)),
(4, 2, '外卖配送员', '负责区域内外卖订单配送，自备电动车优先，按单计酬。', '上海市浦东新区陆家嘴路100号', 2, 20, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 15 DAY)),
(5, 2, '仓库分拣员', '负责仓库内货物分拣、打包，夜班为主，补贴高。', '上海市浦东新区陆家嘴路100号', 2, 15, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(6, 3, '家庭保洁员', '负责家庭日常保洁，按小时计费，时间自由。', '广州市天河区天河路200号', 3, 12, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 60 DAY)),
(7, 3, '家电清洗师', '负责空调、油烟机等家电清洗，提供培训。', '广州市天河区天河路200号', 3, 6, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 45 DAY)),
(8, 4, '兼职家教(小学)', '辅导小学生语数外作业，要求有耐心，师范生优先。', '深圳市南山区科技园路300号', 5, 5, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 90 DAY)),
(9, 4, '课程顾问', '负责课程咨询和推广，底薪+提成，沟通能力强。', '深圳市南山区科技园路300号', 5, 3, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 30 DAY)),
(10, 1, '周末帮厨', '周末厨房帮工，协助厨师准备食材，简单易上手。', '北京市朝阳区建国路88号', 1, 4, 'PUBLISHED', DATE_ADD(NOW(), INTERVAL 14 DAY));

-- ========================
-- 5. 岗位薪资规则 (job_rates)
-- ========================
INSERT INTO job_rates (job_id, type, amount, rules) VALUES
(1, 'HOURLY', 25.00, NULL),
(2, 'HOURLY', 20.00, NULL),
(3, 'HOURLY', 22.00, NULL),
(4, 'PIECE', 5.00, '{"per_order": true, "min_orders_per_day": 10}'),
(5, 'HOURLY', 28.00, '{"night_shift_bonus": 5}'),
(6, 'HOURLY', 35.00, NULL),
(7, 'HOURLY', 40.00, '{"training_provided": true}'),
(8, 'HOURLY', 60.00, NULL),
(9, 'MONTHLY', 3500.00, '{"commission_rate": 0.05}'),
(10, 'HOURLY', 22.00, NULL);

-- ========================
-- 6. 排班模板 (schedule_templates)
-- ========================
INSERT INTO schedule_templates (id, company_id, name, description) VALUES
(1, 1, '餐饮部标准排班', '餐饮部早中晚三班标准排班模板'),
(2, 2, '物流配送排班', '物流配送早晚班排班模板'),
(3, 3, '家政服务排班', '家政服务日常排班模板');

INSERT INTO schedule_template_slots (template_id, day_of_week, start_time, end_time, max_workers, location_name) VALUES
(1, 1, '08:00:00', '12:00:00', 5, '建国路88号1楼'),
(1, 1, '12:00:00', '16:00:00', 5, '建国路88号1楼'),
(1, 1, '17:00:00', '21:00:00', 5, '建国路88号1楼'),
(1, 2, '08:00:00', '12:00:00', 5, '建国路88号1楼'),
(1, 2, '12:00:00', '16:00:00', 5, '建国路88号1楼'),
(1, 2, '17:00:00', '21:00:00', 5, '建国路88号1楼'),
(1, 3, '08:00:00', '12:00:00', 5, '建国路88号1楼'),
(1, 3, '12:00:00', '16:00:00', 5, '建国路88号1楼'),
(1, 3, '17:00:00', '21:00:00', 5, '建国路88号1楼'),
(1, 4, '08:00:00', '12:00:00', 5, '建国路88号1楼'),
(1, 4, '12:00:00', '16:00:00', 5, '建国路88号1楼'),
(1, 4, '17:00:00', '21:00:00', 5, '建国路88号1楼'),
(1, 5, '08:00:00', '12:00:00', 5, '建国路88号1楼'),
(1, 5, '12:00:00', '16:00:00', 5, '建国路88号1楼'),
(1, 5, '17:00:00', '21:00:00', 5, '建国路88号1楼'),
(1, 6, '09:00:00', '14:00:00', 4, '建国路88号1楼'),
(1, 6, '17:00:00', '22:00:00', 4, '建国路88号1楼'),
(1, 7, '09:00:00', '14:00:00', 3, '建国路88号1楼'),
(1, 7, '17:00:00', '22:00:00', 3, '建国路88号1楼'),
(2, 1, '07:00:00', '12:00:00', 10, '陆家嘴路100号仓库'),
(2, 1, '13:00:00', '18:00:00', 10, '陆家嘴路100号仓库'),
(2, 2, '07:00:00', '12:00:00', 10, '陆家嘴路100号仓库'),
(2, 2, '13:00:00', '18:00:00', 10, '陆家嘴路100号仓库'),
(2, 3, '07:00:00', '12:00:00', 10, '陆家嘴路100号仓库'),
(2, 3, '13:00:00', '18:00:00', 10, '陆家嘴路100号仓库'),
(2, 4, '07:00:00', '12:00:00', 10, '陆家嘴路100号仓库'),
(2, 4, '13:00:00', '18:00:00', 10, '陆家嘴路100号仓库'),
(2, 5, '07:00:00', '12:00:00', 10, '陆家嘴路100号仓库'),
(2, 5, '13:00:00', '18:00:00', 10, '陆家嘴路100号仓库'),
(3, 1, '08:00:00', '12:00:00', 5, '天河区'),
(3, 1, '13:00:00', '17:00:00', 5, '天河区'),
(3, 2, '08:00:00', '12:00:00', 5, '天河区'),
(3, 2, '13:00:00', '17:00:00', 5, '天河区'),
(3, 3, '08:00:00', '12:00:00', 5, '天河区'),
(3, 3, '13:00:00', '17:00:00', 5, '天河区'),
(3, 4, '08:00:00', '12:00:00', 5, '天河区'),
(3, 4, '13:00:00', '17:00:00', 5, '天河区'),
(3, 5, '08:00:00', '12:00:00', 5, '天河区'),
(3, 5, '13:00:00', '17:00:00', 5, '天河区');

-- ========================
-- 7. 岗位排班 (job_schedules)
-- ========================
INSERT INTO job_schedules (job_id, schedule_date, start_time, end_time, slots_available) VALUES
(1, CURDATE(), '08:00:00', '12:00:00', 3),
(1, CURDATE(), '12:00:00', '16:00:00', 2),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', 3),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '12:00:00', '16:00:00', 2),
(2, CURDATE(), '07:00:00', '11:00:00', 3),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '11:00:00', 3);

-- ========================
-- 8. C端: 工人 (c_worker)
-- ========================
INSERT INTO c_worker (id, name, phone, wechat_code, open_id, avatar_url, status) VALUES
(1, '小明', '13900139001', 'wechat_xiaoming', 'openid_xiaoming', NULL, 'ACTIVE'),
(2, '小红', '13900139002', 'wechat_xiaohong', 'openid_xiaohong', NULL, 'ACTIVE'),
(3, '小刚', '13900139003', 'wechat_xiaogang', 'openid_xiaogang', NULL, 'ACTIVE'),
(4, '小丽', '13900139004', 'wechat_xiaoli', 'openid_xiaoli', NULL, 'ACTIVE'),
(5, '小强', '13900139005', 'wechat_xiaoqiang', 'openid_xiaoqiang', NULL, 'ACTIVE');

INSERT INTO worker_profiles (worker_id, name, phone, skills, available_days) VALUES
(1, '小明', '13900139001', '["餐厅服务","收银","简单英语"]', '["Monday","Tuesday","Wednesday","Thursday","Friday"]'),
(2, '小红', '13900139002', '["家政保洁","收纳整理"]', '["Monday","Wednesday","Friday","Saturday","Sunday"]'),
(3, '小刚', '13900139003', '["配送","搬运","驾驶"]', '["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"]'),
(4, '小丽', '13900139004', '["家教(小学)","钢琴","绘画"]', '["Saturday","Sunday"]'),
(5, '小强', '13900139005', '["餐饮帮厨","洗碗","切配"]', '["Friday","Saturday","Sunday"]');

INSERT INTO worker_resumes (worker_id, file_name, file_url) VALUES
(1, '小明简历.pdf', '/resumes/xiaoming_resume.pdf'),
(4, '小丽简历.pdf', '/resumes/xiaoli_resume.pdf');

-- ========================
-- 9. C端: 工作数据 (c_job)
-- ========================
INSERT INTO c_job (job_id, company_id, company_name, title, description, location, category_id, category_name, rate_type, rate_amount, status) VALUES
(1, 1, '美味餐饮管理有限公司', '餐厅服务员', '负责餐厅日常接待、点餐、上菜等工作。', '北京市朝阳区建国路88号', 1, '餐饮服务', 'HOURLY', 25.00, 'PUBLISHED'),
(2, 1, '美味餐饮管理有限公司', '洗碗工', '负责餐厅餐具清洗消毒。', '北京市朝阳区建国路88号', 1, '餐饮服务', 'HOURLY', 20.00, 'PUBLISHED'),
(3, 1, '美味餐饮管理有限公司', '传菜员', '负责菜品传送。', '北京市朝阳区建国路88号', 1, '餐饮服务', 'HOURLY', 22.00, 'PUBLISHED'),
(4, 2, '极速物流配送有限公司', '外卖配送员', '负责外卖配送，按单计酬。', '上海市浦东新区陆家嘴路100号', 2, '物流配送', 'PIECE', 5.00, 'PUBLISHED'),
(5, 2, '极速物流配送有限公司', '仓库分拣员', '负责仓库分拣打包。', '上海市浦东新区陆家嘴路100号', 2, '物流配送', 'HOURLY', 28.00, 'PUBLISHED'),
(6, 3, '洁新家政服务有限公司', '家庭保洁员', '负责家庭日常保洁。', '广州市天河区天河路200号', 3, '家政保洁', 'HOURLY', 35.00, 'PUBLISHED'),
(7, 3, '洁新家政服务有限公司', '家电清洗师', '负责家电清洗。', '广州市天河区天河路200号', 3, '家政保洁', 'HOURLY', 40.00, 'PUBLISHED'),
(8, 4, '卓越教育培训中心', '兼职家教(小学)', '辅导小学生作业。', '深圳市南山区科技园路300号', 5, '教育培训', 'HOURLY', 60.00, 'PUBLISHED'),
(9, 4, '卓越教育培训中心', '课程顾问', '负责课程咨询推广。', '深圳市南山区科技园路300号', 5, '教育培训', 'MONTHLY', 3500.00, 'PUBLISHED');

-- ========================
-- 10. C端: 申请和班次数据
-- ========================
INSERT INTO c_job_application (worker_id, job_id, company_id, status, applied_at) VALUES
(1, 1, 1, 'ACCEPTED', NOW()),
(1, 4, 2, 'PENDING', NOW()),
(2, 6, 3, 'ACCEPTED', NOW()),
(3, 4, 2, 'ACCEPTED', NOW()),
(4, 8, 4, 'ACCEPTED', NOW()),
(5, 2, 1, 'PENDING', NOW());

INSERT INTO c_shift (worker_id, job_id, company_id, shift_date, start_time, end_time, location_name, status) VALUES
(1, 1, 1, CURDATE(), '08:00:00', '12:00:00', '建国路88号1楼', 'SCHEDULED'),
(1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', '建国路88号1楼', 'SCHEDULED'),
(3, 4, 2, CURDATE(), '07:00:00', '12:00:00', '陆家嘴路100号仓库', 'SCHEDULED'),
(3, 4, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '12:00:00', '陆家嘴路100号仓库', 'SCHEDULED'),
(4, 8, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '11:00:00', '科技园路300号', 'SCHEDULED'),
(4, 8, 4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', '11:00:00', '科技园路300号', 'SCHEDULED'),
(2, 6, 3, CURDATE(), '08:00:00', '12:00:00', '天河区', 'SCHEDULED'),
(2, 6, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', '天河区', 'SCHEDULED');

INSERT INTO c_attendance_record (shift_id, worker_id, check_in_time, check_out_time, total_hours, status) VALUES
(1, 1, CONCAT(CURDATE(), ' 07:55:00'), CONCAT(CURDATE(), ' 12:05:00'), 4.00, 'COMPLETED'),
(5, 4, CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 08:58:00'), NULL, NULL, 'CHECKED_IN');

INSERT INTO c_notification (recipient_id, type, title, content, status) VALUES
(1, 'APPLICATION', '报名已通过', '您申请的"餐厅服务员"岗位已通过审核，请按时到岗。', 'SENT'),
(1, 'REMINDER', '上班提醒', '您今天(08:00-12:00)有"餐厅服务员"班次，请准时到达。', 'SENT'),
(3, 'APPLICATION', '报名已通过', '您申请的"外卖配送员"岗位已通过审核。', 'SENT');

INSERT INTO c_withdrawal_record (worker_id, amount, status, bank_info) VALUES
(1, 200.00, 'COMPLETED', '{"bank":"中国银行","account":"6222****1234"}'),
(1, 150.00, 'PENDING', '{"bank":"中国银行","account":"6222****1234"}'),
(3, 350.00, 'COMPLETED', '{"bank":"工商银行","account":"6222****5678"}');

-- ========================
-- 11. 企业端: 班次分配和申请
-- ========================
INSERT INTO schedule_shifts (job_id, worker_id, shift_date, start_time, end_time, location_name, status) VALUES
(1, 1, CURDATE(), '08:00:00', '12:00:00', '建国路88号1楼', 'SCHEDULED'),
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', '建国路88号1楼', 'SCHEDULED'),
(6, 2, CURDATE(), '08:00:00', '12:00:00', '天河区', 'SCHEDULED'),
(6, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', '天河区', 'SCHEDULED'),
(4, 3, CURDATE(), '07:00:00', '12:00:00', '陆家嘴路100号仓库', 'SCHEDULED'),
(4, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00:00', '12:00:00', '陆家嘴路100号仓库', 'SCHEDULED'),
(8, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '11:00:00', '科技园路300号', 'SCHEDULED');

INSERT INTO job_applications (job_id, worker_id, status, applied_at) VALUES
(1, 1, 'ACCEPTED', NOW()),
(4, 1, 'PENDING', NOW()),
(6, 2, 'ACCEPTED', NOW()),
(4, 3, 'ACCEPTED', NOW()),
(8, 4, 'ACCEPTED', NOW()),
(2, 5, 'PENDING', NOW());

-- ========================
-- 12. 考勤记录 (attendance_records)
-- ========================
INSERT INTO attendance_records (shift_id, check_in_time, check_in_lat, check_in_lng, check_out_time, check_out_lat, check_out_lng, total_hours, status) VALUES
(1, CONCAT(CURDATE(), ' 07:55:00'), 39.9087, 116.4716, CONCAT(CURDATE(), ' 12:05:00'), 39.9087, 116.4716, 4.00, 'COMPLETED'),
(3, CONCAT(CURDATE(), ' 07:58:00'), 23.1291, 113.2644, NULL, NULL, NULL, NULL, 'CHECKED_IN');

-- ========================
-- 13. 工资批次 (payroll_batches)
-- ========================
INSERT INTO payroll_batches (company_id, name, period_start, period_end, status, total_amount, worker_count) VALUES
(1, '2026年5月上旬工资', DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'PAID', 1200.00, 2),
(1, '2026年5月中旬工资', DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(), 'DRAFT', 800.00, 1);

INSERT INTO payroll_items (batch_id, worker_id, job_id, total_hours, rate_type, rate_amount, total_pay, status) VALUES
(1, 1, 1, 40.00, 'HOURLY', 25.00, 1000.00, 'PAID'),
(1, 5, 2, 10.00, 'HOURLY', 20.00, 200.00, 'PAID'),
(2, 1, 1, 32.00, 'HOURLY', 25.00, 800.00, 'PENDING');

-- ========================
-- 14. 通知模板和日志
-- ========================
INSERT INTO notification_templates (type, channel, title_template, content_template) VALUES
('APPLICATION_APPROVED', 'IN_APP', '报名已通过', '您在"{job_title}"的报名已通过审核，请按时到岗。'),
('APPLICATION_REJECTED', 'IN_APP', '报名未通过', '您在"{job_title}"的报名未通过审核。'),
('SHIFT_REMINDER', 'IN_APP', '上班提醒', '您今天({shift_time})在{location}有班次，请准时到达。'),
('PAYROLL_ISSUED', 'IN_APP', '工资已发放', '您的"{period}"工资{amount}元已发放。'),
('WITHDRAWAL_COMPLETED', 'IN_APP', '提现成功', '您的{amount}元提现申请已处理完成。');

INSERT INTO notification_logs (recipient_id, recipient_type, type, channel, title, content, status, sent_at) VALUES
(1, 'WORKER', 'APPLICATION_APPROVED', 'IN_APP', '报名已通过', '您在"餐厅服务员"的报名已通过审核。', 'SENT', NOW()),
(1, 'WORKER', 'SHIFT_REMINDER', 'IN_APP', '上班提醒', '您今天(08:00-12:00)在建国路88号有班次。', 'SENT', NOW()),
(3, 'WORKER', 'APPLICATION_APPROVED', 'IN_APP', '报名已通过', '您在"外卖配送员"的报名已通过审核。', 'SENT', NOW());

-- ========================
-- 15. 提现记录 (withdrawal_records)
-- ========================
INSERT INTO withdrawal_records (worker_id, amount, status, requested_at, completed_at) VALUES
(1, 200.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 150.00, 'PENDING', NOW(), NULL),
(3, 350.00, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY));

-- ========================
-- 16. 黑名单和评价
-- ========================
INSERT INTO worker_blacklists (company_id, worker_id, reason) VALUES
(2, 999, NULL);
-- dummy entry, no actual blacklisted workers

INSERT INTO worker_evaluations (company_id, job_id, worker_id, rating, comment) VALUES
(1, 1, 1, 5, '工作认真负责，服务态度好。'),
(1, 1, 1, 4, '表现不错，准时到岗。'),
(3, 6, 2, 5, '保洁非常干净，客户满意。');

-- ========================
-- 17. 平台端: 举报数据
-- ========================
INSERT INTO job_reports (job_id, reporter_id, reason, description, status) VALUES
(5, 1, '虚假招聘', '该岗位描述的薪资与实际不符。', 'PENDING'),
(9, 2, '信息不实', '联系电话打不通。', 'DISMISSED');

-- ========================
-- 18. 企业信息 (enterprises)
-- ========================
INSERT INTO enterprises (id, company_name, contact_name, contact_phone, company_address, business_license, status, registration_id) VALUES
(1, '北京迅捷物流有限公司', '王经理', '13800138001', '北京市朝阳区建国路88号', 'BL-2024001', 'ACTIVE', 1),
(2, '上海丰盛餐饮管理有限公司', '李店长', '13900139002', '上海市浦东新区陆家嘴路100号', 'BL-2024002', 'ACTIVE', 2),
(3, '广州天汇商贸有限公司', '陈主管', '13700137003', '广州市天河区天河路200号', 'BL-2024003', 'ACTIVE', 3),
(4, '深圳创想科技有限公司', '张总', '13600136004', '深圳市南山区科技园路300号', 'BL-2024004', 'ACTIVE', 4);

-- ========================
-- 19. 企业端登录账号 (enterprise_accounts)
-- ========================
INSERT INTO enterprise_accounts (enterprise_id, username, password, display_name, role, status) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '企业管理员', 'ADMIN', 'ACTIVE'),
(1, 'hr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '人力资源', 'HR', 'ACTIVE'),
(1, 'manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '运营经理', 'MANAGER', 'ACTIVE'),
(1, 'finance', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '财务', 'FINANCE', 'ACTIVE');
