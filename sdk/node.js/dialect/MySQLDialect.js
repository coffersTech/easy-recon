/**
 * MySQL 数据库方言实现
 */
const { ReconDatabaseDialect } = require('./ReconDatabaseDialect');

class MySQLDialect extends ReconDatabaseDialect {
    /**
     * 获取数据库类型
     */
    getDatabaseType() {
        return 'mysql';
    }

    /**
     * 获取建表 SQL 语句
     * @returns {Array<string>} 建表 SQL 语句列表
     */
    getCreateTableSQL() {
        const tablePrefix = 'easy_recon_'; // Fixed prefix as per Go/Java SDK alignment
        return [
            `CREATE TABLE IF NOT EXISTS ${tablePrefix}order_main (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL COMMENT '业务订单号',
        merchant_id VARCHAR(64) NOT NULL COMMENT '商户ID',
        merchant_name VARCHAR(128) COMMENT '商户名称',
        order_amount DECIMAL(20,2) NOT NULL COMMENT '订单金额',
        pay_amount DECIMAL(20,2) COMMENT '支付金额',
        platform_income DECIMAL(20,2) COMMENT '平台收入',
        pay_fee DECIMAL(20,2) COMMENT '支付手续费',
        refund_amount DECIMAL(20,2) DEFAULT 0 COMMENT '退款金额',
        actual_amount DECIMAL(20,2) COMMENT '实付金额',
        pay_status TINYINT COMMENT '支付状态',
        split_status TINYINT COMMENT '分账状态',
        notify_status TINYINT COMMENT '通知状态',
        refund_status TINYINT DEFAULT 0 COMMENT '退款状态',
        recon_status TINYINT DEFAULT 0 COMMENT '对账状态:0-未对账,1-成功,2-失败',
        order_time DATETIME COMMENT '下单时间',
        pay_time DATETIME COMMENT '支付时间',
        refund_time DATETIME COMMENT '退款时间',
        recon_time DATETIME COMMENT '对账时间',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_order_no (order_no),
        KEY idx_merchant_time (merchant_id, order_time),
        KEY idx_recon_status (recon_status, order_time)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账主订单表'`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}order_split_sub (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL COMMENT '关联主订单号',
        sub_order_no VARCHAR(64) NOT NULL COMMENT '子订单号',
        merchant_id VARCHAR(64) NOT NULL COMMENT '分账商户ID',
        merchant_order_no VARCHAR(64) COMMENT '商户原始订单号',
        split_amount DECIMAL(20,2) NOT NULL COMMENT '分账金额',
        status TINYINT COMMENT '分账状态',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_sub_order (sub_order_no, merchant_id),
        KEY idx_order_no (order_no)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单分账子表'`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}order_refund_split_sub (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL COMMENT '关联主订单号',
        sub_order_no VARCHAR(64) NOT NULL COMMENT '关联子订单号',
        merchant_id VARCHAR(64) NOT NULL COMMENT '分账商户ID',
        refund_split_amount DECIMAL(20,2) NOT NULL COMMENT '退款分账金额',
        status TINYINT COMMENT '状态',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        KEY idx_order_sub (order_no, sub_order_no)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单退款分账子表'`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}notify_log (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL COMMENT '业务订单号',
        merchant_id VARCHAR(64) COMMENT '商户ID',
        sub_order_no VARCHAR(64) COMMENT '子订单号',
        merchant_order_no VARCHAR(64) COMMENT '商户原始订单号',
        notify_url VARCHAR(255) COMMENT '通知地址',
        notify_status TINYINT COMMENT '通知状态',
        notify_result TEXT COMMENT '通知结果',
        notify_count INT DEFAULT 1 COMMENT '通知次数',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        KEY idx_order_no (order_no)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账通知日志表'`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}summary (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        recon_date DATE NOT NULL COMMENT '对账日期',
        total_orders INT DEFAULT 0 COMMENT '总订单数',
        total_amount DECIMAL(20,2) DEFAULT 0 COMMENT '总金额',
        success_count INT DEFAULT 0 COMMENT '成功数',
        fail_count INT DEFAULT 0 COMMENT '失败数',
        exception_count INT DEFAULT 0 COMMENT '异常数',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_recon_date (recon_date)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日对账汇总表'`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}exception (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL COMMENT '业务订单号',
        merchant_id VARCHAR(64) COMMENT '商户ID',
        exception_type TINYINT COMMENT '异常类型',
        exception_msg VARCHAR(255) COMMENT '异常信息',
        exception_step TINYINT COMMENT '异常步骤',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        KEY idx_order_no (order_no)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账异常表'`
        ];
    }

    /**
     * 获取日期函数
     * @returns {string} 日期函数名
     */
    getDateFunction() {
        return 'DATE';
    }

    /**
     * 获取当前时间函数
     * @returns {string} 当前时间函数名
     */
    getCurrentTimeFunction() {
        return 'NOW()';
    }
}

module.exports = MySQLDialect;
