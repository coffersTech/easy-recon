-- Easy Recon MySQL 初始化全量脚本 (适用于 1.0.1+ 版本全量部署)
-- 包含：主订单、业务子单(意图)、分账明细(事实)、退款明细、商户结算统计、异常记录及对账规则

-- 1. 对账订单主记录
CREATE TABLE IF NOT EXISTS `easy_recon_order_main` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `pay_amount` DECIMAL(18,2) NOT NULL COMMENT '实付金额',
  `platform_income` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '平台收入',
  `pay_fee` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '支付手续费',
  `split_total_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '分账总金额',
  `pay_amount_fen` BIGINT COMMENT '实付金额（分）',
  `platform_income_fen` BIGINT COMMENT '平台收入（分）',
  `pay_fee_fen` BIGINT COMMENT '支付手续费（分）',
  `split_total_amount_fen` BIGINT COMMENT '分账总金额（分）',
  `refund_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '退款金额',
  `refund_amount_fen` BIGINT COMMENT '退款金额（分）',
  `refund_status` TINYINT DEFAULT 0 COMMENT '退款状态：0=未退款，1=部分退款，2=全额退款',
  `refund_time` DATETIME COMMENT '退款时间',
  `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态：0=处理中，1=成功，2=失败',
  `split_status` TINYINT DEFAULT 0 COMMENT '分账状态：0=处理中，1=成功，2=失败',
  `notify_status` TINYINT DEFAULT 0 COMMENT '通知状态：0=处理中，1=成功，2=失败',
  `notify_result` TEXT COMMENT '最新通知结果',
  `recon_status` TINYINT NOT NULL DEFAULT 0 COMMENT '对账状态：0=待对账，1=成功，2=失败',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_main_recon_status` (`recon_status`),
  KEY `idx_main_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账订单主记录';

-- 2. 业务子订单表 (意图层)
CREATE TABLE IF NOT EXISTS `easy_recon_order_sub` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '主订单号',
  `sub_order_no` VARCHAR(64) NULL COMMENT '业务子订单号',
  `merchant_order_no` VARCHAR(64) NULL COMMENT '商户原始订单号',
  `merchant_id` VARCHAR(64) NOT NULL COMMENT '商户 ID',
  `order_amount` DECIMAL(18,2) NULL COMMENT '订单金额',
  `order_amount_fen` BIGINT NULL COMMENT '订单金额（分）',
  `split_amount` DECIMAL(18,2) NULL COMMENT '分账金额',
  `split_amount_fen` BIGINT NULL COMMENT '分账金额（分）',
  `fee` DECIMAL(18,2) NULL COMMENT '手续费',
  `fee_fen` BIGINT NULL COMMENT '手续费（分）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_mch_sub` (`order_no`, `merchant_id`, `sub_order_no`),
  KEY `idx_sub_order_no` (`order_no`),
  KEY `idx_sub_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务子订单表';

-- 3. 分账事实明细表 (事实层)
CREATE TABLE IF NOT EXISTS `easy_recon_order_split_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `merchant_id` VARCHAR(64) NOT NULL COMMENT '商户 ID',
  `split_amount` DECIMAL(18,2) NOT NULL COMMENT '分账金额',
  `split_amount_fen` BIGINT COMMENT '分账金额（分）',
  `arrival_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '实际到账金额 (元)',
  `arrival_amount_fen` BIGINT COMMENT '实际到账金额 (分)',
  `split_fee` DECIMAL(18,2) DEFAULT 0.00 COMMENT '分账手续费 (元)',
  `split_fee_fen` BIGINT COMMENT '分账手续费 (分)',
  `notify_status` TINYINT NOT NULL DEFAULT 2 COMMENT '通知状态 (0:失败, 1:成功, 2:待处理)',
  `notify_result` TEXT NULL COMMENT '通知返回结果',
  `settlement_type` TINYINT UNSIGNED NULL COMMENT '判定后的清算类型',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_mch_split` (`order_no`, `merchant_id`),
  KEY `idx_split_order` (`order_no`),
  KEY `idx_split_mch` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分账事实明细表';

