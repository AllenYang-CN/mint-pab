-- 初始化管理员用户
INSERT INTO `sys_user` (`username`, `password`, `salt`, `token`, `token_expire_time`, `login_fail_count`, `last_login_time`, `create_time`, `update_time`, `is_deleted`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'fixed_salt_admin', NULL, NULL, 0, NULL, NOW(), NOW(), 0);

-- 初始化系统预置分类（组合模式：一级分类 + 二级分类）
-- 一级分类（parent_id = NULL）
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(1, 0, NULL, '收入', 1, 1, 1, '#67C23A', 'el-icon-coin', NOW(), NOW(), 0),
(2, 0, NULL, '餐饮', 2, 1, 10, '#E6A23C', 'el-icon-food', NOW(), NOW(), 0),
(3, 0, NULL, '交通', 2, 1, 20, '#409EFF', 'el-icon-truck', NOW(), NOW(), 0),
(4, 0, NULL, '购物', 2, 1, 30, '#F56C6C', 'el-icon-shopping-bag-1', NOW(), NOW(), 0),
(5, 0, NULL, '居住', 2, 1, 40, '#909399', 'el-icon-house', NOW(), NOW(), 0),
(6, 0, NULL, '娱乐', 2, 1, 50, '#E040FB', 'el-icon-film', NOW(), NOW(), 0),
(7, 0, NULL, '医疗', 2, 1, 60, '#00BCD4', 'el-icon-first-aid-kit', NOW(), NOW(), 0),
(8, 0, NULL, '教育', 2, 1, 70, '#FF9800', 'el-icon-reading', NOW(), NOW(), 0),
(9, 0, NULL, '转账', 2, 1, 80, '#8BC34A', 'el-icon-sort', NOW(), NOW(), 0);

-- 二级分类：收入
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(10, 0, 1, '工资', 1, 1, 1, '#67C23A', 'el-icon-money', NOW(), NOW(), 0),
(11, 0, 1, '奖金', 1, 1, 2, '#67C23A', 'el-icon-trophy', NOW(), NOW(), 0),
(12, 0, 1, '投资收益', 1, 1, 3, '#67C23A', 'el-icon-data-line', NOW(), NOW(), 0),
(13, 0, 1, '兼职', 1, 1, 4, '#67C23A', 'el-icon-suitcase', NOW(), NOW(), 0),
(14, 0, 1, '红包', 1, 1, 5, '#67C23A', 'el-icon-present', NOW(), NOW(), 0);

-- 二级分类：餐饮
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(15, 0, 2, '外卖', 2, 1, 10, '#E6A23C', 'el-icon-takeaway-box', NOW(), NOW(), 0),
(16, 0, 2, '堂食', 2, 1, 11, '#E6A23C', 'el-icon-bowl', NOW(), NOW(), 0),
(17, 0, 2, '食材', 2, 1, 12, '#E6A23C', 'el-icon-apple', NOW(), NOW(), 0),
(18, 0, 2, '零食饮料', 2, 1, 13, '#E6A23C', 'el-icon-coffee', NOW(), NOW(), 0);

-- 二级分类：交通
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(19, 0, 3, '公共交通', 2, 1, 20, '#409EFF', 'el-icon-bus', NOW(), NOW(), 0),
(20, 0, 3, '打车', 2, 1, 21, '#409EFF', 'el-icon-car', NOW(), NOW(), 0),
(21, 0, 3, '加油', 2, 1, 22, '#409EFF', 'el-icon-gas', NOW(), NOW(), 0),
(22, 0, 3, '停车', 2, 1, 23, '#409EFF', 'el-icon-parking', NOW(), NOW(), 0);

-- 二级分类：购物
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(23, 0, 4, '服饰鞋包', 2, 1, 30, '#F56C6C', 'el-icon-goods', NOW(), NOW(), 0),
(24, 0, 4, '日用百货', 2, 1, 31, '#F56C6C', 'el-icon-box', NOW(), NOW(), 0),
(25, 0, 4, '数码电子', 2, 1, 32, '#F56C6C', 'el-icon-mobile-phone', NOW(), NOW(), 0);

-- 二级分类：居住
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(26, 0, 5, '房租', 2, 1, 40, '#909399', 'el-icon-office-building', NOW(), NOW(), 0),
(27, 0, 5, '水电煤', 2, 1, 41, '#909399', 'el-icon-lightbulb', NOW(), NOW(), 0),
(28, 0, 5, '物业', 2, 1, 42, '#909399', 'el-icon-key', NOW(), NOW(), 0),
(29, 0, 5, '维修', 2, 1, 43, '#909399', 'el-icon-setting', NOW(), NOW(), 0);

-- 二级分类：娱乐
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(30, 0, 6, '电影演出', 2, 1, 50, '#E040FB', 'el-icon-film', NOW(), NOW(), 0),
(31, 0, 6, '游戏', 2, 1, 51, '#E040FB', 'el-icon-monitor', NOW(), NOW(), 0),
(32, 0, 6, '旅游', 2, 1, 52, '#E040FB', 'el-icon-place', NOW(), NOW(), 0),
(33, 0, 6, '会员订阅', 2, 1, 53, '#E040FB', 'el-icon-user', NOW(), NOW(), 0);

-- 二级分类：医疗
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(34, 0, 7, '药品', 2, 1, 60, '#00BCD4', 'el-icon-capsule', NOW(), NOW(), 0),
(35, 0, 7, '诊疗', 2, 1, 61, '#00BCD4', 'el-icon-stethoscope', NOW(), NOW(), 0),
(36, 0, 7, '体检', 2, 1, 62, '#00BCD4', 'el-icon-heartbeat', NOW(), NOW(), 0);

-- 二级分类：教育
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(37, 0, 8, '书籍', 2, 1, 70, '#FF9800', 'el-icon-notebook-2', NOW(), NOW(), 0),
(38, 0, 8, '课程培训', 2, 1, 71, '#FF9800', 'el-icon-school', NOW(), NOW(), 0),
(39, 0, 8, '考试', 2, 1, 72, '#FF9800', 'el-icon-document', NOW(), NOW(), 0);

-- 二级分类：转账
INSERT INTO `category` (`id`, `user_id`, `parent_id`, `name`, `type`, `is_system`, `sort_order`, `color`, `icon`, `create_time`, `update_time`, `is_deleted`) VALUES
(40, 0, 9, '账户间转账', 2, 1, 80, '#8BC34A', 'el-icon-sort', NOW(), NOW(), 0);
