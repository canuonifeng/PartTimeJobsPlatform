CREATE TABLE `attendance_corrections` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shift_id` bigint NOT NULL,
  `worker_id` bigint NOT NULL,
  `reason` varchar(500) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `reject_reason` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` datetime DEFAULT NULL,
  `processor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shift_id` (`shift_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `attendance_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shift_id` bigint NOT NULL,
  `job_id` bigint DEFAULT NULL COMMENT '岗位ID',
  `company_id` bigint DEFAULT NULL COMMENT '企业ID',
  `check_in_time` datetime DEFAULT NULL,
  `check_in_lat` decimal(10,7) DEFAULT NULL,
  `check_in_lng` decimal(10,7) DEFAULT NULL,
  `check_out_time` datetime DEFAULT NULL,
  `check_out_lat` decimal(10,7) DEFAULT NULL,
  `check_out_lng` decimal(10,7) DEFAULT NULL,
  `total_hours` decimal(5,2) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CHECKED_IN/CHECKED_OUT',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `scheduled_pay` decimal(10,2) DEFAULT NULL COMMENT 'æŽ’ç­è–ªèµ„',
  `calculated_at` datetime DEFAULT NULL COMMENT '结算计算时间',
  `worker_id` bigint DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `is_paid` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已发放',
  `payable_pay` decimal(10,2) DEFAULT NULL COMMENT 'åº”ä»˜è–ªèµ„',
  PRIMARY KEY (`id`),
  KEY `idx_shift_id` (`shift_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_company_id` (`company_id`),
  CONSTRAINT `attendance_records_ibfk_1` FOREIGN KEY (`shift_id`) REFERENCES `schedule_shifts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `c_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recipient_id` bigint NOT NULL,
  `recipient_type` varchar(20) NOT NULL DEFAULT 'WORKER',
  `type` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `sent_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recipient` (`recipient_id`,`recipient_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `c_withdrawal_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `bank_info` varchar(500) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `processed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `c_worker` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `wechat_code` varchar(100) DEFAULT NULL,
  `open_id` varchar(200) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `wechat_code` (`wechat_code`),
  UNIQUE KEY `open_id` (`open_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_wechat_code` (`wechat_code`),
  KEY `idx_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `company_workers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL,
  `worker_id` bigint NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/BLACKLISTED',
  `first_contact_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_contact_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_worker` (`company_id`,`worker_id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `enterprise_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enterprise_id` bigint NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT 'ADMIN/HR/MANAGER/FINANCE',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  CONSTRAINT `enterprise_accounts_ibfk_1` FOREIGN KEY (`enterprise_id`) REFERENCES `enterprises` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `enterprises` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_name` varchar(200) NOT NULL,
  `company_logo` varchar(500) DEFAULT NULL COMMENT '企业logoURL',
  `contact_name` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `company_address` varchar(500) DEFAULT NULL,
  `business_license` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / SUSPENDED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `flyway_schema_history_cservice` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_cservice_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `flyway_schema_history_enterprise` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_enterprise_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `flyway_schema_history_platform` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_platform_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `job_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `company_id` bigint NOT NULL,
  `worker_id` bigint NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `applied_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_worker` (`job_id`,`worker_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `job_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job categories (admin-managed)';

CREATE TABLE `job_rates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `type` varchar(20) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `currency` varchar(10) NOT NULL DEFAULT 'CNY',
  `rules` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `job_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `reporter_id` bigint NOT NULL,
  `reason` varchar(200) NOT NULL,
  `description` text,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, REVIEWED, DISMISSED, BANNED',
  `reviewer_id` varchar(100) DEFAULT NULL,
  `review_remark` varchar(500) DEFAULT NULL,
  `reviewed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Job reports from workers';

CREATE TABLE `job_schedules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `schedule_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `slots_available` int NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `job_tag_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '标签组名称',
  `code` varchar(50) NOT NULL COMMENT '标签组编码',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_tag_groups_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位标签组';

CREATE TABLE `job_tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '标签组ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `code` varchar(50) NOT NULL COMMENT '标签编码',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_tags_group_code` (`group_id`,`code`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位标签';

CREATE TABLE `job_tag_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL COMMENT '岗位ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_tag_relations_job_tag` (`job_id`,`tag_id`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位标签关系';

INSERT INTO `job_tag_groups` (`name`, `code`, `sort_order`, `status`) VALUES
('结算周期', 'settlement_cycle', 10, 'ACTIVE'),
('结算方式', 'settlement_method', 20, 'ACTIVE');

INSERT INTO `job_tags` (`group_id`, `name`, `code`, `sort_order`, `status`) VALUES
((SELECT `id` FROM `job_tag_groups` WHERE `code` = 'settlement_cycle'), '日结', 'daily', 10, 'ACTIVE'),
((SELECT `id` FROM `job_tag_groups` WHERE `code` = 'settlement_cycle'), '周结', 'weekly', 20, 'ACTIVE'),
((SELECT `id` FROM `job_tag_groups` WHERE `code` = 'settlement_cycle'), '月结', 'monthly', 30, 'ACTIVE'),
((SELECT `id` FROM `job_tag_groups` WHERE `code` = 'settlement_method'), '计件', 'piecework', 10, 'ACTIVE'),
((SELECT `id` FROM `job_tag_groups` WHERE `code` = 'settlement_method'), '按时', 'hourly', 20, 'ACTIVE');

CREATE TABLE `jobs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` longtext,
  `requirements` longtext COMMENT '任职要求富文本',
  `contact_phone` varchar(30) DEFAULT NULL COMMENT '岗位联系方式',
  `location` varchar(500) DEFAULT NULL,
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区',
  `address` varchar(200) DEFAULT NULL COMMENT '详细地址（街道门牌号）',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经纬度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '经纬度',
  `category_id` bigint DEFAULT NULL,
  `headcount` int NOT NULL DEFAULT '1',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `deadline` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `rate_type` varchar(20) DEFAULT NULL,
  `rate_amount` decimal(10,2) DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  `accepted_count` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recipient_id` bigint NOT NULL,
  `recipient_type` varchar(20) NOT NULL COMMENT 'WORKER/ENTERPRISE',
  `type` varchar(50) NOT NULL,
  `channel` varchar(30) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENT/FAILED',
  `error_message` varchar(500) DEFAULT NULL,
  `sent_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recipient` (`recipient_id`,`recipient_type`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_templates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL COMMENT 'APPLICATION_RECEIVED/APPLICATION_STATUS/SCHEDULE_ASSIGNED/CHECK_IN_REMINDER/PAYROLL_READY/JOB_RECOMMENDATION',
  `channel` varchar(30) NOT NULL COMMENT 'IN_APP/WECHAT_TEMPLATE/SMS',
  `title_template` varchar(200) NOT NULL,
  `content_template` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_channel` (`channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `schedule_shifts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_id` bigint NOT NULL,
  `worker_id` bigint NOT NULL,
  `shift_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `location_lat` decimal(10,7) DEFAULT NULL,
  `location_lng` decimal(10,7) DEFAULT NULL,
  `location_radius` int DEFAULT NULL,
  `location_name` varchar(255) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/CHECKED_IN/CHECKED_OUT/ABSENT',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `application_id` bigint DEFAULT NULL COMMENT '报名ID快照',
  `salary_type` varchar(20) DEFAULT NULL COMMENT '薪资类型快照',
  `salary_amount` decimal(10,2) DEFAULT NULL COMMENT '薪资金额快照',
  `salary_currency` varchar(10) DEFAULT NULL COMMENT '薪资币种快照',
  `company_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_shifts_application_schedule` (`application_id`,`job_id`,`worker_id`,`shift_date`,`start_time`,`end_time`),
  KEY `idx_job_id` (`job_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_shift_date` (`shift_date`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `system_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL,
  `config_value` text NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`),
  KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='System configuration key-value store';

CREATE TABLE `withdrawal_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/COMPLETED/FAILED',
  `requested_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `worker_blacklists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL,
  `worker_id` bigint NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_worker` (`company_id`,`worker_id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_worker_id` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `worker_evaluations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `worker_id` bigint NOT NULL,
  `rating` int NOT NULL COMMENT '1-5',
  `comment` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_worker_id` (`worker_id`),
  KEY `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `worker_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `skills` text,
  `available_days` text COMMENT 'JSON array of available days',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `worker_id` (`worker_id`),
  KEY `idx_worker_id` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `worker_resumes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_url` varchar(500) NOT NULL,
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_worker_id` (`worker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