-- 4. 退款事实明细表 (事实层)
CREATE TABLE IF NOT EXISTS `easy_recon_order_refund_detail` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `merchant_id` VARCHAR(64) NOT NULL COMMENT '商户 ID',
  `refund_split_amount` DECIMAL(18,2) NOT NULL COMMENT '退款分账金额',
  `refund_split_amount_fen` BIGINT COMMENT '退款分账金额（分）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_refund_order` (`order_no`),
  KEY `idx_refund_mch` (`merchant_id`),
  KEY `idx_refund_order_mch` (`order_no`, `merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款事实明细表';

-- 5. 商户维度结算统计表 (结算层)
CREATE TABLE IF NOT EXISTS `easy_recon_order_merchant_settlement` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '主订单号',
  `merchant_id` VARCHAR(64) NOT NULL COMMENT '商户 ID',
  `settlement_type` TINYINT UNSIGNED NULL COMMENT '推算的到账方式 (1:平台代收, 2:全额到商户, 3:空中分账)',
  `order_amount_fen` BIGINT DEFAULT 0 COMMENT '订单意图金额 (分)',
  `split_amount_fen` BIGINT DEFAULT 0 COMMENT '分账事实金额 (分)',
  `split_ratio` INT DEFAULT 0 COMMENT '分账比例 (基点)',
  `split_fee_fen` BIGINT DEFAULT 0 COMMENT '分账手续费 (分)',
  `arrival_amount_fen` BIGINT DEFAULT 0 COMMENT '实际到账金额 (分)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_merchant` (`order_no`, `merchant_id`),
  KEY `idx_mch_settle_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户维度结算统计表';

-- 6. 对账异常记录
CREATE TABLE IF NOT EXISTS `easy_recon_exception` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `merchant_id` VARCHAR(64) NOT NULL COMMENT '商户 ID',
  `exception_msg` TEXT NOT NULL COMMENT '异常信息',
  `exception_step` TINYINT NOT NULL COMMENT '异常步骤：1=支付状态，2=分账状态，3=通知状态，4=金额校验，5=其他',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exc_order_no` (`order_no`),
  KEY `idx_exc_merchant_id` (`merchant_id`),
  KEY `idx_exc_step` (`exception_step`),
  KEY `idx_exc_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账异常记录';

-- 7. 对账通知日志
CREATE TABLE IF NOT EXISTS `easy_recon_notify_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `sub_order_no` VARCHAR(64) NULL COMMENT '子订单号',
  `merchant_id` VARCHAR(64) NOT NULL COMMENT '商户 ID',
  `notify_url` VARCHAR(255) NOT NULL COMMENT '通知 URL',
  `notify_status` TINYINT NOT NULL DEFAULT 0 COMMENT '通知状态：0=失败，1=成功',
  `notify_result` TEXT COMMENT '通知结果',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no_mch_sub_notify` (`order_no`, `merchant_id`, `sub_order_no`),
  KEY `idx_notify_log_merchant_id` (`merchant_id`),
  KEY `idx_notify_log_status` (`notify_status`),
  KEY `idx_notify_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账通知日志';

-- 8. 对账规则表
CREATE TABLE IF NOT EXISTS `easy_recon_rule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
  `rule_type` TINYINT NOT NULL COMMENT '规则类型：1=金额规则，2=状态规则，3=其他规则',
  `rule_expression` TEXT NOT NULL COMMENT '规则表达式',
  `rule_desc` VARCHAR(255) COMMENT '规则描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_name` (`rule_name`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账规则';

-- 插入默认对账规则
INSERT INTO `easy_recon_rule` (`rule_name`, `rule_type`, `rule_expression`, `rule_desc`, `status`, `create_time`, `update_time`)
VALUES 
('默认金额规则', 1, 'payAmount = splitTotalAmount + platformIncome + payFee', '默认金额等式校验规则', 1, NOW(), NOW()),
('默认状态规则', 2, 'payStatus && notifyStatus', '默认状态校验规则', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);
