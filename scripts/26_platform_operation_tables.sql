-- =============================================
-- 运营平台核心表结构
-- =============================================

-- 轮播图表
CREATE TABLE IF NOT EXISTS banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
    position VARCHAR(50) DEFAULT 'HOME' COMMENT '位置: HOME, WORKER, ENTERPRISE',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_position (position),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 运营活动表
CREATE TABLE IF NOT EXISTS operation_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    activity_type VARCHAR(50) DEFAULT 'GENERAL' COMMENT '活动类型: NEW_USER, REFERRAL, HOLIDAY, GENERAL',
    banner_image VARCHAR(500) DEFAULT NULL COMMENT '活动横幅',
    content_image VARCHAR(500) DEFAULT NULL COMMENT '活动内容图',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '活动链接',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT, PUBLISHED, ENDED',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    participant_count INT DEFAULT 0 COMMENT '参与人数',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    sort_order INT DEFAULT 0 COMMENT '排序',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_type (activity_type),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营活动表';

-- 推送任务表
CREATE TABLE IF NOT EXISTS push_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '推送标题',
    content TEXT NOT NULL COMMENT '推送内容',
    target_type VARCHAR(50) DEFAULT 'ALL' COMMENT '目标类型: ALL, WORKER, ENTERPRISE, CUSTOM',
    target_ids TEXT COMMENT '目标ID列表，逗号分隔',
    push_type VARCHAR(50) DEFAULT 'IMMEDIATE' COMMENT '推送类型: IMMEDIATE, SCHEDULED',
    scheduled_time DATETIME DEFAULT NULL COMMENT '定时推送时间',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, PROCESSING, COMPLETED, FAILED',
    total_count INT DEFAULT 0 COMMENT '目标总数',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_target_type (target_type),
    INDEX idx_scheduled_time (scheduled_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送任务表';

-- 投诉工单表
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_no VARCHAR(50) NOT NULL COMMENT '工单编号',
    complainant_type VARCHAR(50) NOT NULL COMMENT '投诉人类型: WORKER, ENTERPRISE',
    complainant_id BIGINT NOT NULL COMMENT '投诉人ID',
    complainant_name VARCHAR(100) NOT NULL COMMENT '投诉人姓名',
    complainant_phone VARCHAR(20) COMMENT '投诉人电话',
    accused_type VARCHAR(50) NOT NULL COMMENT '被投诉方类型: WORKER, ENTERPRISE, PLATFORM',
    accused_id BIGINT DEFAULT NULL COMMENT '被投诉方ID',
    accused_name VARCHAR(100) DEFAULT NULL COMMENT '被投诉方名称',
    complaint_type VARCHAR(50) NOT NULL COMMENT '投诉类型: PAYMENT, SCHEDULE, BEHAVIOR, SERVICE, OTHER',
    title VARCHAR(200) NOT NULL COMMENT '投诉标题',
    content TEXT NOT NULL COMMENT '投诉内容',
    images TEXT COMMENT '图片URL列表，逗号分隔',
    related_job_id BIGINT DEFAULT NULL COMMENT '关联职位ID',
    related_job_title VARCHAR(200) DEFAULT NULL COMMENT '关联职位标题',
    related_schedule_id BIGINT DEFAULT NULL COMMENT '关联排班ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, PROCESSING, RESOLVED, CLOSED',
    priority VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级: LOW, NORMAL, HIGH, URGENT',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    handler_name VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
    handle_result TEXT COMMENT '处理结果',
    handled_at DATETIME DEFAULT NULL COMMENT '处理时间',
    satisfaction_score INT DEFAULT NULL COMMENT '满意度评分: 1-5',
    is_appeal TINYINT(1) DEFAULT 0 COMMENT '是否申诉: 0否, 1是',
    parent_id BIGINT DEFAULT NULL COMMENT '父工单ID（申诉关联）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_complaint_no (complaint_no),
    INDEX idx_status (status),
    INDEX idx_complainant (complainant_type, complainant_id),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉工单表';

-- 风险黑名单表
CREATE TABLE IF NOT EXISTS risk_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型: WORKER, ENTERPRISE, IP, PHONE',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID（用户或企业）',
    target_name VARCHAR(100) DEFAULT NULL COMMENT '目标名称',
    target_value VARCHAR(200) DEFAULT NULL COMMENT '目标值（手机号、IP等）',
    reason VARCHAR(500) NOT NULL COMMENT '拉黑原因',
    risk_level VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '风险等级: LOW, MEDIUM, HIGH, SEVERE',
    ban_type VARCHAR(20) DEFAULT 'PERMANENT' COMMENT '封禁类型: TEMPORARY, PERMANENT',
    ban_start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '封禁开始时间',
    ban_end_time DATETIME DEFAULT NULL COMMENT '封禁结束时间（临时封禁）',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, EXPIRED, REMOVED',
    removed_reason VARCHAR(500) DEFAULT NULL COMMENT '移除原因',
    removed_at DATETIME DEFAULT NULL COMMENT '移除时间',
    removed_by_id BIGINT DEFAULT NULL COMMENT '移除人ID',
    removed_by_name VARCHAR(100) DEFAULT NULL COMMENT '移除人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_id),
    INDEX idx_status (status),
    INDEX idx_risk_level (risk_level),
    INDEX idx_ban_end_time (ban_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险黑名单表';

-- 风险白名单表
CREATE TABLE IF NOT EXISTS risk_whitelist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型: WORKER, ENTERPRISE, IP, PHONE',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID（用户或企业）',
    target_name VARCHAR(100) DEFAULT NULL COMMENT '目标名称',
    target_value VARCHAR(200) DEFAULT NULL COMMENT '目标值（手机号、IP等）',
    reason VARCHAR(500) NOT NULL COMMENT '白名单原因',
    effective_start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
    effective_end_time DATETIME DEFAULT NULL COMMENT '生效结束时间',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, EXPIRED, REMOVED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_target (target_type, target_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险白名单表';

-- 风控规则表
CREATE TABLE IF NOT EXISTS risk_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) NOT NULL COMMENT '规则代码',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型: REGISTRATION, LOGIN, APPLICATION, CLOCKIN, WITHDRAWAL, BEHAVIOR',
    description VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
    trigger_condition TEXT NOT NULL COMMENT '触发条件（JSON配置）',
    action_type VARCHAR(50) NOT NULL COMMENT '动作类型: ALERT, BLOCK, REVIEW, LIMIT',
    action_config TEXT COMMENT '动作配置（JSON）',
    risk_level VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '风险等级: LOW, MEDIUM, HIGH',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE',
    trigger_count INT DEFAULT 0 COMMENT '触发次数',
    last_trigger_at DATETIME DEFAULT NULL COMMENT '最后触发时间',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 评价管理表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_type VARCHAR(50) NOT NULL COMMENT '评价类型: WORKER_TO_ENTERPRISE, ENTERPRISE_TO_WORKER',
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID',
    reviewer_name VARCHAR(100) NOT NULL COMMENT '评价人姓名',
    reviewer_type VARCHAR(50) NOT NULL COMMENT '评价人类型: WORKER, ENTERPRISE',
    reviewee_id BIGINT NOT NULL COMMENT '被评价人ID',
    reviewee_name VARCHAR(100) NOT NULL COMMENT '被评价人姓名',
    reviewee_type VARCHAR(50) NOT NULL COMMENT '被评价人类型: WORKER, ENTERPRISE',
    job_id BIGINT DEFAULT NULL COMMENT '关联职位ID',
    job_title VARCHAR(200) DEFAULT NULL COMMENT '关联职位标题',
    schedule_id BIGINT DEFAULT NULL COMMENT '关联排班ID',
    rating INT NOT NULL COMMENT '评分: 1-5',
    content TEXT COMMENT '评价内容',
    images TEXT COMMENT '图片URL列表，逗号分隔',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签，逗号分隔',
    is_anonymous TINYINT(1) DEFAULT 0 COMMENT '是否匿名: 0否, 1是',
    is_violation TINYINT(1) DEFAULT 0 COMMENT '是否违规: 0否, 1是',
    violation_reason VARCHAR(500) DEFAULT NULL COMMENT '违规原因',
    violation_handled_at DATETIME DEFAULT NULL COMMENT '违规处理时间',
    violation_handler_id BIGINT DEFAULT NULL COMMENT '违规处理人ID',
    violation_handler_name VARCHAR(100) DEFAULT NULL COMMENT '违规处理人姓名',
    reply_content TEXT COMMENT '回复内容',
    reply_at DATETIME DEFAULT NULL COMMENT '回复时间',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态: NORMAL, HIDDEN, DELETED',
    helpful_count INT DEFAULT 0 COMMENT '有帮助计数',
    report_count INT DEFAULT 0 COMMENT '举报计数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reviewer (reviewer_type, reviewer_id),
    INDEX idx_reviewee (reviewee_type, reviewee_id),
    INDEX idx_job_id (job_id),
    INDEX idx_rating (rating),
    INDEX idx_is_violation (is_violation),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价管理表';

-- 平台操作员表
CREATE TABLE IF NOT EXISTS platform_operators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    role VARCHAR(50) DEFAULT 'OPERATOR' COMMENT '角色: SUPER_ADMIN, ADMIN, OPERATOR, CUSTOMER_SERVICE, FINANCE',
    permissions TEXT COMMENT '权限列表（JSON）',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE, LOCKED',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    login_fail_count INT DEFAULT 0 COMMENT '登录失败次数',
    lock_time DATETIME DEFAULT NULL COMMENT '锁定时间',
    operator_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '创建人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    INDEX idx_phone (phone),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台操作员表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    module VARCHAR(50) NOT NULL COMMENT '模块: JOB, USER, APPLICATION, SCHEDULE, SETTLEMENT, RISK, CONTENT, SYSTEM',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE, UPDATE, DELETE, QUERY, EXPORT, AUDIT, APPROVE, REJECT',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    target_name VARCHAR(200) DEFAULT NULL COMMENT '目标名称',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT 'User Agent',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数（JSON）',
    old_value TEXT COMMENT '变更前值（JSON）',
    new_value TEXT COMMENT '变更后值（JSON）',
    result VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '操作结果: SUCCESS, FAIL, PARTIAL',
    error_message TEXT COMMENT '错误信息',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator_id (operator_id),
    INDEX idx_module (module),
    INDEX idx_operation_type (operation_type),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- FAQ表
CREATE TABLE IF NOT EXISTS faqs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    category VARCHAR(50) DEFAULT 'GENERAL' COMMENT '分类: GENERAL, PAYMENT, SCHEDULE, REGISTRATION, WITHDRAWAL, COMPLAINT',
    sort_order INT DEFAULT 0 COMMENT '排序',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    helpful_count INT DEFAULT 0 COMMENT '有帮助计数',
    not_helpful_count INT DEFAULT 0 COMMENT '无帮助计数',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FAQ表';

-- 插入默认管理员账号（密码: admin123，BCrypt加密）
INSERT IGNORE INTO platform_operators (username, password, real_name, phone, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7i.8VqJ5W', '系统管理员', '13800000000', 'SUPER_ADMIN', 'ACTIVE');
