-- ============================================
-- 零工平台 - 演示数据
-- ============================================
-- 密码均为 123456 (BCrypt hash)
-- ============================================

-- ----------------------------
-- 1. 职位分类
-- ----------------------------
INSERT INTO job_categories (id, name, parent_id, sort_order) VALUES
(1, '餐饮服务', NULL, 1),
(2, '零售促销', NULL, 2),
(3, '物流配送', NULL, 3),
(4, '教育培训', NULL, 4),
(5, '服务员', 1, 1),
(6, '厨师', 1, 2),
(7, '促销员', 2, 1),
(8, '收银员', 2, 2),
(9, '快递员', 3, 1),
(10, '搬运工', 3, 2);

-- ----------------------------
-- 2. 企业
-- ----------------------------
INSERT INTO enterprises (id, company_name, company_logo, contact_name, contact_phone, company_address, status) VALUES
(1, '美味餐饮有限公司', NULL, '张三', '13800000001', '北京市朝阳区建国路88号', 'ACTIVE'),
(2, '优品超市连锁', NULL, '李四', '13800000002', '上海市浦东新区陆家嘴路100号', 'ACTIVE'),
(3, '极速物流有限公司', NULL, '王五', '13800000003', '广州市天河区体育西路50号', 'SUSPENDED');

