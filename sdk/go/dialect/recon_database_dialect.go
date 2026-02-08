package dialect

import (
	"database/sql"
)

// ReconDatabaseDialect 数据库方言接口
type ReconDatabaseDialect interface {
	// 获取数据库类型名称
	GetDatabaseType() string

	// 获取日期函数
	GetDateFunction() string

	// 获取当前时间函数
	GetCurrentTimeFunction() string
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
