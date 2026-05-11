-- 用户表
CREATE TABLE `sys_user` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `username` varchar(64) NOT NULL COMMENT '用户名，唯一',
    `password` varchar(128) NOT NULL COMMENT '密码，加密存储',
    `salt` varchar(64) NOT NULL COMMENT '密码加密盐值',
    `token` varchar(512) DEFAULT NULL COMMENT 'Token凭证',
    `token_expire_time` datetime DEFAULT NULL COMMENT 'Token过期时间',
    `login_fail_count` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '连续登录失败次数',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 账户表
CREATE TABLE `account` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '账户ID，主键',
    `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
    `name` varchar(64) NOT NULL COMMENT '账户名称，同一用户下唯一',
    `type` tinyint unsigned NOT NULL COMMENT '账户类型：1现金 2银行储蓄卡 3信用卡 4支付宝 5微信',
    `initial_balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '初始余额',
    `current_balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '当前余额',
    `remark` varchar(200) DEFAULT NULL COMMENT '账户备注',
    `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '账户状态：1正常 2停用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_name` (`id`,`user_id`, `name`) USING BTREE,
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_user_id_status` (`user_id`, `status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

-- 分类表（组合模式：parent_id自引用，NULL为一级分类）
CREATE TABLE `category` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键',
    `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '所属用户ID，系统预置分类为0',
    `parent_id` bigint unsigned DEFAULT NULL COMMENT '父分类ID，一级分类为NULL',
    `name` varchar(64) NOT NULL COMMENT '分类名称',
    `type` tinyint unsigned NOT NULL COMMENT '分类类型：1收入 2支出',
    `is_system` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否系统预置：1是 0否',
    `sort_order` int unsigned NOT NULL DEFAULT '0' COMMENT '排序序号',
    `color` varchar(20) DEFAULT NULL COMMENT '分类颜色，十六进制色值如#409EFF',
    `icon` varchar(64) DEFAULT NULL COMMENT '分类图标，Element UI图标名称',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_parent_id` (`parent_id`) USING BTREE,
    KEY `idx_type` (`type`) USING BTREE,
    KEY `idx_is_system` (`is_system`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易分类表';

-- 交易记录表
CREATE TABLE `transaction` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '交易ID，主键',
    `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
    `type` tinyint unsigned NOT NULL COMMENT '交易类型：1收入 2支出 3转账',
    `from_account_id` bigint unsigned DEFAULT NULL COMMENT '转出账户ID，支出/转账时必填',
    `to_account_id` bigint unsigned DEFAULT NULL COMMENT '转入账户ID，收入/转账时必填',
    `amount` decimal(12,2) NOT NULL COMMENT '交易金额，必须大于0',
    `category_id` bigint unsigned NOT NULL COMMENT '交易分类ID',
    `transaction_time` datetime NOT NULL COMMENT '交易发生时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '交易备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) USING BTREE,
    KEY `idx_user_id_type` (`user_id`, `type`) USING BTREE,
    KEY `idx_user_id_transaction_time` (`user_id`, `transaction_time`) USING BTREE,
    KEY `idx_from_account_id` (`from_account_id`) USING BTREE,
    KEY `idx_to_account_id` (`to_account_id`) USING BTREE,
    KEY `idx_category_id` (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- 预算表
CREATE TABLE `budget` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '预算ID，主键',
    `user_id` bigint unsigned NOT NULL COMMENT '所属用户ID',
    `month` varchar(7) NOT NULL COMMENT '预算月份，格式：yyyy-MM',
    `type` tinyint unsigned NOT NULL COMMENT '预算类型：1总预算 2分类预算',
    `category_id` bigint unsigned DEFAULT NULL COMMENT '关联分类ID，分类预算时必填',
    `amount` decimal(12,2) NOT NULL COMMENT '预算金额，必须大于0',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_month_type_category` (`user_id`, `month`, `type`, `category_id`) USING BTREE,
    KEY `idx_user_id_month` (`user_id`, `month`) USING BTREE,
    KEY `idx_category_id` (`category_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算表';