-- ----------------------------
-- 3. 企业账号
-- ----------------------------
INSERT INTO enterprise_accounts (id, enterprise_id, username, password, display_name, role, status) VALUES
(1, 1, 'admin@meiwei', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张三', 'ADMIN', 'ACTIVE'),
(2, 1, 'hr@meiwei', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李人事', 'HR', 'ACTIVE'),
(3, 2, 'admin@yopin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李四', 'ADMIN', 'ACTIVE');

-- ----------------------------
-- 4. 工人
-- ----------------------------
INSERT INTO c_worker (id, name, phone, wechat_code, open_id, avatar_url, status) VALUES
(1, '赵小明', '13900000001', 'wx_zhaoxm', 'openid_zxm', NULL, 'ACTIVE'),
(2, '钱小红', '13900000002', 'wx_qianxh', 'openid_qxh', NULL, 'ACTIVE'),
(3, '孙大鹏', '13900000003', 'wx_sundp', 'openid_sdp', NULL, 'ACTIVE'),
(4, '李芳芳', '13900000004', 'wx_liff', 'openid_lff', NULL, 'ACTIVE');

-- ----------------------------
-- 5. 工人档案
-- ----------------------------
INSERT INTO worker_profiles (id, worker_id, name, phone, avatar_url, skills, available_days) VALUES
(1, 1, '赵小明', '13900000001', NULL, '["餐饮服务", "促销"]', '["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"]'),
(2, 2, '钱小红', '13900000002', NULL, '["收银", "促销", "导购"]', '["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]'),
(3, 3, '孙大鹏', '13900000003', NULL, '["快递", "搬运"]', '["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"]'),
(4, 4, '李芳芳', '13900000004', NULL, '["服务员", "餐饮"]', '["FRIDAY","SATURDAY","SUNDAY"]');

-- ----------------------------
-- 6. 职位
-- ----------------------------
INSERT INTO jobs (id, company_id, title, description, location, province, city, district, address, latitude, longitude, category_id, headcount, status, deadline, rate_type, rate_amount, published_at, accepted_count) VALUES
(1, 1, '周末餐厅服务员', '负责餐厅周末的顾客接待、点餐、上菜等工作。要求有服务意识，态度亲和。', '北京市朝阳区建国路88号', '北京市', '北京市', '朝阳区', '建国路88号美味大厦3层', 39.9087000, 116.4716000, 5, 5, 'PUBLISHED', '2026-06-30 23:59:59', 'HOURLY', 25.00, '2026-05-20 10:00:00', 2),
(2, 1, '帮厨兼洗碗工', '协助厨师进行食材准备、餐具清洗、厨房清洁等工作。', '北京市朝阳区建国路88号', '北京市', '北京市', '朝阳区', '建国路88号美味大厦B1', 39.9087000, 116.4716000, 6, 3, 'PUBLISHED', '2026-06-15 23:59:59', 'HOURLY', 22.00, '2026-05-21 09:00:00', 1),
(3, 1, '晚班传菜员', '负责晚餐高峰期的传菜工作，工作时间17:00-22:00。', '北京市朝阳区建国路88号', '北京市', '北京市', '朝阳区', '建国路88号美味大厦3层', 39.9087000, 116.4716000, 5, 4, 'PUBLISHED', '2026-06-20 23:59:59', 'HOURLY', 20.00, '2026-05-22 14:00:00', 0),
(4, 2, '周末促销员', '在超市内进行产品试吃和促销推广活动，引导顾客购买。', '上海市浦东新区陆家嘴路100号', '上海市', '上海市', '浦东新区', '陆家嘴路100号优品超市', 31.2354000, 121.5016000, 7, 10, 'PUBLISHED', '2026-06-25 23:59:59', 'HOURLY', 30.00, '2026-05-19 10:00:00', 2),
(5, 2, '收银员（周末）', '超市收银台工作，要求有收银经验，细心负责。', '上海市浦东新区陆家嘴路100号', '上海市', '上海市', '浦东新区', '陆家嘴路100号优品超市', 31.2354000, 121.5016000, 8, 3, 'PUBLISHED', '2026-06-28 23:59:59', 'HOURLY', 28.00, '2026-05-23 10:00:00', 0),
(6, 1, '厨师助理（已关闭）', '协助主厨进行菜品制作。', '北京市朝阳区建国路88号', '北京市', '北京市', '朝阳区', '建国路88号美味大厦3层', 39.9087000, 116.4716000, 6, 2, 'CLOSED', '2026-05-01 23:59:59', 'DAILY', 200.00, '2026-04-15 10:00:00', 1),
(7, 1, '临时帮工（草稿）', '临时帮工岗位', '北京市朝阳区建国路88号', '北京市', '北京市', '朝阳区', '建国路88号美味大厦3层', 39.9087000, 116.4716000, 5, 2, 'DRAFT', '2026-07-01 23:59:59', 'HOURLY', 18.00, NULL, 0);

-- ----------------------------
-- 7. 职位薪资
-- ----------------------------
INSERT INTO job_rates (id, job_id, type, amount, currency, rules) VALUES
(1, 1, 'HOURLY', 25.00, 'CNY', NULL),
(2, 2, 'HOURLY', 22.00, 'CNY', NULL),
(3, 3, 'HOURLY', 20.00, 'CNY', NULL),
(4, 4, 'HOURLY', 30.00, 'CNY', NULL),
(5, 5, 'HOURLY', 28.00, 'CNY', NULL),
(6, 6, 'DAILY', 200.00, 'CNY', NULL),
(7, 7, 'HOURLY', 18.00, 'CNY', NULL);

-- ----------------------------
-- 8. 排班时段
-- ----------------------------
INSERT INTO job_schedules (id, job_id, schedule_date, start_time, end_time, slots_available) VALUES
-- 职位1：周末服务员 (周六日)
(1, 1, '2026-06-06', '10:00:00', '18:00:00', 5),
(2, 1, '2026-06-07', '10:00:00', '18:00:00', 5),
(3, 1, '2026-06-13', '10:00:00', '18:00:00', 4),
(4, 1, '2026-06-14', '10:00:00', '18:00:00', 5),
-- 职位2：帮厨 (周一至周五)
(5, 2, '2026-06-01', '08:00:00', '16:00:00', 3),
(6, 2, '2026-06-02', '08:00:00', '16:00:00', 3),
(7, 2, '2026-06-03', '08:00:00', '16:00:00', 2),
(8, 2, '2026-06-04', '08:00:00', '16:00:00', 3),
(9, 2, '2026-06-05', '08:00:00', '16:00:00', 3),
-- 职位4：促销员 (周六日)
(10, 4, '2026-06-06', '09:00:00', '18:00:00', 10),
(11, 4, '2026-06-07', '09:00:00', '18:00:00', 10),
(12, 4, '2026-06-13', '09:00:00', '18:00:00', 8),
(13, 4, '2026-06-14', '09:00:00', '18:00:00', 10),
-- 职位6：已关闭的职位
(14, 6, '2026-04-20', '09:00:00', '18:00:00', 2),
(15, 6, '2026-04-21', '09:00:00', '18:00:00', 2);

-- ----------------------------
-- 9. 报名记录
-- ----------------------------
INSERT INTO job_applications (id, job_id, company_id, worker_id, status, applied_at) VALUES
(1, 1, 1, 1, 'ACCEPTED', '2026-05-21 10:30:00'),
(2, 1, 1, 2, 'ACCEPTED', '2026-05-21 14:00:00'),
(3, 2, 1, 1, 'ACCEPTED', '2026-05-22 09:00:00'),
(4, 4, 2, 2, 'ACCEPTED', '2026-05-20 11:00:00'),
(5, 4, 2, 4, 'ACCEPTED', '2026-05-20 15:30:00'),
(6, 6, 1, 1, 'ACCEPTED', '2026-04-16 10:00:00'),
(7, 1, 1, 3, 'PENDING', '2026-05-25 09:00:00');

-- ----------------------------
-- 10. 排班
-- ----------------------------
INSERT INTO schedule_shifts (id, job_id, worker_id, shift_date, start_time, end_time, location_name, status, application_id, salary_type, salary_amount, company_id) VALUES
-- 服务员 - 赵小明
(1, 1, 1, '2026-06-06', '10:00:00', '18:00:00', '美味大厦3层', 'SCHEDULED', 1, 'HOURLY', 25.00, 1),
(2, 1, 1, '2026-06-07', '10:00:00', '18:00:00', '美味大厦3层', 'SCHEDULED', 1, 'HOURLY', 25.00, 1),
-- 服务员 - 钱小红
(3, 1, 2, '2026-06-06', '10:00:00', '18:00:00', '美味大厦3层', 'SCHEDULED', 2, 'HOURLY', 25.00, 1),
(4, 1, 2, '2026-06-07', '10:00:00', '18:00:00', '美味大厦3层', 'SCHEDULED', 2, 'HOURLY', 25.00, 1),
-- 帮厨 - 赵小明（已打卡）
(5, 2, 1, '2026-06-01', '08:00:00', '16:00:00', '美味大厦B1', 'CHECKED_OUT', 3, 'HOURLY', 22.00, 1),
(6, 2, 1, '2026-06-02', '08:00:00', '16:00:00', '美味大厦B1', 'CHECKED_OUT', 3, 'HOURLY', 22.00, 1),
-- 促销员 - 钱小红（已缺勤）
(7, 4, 2, '2026-06-06', '09:00:00', '18:00:00', '优品超市', 'ABSENT', 4, 'HOURLY', 30.00, 2),
(8, 4, 4, '2026-06-06', '09:00:00', '18:00:00', '优品超市', 'SCHEDULED', 5, 'HOURLY', 30.00, 2),
-- 已关闭职位的旧排班
(9, 6, 1, '2026-04-20', '09:00:00', '18:00:00', '美味大厦3层', 'CHECKED_OUT', 6, 'DAILY', 200.00, 1);

-- ----------------------------
-- 11. 考勤记录
-- ----------------------------
INSERT INTO attendance_records (id, shift_id, job_id, company_id, worker_id, check_in_time, check_out_time, total_hours, status, scheduled_pay, payable_pay, is_paid, remark) VALUES
-- 帮厨 - 赵小明 (6月1日)
(1, 5, 2, 1, 1, '2026-06-01 07:55:00', '2026-06-01 16:05:00', 8.00, 'CHECKED_OUT', 176.00, 176.00, TRUE, NULL),
-- 帮厨 - 赵小明 (6月2日)
(2, 6, 2, 1, 1, '2026-06-02 08:02:00', '2026-06-02 16:00:00', 8.00, 'CHECKED_OUT', 176.00, 176.00, FALSE, NULL),
-- 旧职位排班
(3, 9, 6, 1, 1, '2026-04-20 08:55:00', '2026-04-20 18:10:00', 8.00, 'CHECKED_OUT', 200.00, 200.00, TRUE, NULL);

-- ----------------------------
-- 12. 考勤修正申请
-- ----------------------------
INSERT INTO attendance_corrections (id, shift_id, worker_id, reason, status, reject_reason, created_at, processed_at, processor_id) VALUES
(1, 5, 1, '当天其实加班了1小时，实际工时应为9小时', 'PENDING', NULL, '2026-06-02 10:00:00', NULL, NULL);

-- ----------------------------
-- 13. 企业与工人关系
-- ----------------------------
INSERT INTO company_workers (id, company_id, worker_id, status, first_contact_at) VALUES
(1, 1, 1, 'ACTIVE', '2026-05-21 10:30:00'),
(2, 1, 2, 'ACTIVE', '2026-05-21 14:00:00'),
(3, 2, 2, 'ACTIVE', '2026-05-20 11:00:00'),
(4, 2, 4, 'ACTIVE', '2026-05-20 15:30:00'),
(5, 1, 3, 'ACTIVE', '2026-05-25 09:00:00');

-- ----------------------------
-- 14. 评分
-- ----------------------------
INSERT INTO worker_evaluations (id, company_id, job_id, worker_id, rating, comment) VALUES
(1, 1, 2, 1, 5, '工作认真负责，准时到岗');

-- ----------------------------
-- 15. 通知模板
-- ----------------------------
INSERT INTO notification_templates (id, type, channel, title_template, content_template) VALUES
(1, 'APPLICATION_RECEIVED', 'IN_APP', '新报名通知', '您的职位「{job_title}」收到一份新的报名，请及时处理。'),
(2, 'APPLICATION_STATUS', 'IN_APP', '报名状态更新', '您报名的职位「{job_title}」状态已更新为：{status}'),
(3, 'SCHEDULE_ASSIGNED', 'IN_APP', '排班通知', '您已被安排到职位「{job_title}」的排班：{shift_date} {start_time}-{end_time}'),
(4, 'CHECK_IN_REMINDER', 'IN_APP', '打卡提醒', '您今天有排班，记得准时打卡哦！职位：{job_title}，时间：{start_time}-{end_time}'),
(5, 'PAYROLL_READY', 'IN_APP', '薪资已发放', '您有一笔薪资已发放：¥{amount}，请查看详情。'),
(6, 'JOB_RECOMMENDATION', 'IN_APP', '职位推荐', '为您推荐适合的职位：「{job_title}」，时薪¥{rate}，点击查看详情。');

-- ----------------------------
-- 16. 系统配置
-- ----------------------------
INSERT INTO system_configs (id, config_key, config_value, description) VALUES
(1, 'max_application_per_worker', '10', '工人最多可同时报名的职位数'),
(2, 'check_in_radius_meters', '100', '打卡允许误差范围（米）'),
(3, 'withdrawal_min_amount', '50.00', '最低提现金额（元）'),
(4, 'review_required_for_publish', 'false', '职位发布是否需要平台审核'),
(5, 'enterprise_registration_enabled', 'true', '是否允许企业自助注册');
