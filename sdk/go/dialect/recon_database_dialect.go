package dialect

import (
	"database/sql"
	"fmt"
	"strings"
)

// ReconDatabaseDialect 数据库方言接口
type ReconDatabaseDialect interface {
	// 获取数据库类型名称
	GetDatabaseType() string

	// 获取日期函数
	GetDateFunction() string

	// 获取当前时间函数
	GetCurrentTimeFunction() string

	// 获取占位符
	GetPlaceholder(index int) string

	// GetCreateTableSQL 获取建表SQL
	GetCreateTableSQL() []string
}

// MySQLDialect MySQL方言实现
type MySQLDialect struct{}

// GetDatabaseType 获取数据库类型名称
func (d *MySQLDialect) GetDatabaseType() string {
	return "mysql"
}

// GetDateFunction 获取日期函数
func (d *MySQLDialect) GetDateFunction() string {
	return "DATE"
}

// GetCurrentTimeFunction 获取当前时间函数
func (d *MySQLDialect) GetCurrentTimeFunction() string {
	return "NOW()"
}

// GetPlaceholder 获取占位符 (MySQL uses ?)
func (d *MySQLDialect) GetPlaceholder(index int) string {
	return "?"
}

// GetCreateTableSQL 获取建表SQL (MySQL)
func (d *MySQLDialect) GetCreateTableSQL() []string {
	schema := `
CREATE TABLE IF NOT EXISTS easy_recon_order_main (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单号',
  merchant_id VARCHAR(64) NOT NULL COMMENT '商户 ID',
  merchant_name VARCHAR(128) COMMENT '商户名称',
  merchant_order_no VARCHAR(64) COMMENT '商户原始订单号',
  order_amount DECIMAL(20,2) NOT NULL COMMENT '订单金额',
  actual_amount DECIMAL(20,2) NOT NULL COMMENT '实付金额',
  platform_income DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '平台收入',
  pay_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '支付手续费',
  split_total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '分账总金额',
  pay_status TINYINT DEFAULT 0 COMMENT '支付状态：0=处理中，1=成功，2=失败',
  split_status TINYINT DEFAULT 0 COMMENT '分账状态：0=处理中，1=成功，2=失败',
  notify_status TINYINT DEFAULT 0 COMMENT '通知状态：0=处理中，1=成功，2=失败',
  recon_status TINYINT NOT NULL DEFAULT 0 COMMENT '对账状态：0=待对账，1=成功，2=失败',
  order_time DATETIME COMMENT '订单时间',
  pay_time DATETIME COMMENT '支付时间',
  recon_time DATETIME COMMENT '对账时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_main_recon_status (recon_status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账订单主记录';

CREATE TABLE IF NOT EXISTS easy_recon_order_refund_split_sub (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
	order_no VARCHAR(64) NOT NULL COMMENT '订单号',
	sub_order_no VARCHAR(64) COMMENT '子订单号',
	merchant_id VARCHAR(64) NOT NULL COMMENT '商户 ID',
	merchant_order_no VARCHAR(64) COMMENT '商户原始订单号',
	refund_split_amount DECIMAL(18,2) NOT NULL COMMENT '退款分账金额',
	create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (id),
	KEY idx_order_no (order_no),
	KEY idx_sub_order_no (sub_order_no),
	KEY idx_merchant_order_no (merchant_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账订单退款分账子记录';

CREATE TABLE IF NOT EXISTS easy_recon_order_split_sub (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单号',
  sub_order_no VARCHAR(64) NULL COMMENT '子订单号',
  merchant_id VARCHAR(64) NOT NULL COMMENT '商户 ID',
  split_amount DECIMAL(18,2) NOT NULL COMMENT '分账金额',
  status TINYINT DEFAULT 0 COMMENT '状态',
  notify_status TINYINT NOT NULL DEFAULT 2 COMMENT '通知状态 (0:失败, 1:成功, 2:待处理)',
  notify_result TEXT NULL COMMENT '通知返回结果',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_mch_sub (order_no, merchant_id, sub_order_no),
  KEY idx_sub_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账订单分账子记录';

CREATE TABLE IF NOT EXISTS easy_recon_exception (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单号',
  merchant_id VARCHAR(64) NOT NULL COMMENT '商户 ID',
  exception_type TINYINT DEFAULT 1 COMMENT '异常类型',
  exception_msg TEXT NOT NULL COMMENT '异常信息',
  exception_step TINYINT NOT NULL COMMENT '异常步骤：1=支付状态，2=分账状态，3=通知状态，4=金额校验，5=其他',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_exc_order_no (order_no),
  KEY idx_exc_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账异常记录';

CREATE TABLE IF NOT EXISTS easy_recon_summary (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  summary_date DATE NOT NULL COMMENT '统计日期',
  total_orders INT NOT NULL DEFAULT 0 COMMENT '总订单数',
  success_count INT NOT NULL DEFAULT 0 COMMENT '成功数',
  fail_count INT NOT NULL DEFAULT 0 COMMENT '失败数',
  init_count INT NOT NULL DEFAULT 0 COMMENT '初始数',
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_summary_date (summary_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账汇总统计';

CREATE TABLE IF NOT EXISTS easy_recon_notify_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  order_no VARCHAR(64) NOT NULL COMMENT '订单号',
  sub_order_no VARCHAR(64) NULL COMMENT '子订单号',
  merchant_id VARCHAR(64) NOT NULL COMMENT '商户 ID',
  notify_url VARCHAR(255) NOT NULL COMMENT '通知 URL',
  notify_status TINYINT NOT NULL DEFAULT 0 COMMENT '通知状态：0=失败，1=成功',
  notify_result TEXT COMMENT '通知结果',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_notify_log_order_no (order_no),
  KEY idx_notify_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账通知日志';
`
	return splitSQL(schema)
}

