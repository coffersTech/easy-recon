/**
 * PostgreSQL 数据库方言实现
 */
const { ReconDatabaseDialect } = require('./ReconDatabaseDialect');

class PostgreSQLDialect extends ReconDatabaseDialect {
    /**
     * 获取数据库类型
     */
    getDatabaseType() {
        return 'postgresql';
    }

    /**
     * 获取建表 SQL 语句
     * @returns {Array<string>} 建表 SQL 语句列表
     */
    getCreateTableSQL() {
        const tablePrefix = 'easy_recon_'; // Fixed prefix
        return [
            `CREATE TABLE IF NOT EXISTS ${tablePrefix}order_main (
        id SERIAL PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL,
        merchant_id VARCHAR(64) NOT NULL,
        merchant_name VARCHAR(128),
        order_amount DECIMAL(20,2) NOT NULL,
        pay_amount DECIMAL(20,2),
        platform_income DECIMAL(20,2),
        pay_fee DECIMAL(20,2),
        refund_amount DECIMAL(20,2) DEFAULT 0,
        actual_amount DECIMAL(20,2),
        pay_status SMALLINT,
        split_status SMALLINT,
        notify_status SMALLINT,
        refund_status SMALLINT DEFAULT 0,
        recon_status SMALLINT DEFAULT 0,
        order_time TIMESTAMP,
        pay_time TIMESTAMP,
        refund_time TIMESTAMP,
        recon_time TIMESTAMP,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )`,
            `CREATE UNIQUE INDEX IF NOT EXISTS uk_order_no ON ${tablePrefix}order_main (order_no)`,
            `CREATE INDEX IF NOT EXISTS idx_merchant_time ON ${tablePrefix}order_main (merchant_id, order_time)`,
            `CREATE INDEX IF NOT EXISTS idx_recon_status ON ${tablePrefix}order_main (recon_status, order_time)`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}order_split_sub (
        id SERIAL PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL,
        sub_order_no VARCHAR(64) NOT NULL,
        merchant_id VARCHAR(64) NOT NULL,
        merchant_order_no VARCHAR(64),
        split_amount DECIMAL(20,2) NOT NULL,
        status SMALLINT,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )`,
            `CREATE UNIQUE INDEX IF NOT EXISTS uk_sub_order ON ${tablePrefix}order_split_sub (sub_order_no, merchant_id)`,
            `CREATE INDEX IF NOT EXISTS idx_split_order_no ON ${tablePrefix}order_split_sub (order_no)`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}order_refund_split_sub (
        id SERIAL PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL,
        sub_order_no VARCHAR(64) NOT NULL,
        merchant_id VARCHAR(64) NOT NULL,
        refund_split_amount DECIMAL(20,2) NOT NULL,
        status SMALLINT,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )`,
            `CREATE INDEX IF NOT EXISTS idx_order_sub ON ${tablePrefix}order_refund_split_sub (order_no, sub_order_no)`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}notify_log (
        id SERIAL PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL,
        merchant_id VARCHAR(64),
        sub_order_no VARCHAR(64),
        merchant_order_no VARCHAR(64),
        notify_url VARCHAR(255),
        notify_status SMALLINT,
        notify_result TEXT,
        notify_count INT DEFAULT 1,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )`,
            `CREATE INDEX IF NOT EXISTS idx_notify_order_no ON ${tablePrefix}notify_log (order_no)`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}summary (
        id SERIAL PRIMARY KEY,
        recon_date DATE NOT NULL,
        total_orders INT DEFAULT 0,
        total_amount DECIMAL(20,2) DEFAULT 0,
        success_count INT DEFAULT 0,
        fail_count INT DEFAULT 0,
        exception_count INT DEFAULT 0,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )`,
            `CREATE UNIQUE INDEX IF NOT EXISTS uk_recon_date ON ${tablePrefix}summary (recon_date)`,

            `CREATE TABLE IF NOT EXISTS ${tablePrefix}exception (
        id SERIAL PRIMARY KEY,
        order_no VARCHAR(64) NOT NULL,
        merchant_id VARCHAR(64),
        exception_type SMALLINT,
        exception_msg VARCHAR(255),
        exception_step SMALLINT,
        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )`,
            `CREATE INDEX IF NOT EXISTS idx_exception_order_no ON ${tablePrefix}exception (order_no)`
        ];
    }

    /**
     * 获取日期函数
     * @returns {string} 日期函数名
     */
    getDateFunction() {
        // PostgreSQL use ::DATE cast or DATE() depending on version, generic way is cast
        return 'DATE';
    }

    /**
     * 获取当前时间函数
     * @returns {string} 当前时间函数名
     */
    getCurrentTimeFunction() {
        return 'CURRENT_TIMESTAMP';
    }
}

module.exports = PostgreSQLDialect;
