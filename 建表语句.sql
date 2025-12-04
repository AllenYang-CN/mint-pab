-- 账户表
CREATE TABLE `pab_accounts` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '账户名称',
  `type` VARCHAR(20) NOT NULL COMMENT '账户类型（现金/银行卡/支付宝/微信/投资等）',
  `balance` DECIMAL(12,2) DEFAULT '0.00' COMMENT '当前余额',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_account_type` (`type`)
) ENGINE=InnoDB COMMENT='账户信息表';

-- 分类表
CREATE TABLE `pab_categories` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `type` ENUM('income','expense') NOT NULL COMMENT '类型（收入/支出）',
  `parent_id` INT UNSIGNED NULL DEFAULT NULL COMMENT '父分类ID（一级分类为NULL）',
  `icon` VARCHAR(100) DEFAULT '' COMMENT '分类图标',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_type` (`type`),
  KEY `idx_category_parent` (`parent_id`),
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `pab_categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='收支分类表';

-- 标签表
CREATE TABLE `pab_tags` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(30) NOT NULL COMMENT '标签名称（如“加班餐”）',
  `color` VARCHAR(7) DEFAULT '#FFFFFF' COMMENT '标签颜色（十六进制）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE=InnoDB COMMENT='自定义标签表';

-- 交易记录表
CREATE TABLE `pab_transactions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `account_id` INT UNSIGNED NOT NULL COMMENT '关联账户ID',
  `category_id` INT UNSIGNED NOT NULL COMMENT '关联分类ID',
  `type` ENUM('income','expense','transfer') NOT NULL COMMENT '交易类型（收入/支出/转账）',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '交易金额（正数）',
  `target_account_id` INT UNSIGNED NULL DEFAULT NULL COMMENT '转账目标账户（仅transfer类型）',
  `description` TEXT COMMENT '交易备注',
  `transaction_date` DATE NOT NULL COMMENT '交易日期',
  `transaction_time` TIME DEFAULT NULL COMMENT '交易时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_account` (`account_id`),
  KEY `idx_transaction_category` (`category_id`),
  KEY `idx_transaction_date` (`transaction_date`),
  KEY `idx_transaction_type` (`type`),
  KEY `idx_transaction_target_account` (`target_account_id`),
  CONSTRAINT `fk_transaction_account` FOREIGN KEY (`account_id`) REFERENCES `pab_accounts` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_transaction_category` FOREIGN KEY (`category_id`) REFERENCES `pab_categories` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_transaction_target_account` FOREIGN KEY (`target_account_id`) REFERENCES `pab_accounts` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='交易明细表';

-- 交易标签关联表
CREATE TABLE `pab_transaction_tags` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `transaction_id` INT UNSIGNED NOT NULL COMMENT '关联交易ID',
  `tag_id` INT UNSIGNED NOT NULL COMMENT '关联标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_tag` (`transaction_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`),
  CONSTRAINT `fk_transaction_tag_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `pab_transactions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transaction_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `pab_tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='交易标签关联表';