// PostgreSQLDialect PostgreSQL方言实现
type PostgreSQLDialect struct{}

// GetDatabaseType 获取数据库类型名称
func (d *PostgreSQLDialect) GetDatabaseType() string {
	return "postgresql"
}

// GetDateFunction 获取日期函数
func (d *PostgreSQLDialect) GetDateFunction() string {
	return "DATE"
}

// GetCurrentTimeFunction 获取当前时间函数
func (d *PostgreSQLDialect) GetCurrentTimeFunction() string {
	return "CURRENT_TIMESTAMP"
}

// GetPlaceholder 获取占位符 (PostgreSQL uses $1, $2, ...)
func (d *PostgreSQLDialect) GetPlaceholder(index int) string {
	return fmt.Sprintf("$%d", index)
}

// GetCreateTableSQL 获取建表SQL (PostgreSQL)
func (d *PostgreSQLDialect) GetCreateTableSQL() []string {
	schema := `
CREATE TABLE IF NOT EXISTS easy_recon_order_main (
  id BIGSERIAL PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  merchant_name VARCHAR(128),
  merchant_order_no VARCHAR(64),
  order_amount DECIMAL(20,2) NOT NULL,
  actual_amount DECIMAL(20,2) NOT NULL,
  platform_income DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  pay_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  split_total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  pay_status SMALLINT DEFAULT 0,
  split_status SMALLINT DEFAULT 0,
  notify_status SMALLINT DEFAULT 0,
  recon_status SMALLINT NOT NULL DEFAULT 0,
  order_time TIMESTAMP,
  pay_time TIMESTAMP,
  recon_time TIMESTAMP,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_easy_recon_order_no ON easy_recon_order_main (order_no);
CREATE INDEX IF NOT EXISTS idx_easy_recon_main_recon_status ON easy_recon_order_main (recon_status);
CREATE INDEX IF NOT EXISTS idx_easy_recon_main_create_time ON easy_recon_order_main (create_time);

CREATE TABLE IF NOT EXISTS easy_recon_order_refund_split_sub (
	id BIGSERIAL PRIMARY KEY,
	order_no VARCHAR(64) NOT NULL,
	sub_order_no VARCHAR(64),
	merchant_id VARCHAR(64) NOT NULL,
	merchant_order_no VARCHAR(64),
	refund_split_amount DECIMAL(18,2) NOT NULL,
	create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_easy_recon_refund_order_no ON easy_recon_order_refund_split_sub (order_no);
CREATE INDEX IF NOT EXISTS idx_easy_recon_refund_sub_order_no ON easy_recon_order_refund_split_sub (sub_order_no);
CREATE INDEX IF NOT EXISTS idx_easy_recon_refund_mch_order_no ON easy_recon_order_refund_split_sub (merchant_order_no);

CREATE TABLE IF NOT EXISTS easy_recon_order_split_sub (
  id BIGSERIAL PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  sub_order_no VARCHAR(64),
  merchant_id VARCHAR(64) NOT NULL,
  split_amount DECIMAL(18,2) NOT NULL,
  status SMALLINT DEFAULT 0,
  notify_status SMALLINT NOT NULL DEFAULT 2,
  notify_result TEXT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_easy_recon_order_mch_sub ON easy_recon_order_split_sub (order_no, merchant_id, sub_order_no);
CREATE INDEX IF NOT EXISTS idx_easy_recon_split_order_no ON easy_recon_order_split_sub (order_no);

CREATE TABLE IF NOT EXISTS easy_recon_exception (
  id BIGSERIAL PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  merchant_id VARCHAR(64) NOT NULL,
  exception_type SMALLINT DEFAULT 1,
  exception_msg TEXT NOT NULL,
  exception_step SMALLINT NOT NULL,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_easy_recon_exc_order_no ON easy_recon_exception (order_no);
CREATE INDEX IF NOT EXISTS idx_easy_recon_exc_create_time ON easy_recon_exception (create_time);

CREATE TABLE IF NOT EXISTS easy_recon_summary (
  id BIGSERIAL PRIMARY KEY,
  summary_date DATE NOT NULL,
  total_orders INT NOT NULL DEFAULT 0,
  success_count INT NOT NULL DEFAULT 0,
  fail_count INT NOT NULL DEFAULT 0,
  init_count INT NOT NULL DEFAULT 0,
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_easy_recon_summary_date ON easy_recon_summary (summary_date);

CREATE TABLE IF NOT EXISTS easy_recon_notify_log (
  id BIGSERIAL PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  sub_order_no VARCHAR(64),
  merchant_id VARCHAR(64) NOT NULL,
  notify_url VARCHAR(255) NOT NULL,
  notify_status SMALLINT NOT NULL DEFAULT 0,
  notify_result TEXT,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_easy_recon_notify_log_order_no ON easy_recon_notify_log (order_no);
CREATE INDEX IF NOT EXISTS idx_easy_recon_notify_log_create_time ON easy_recon_notify_log (create_time);
`
	return splitSQL(schema)
}

func splitSQL(schema string) []string {
	rawStmts := strings.Split(schema, ";")
	var stmts []string
	for _, s := range rawStmts {
		trimmed := strings.TrimSpace(s)
		if trimmed != "" {
			stmts = append(stmts, trimmed)
		}
	}
	return stmts
}

// CreateDialect 创建数据库方言
func CreateDialect(db *sql.DB) ReconDatabaseDialect {
	// 通过查询数据库类型来判断
	var dbType string
	err := db.QueryRow("SELECT DATABASE()").Scan(&dbType)
	if err == nil {
		return &MySQLDialect{}
	}

	// 尝试PostgreSQL
	err = db.QueryRow("SELECT current_database()").Scan(&dbType)
	if err == nil {
		return &PostgreSQLDialect{}
	}

	// 默认返回MySQL方言
	return &MySQLDialect{}
}
