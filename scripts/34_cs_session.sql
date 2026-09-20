-- 在线客服会话表
CREATE TABLE IF NOT EXISTS cs_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_no VARCHAR(64) NOT NULL COMMENT '会话编号',
    user_type VARCHAR(20) NOT NULL COMMENT '用户类型: WORKER, ENTERPRISE',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) DEFAULT NULL COMMENT '用户名称快照',
    user_phone VARCHAR(50) DEFAULT NULL COMMENT '用户电话快照',
    agent_id BIGINT DEFAULT NULL COMMENT '接起客服ID',
    agent_name VARCHAR(100) DEFAULT NULL COMMENT '接起客服名称',
    status VARCHAR(20) DEFAULT 'WAITING' COMMENT '状态: WAITING, PROCESSING, CLOSED',
    last_message TEXT COMMENT '最后一条消息快照',
    last_message_at DATETIME DEFAULT NULL COMMENT '最后消息时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    closed_at DATETIME DEFAULT NULL COMMENT '结束时间',
    UNIQUE KEY uk_session_no (session_no),
    INDEX idx_status (status),
    INDEX idx_user (user_type, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- 在线客服消息表
CREATE TABLE IF NOT EXISTS cs_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    sender_type VARCHAR(20) NOT NULL COMMENT '发送方: USER, AGENT, SYSTEM',
    sender_name VARCHAR(100) DEFAULT NULL COMMENT '发送方名称快照',
    content TEXT NOT NULL COMMENT '消息内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';
