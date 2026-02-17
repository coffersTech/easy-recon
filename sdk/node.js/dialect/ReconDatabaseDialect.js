/**
 * 数据库方言抽象类
 */
class ReconDatabaseDialect {
  /**
   * 获取数据库类型名称
   */
  getDatabaseType() {
    throw new Error('子类必须实现getDatabaseType方法');
  }

  /**
   * 获取建表 SQL 语句
   * @returns {Array<string>}
   */
  getCreateTableSQL() {
    throw new Error('子类必须实现getCreateTableSQL方法');
  }

  /**
   * 获取日期函数
   */
  getDateFunction() {
    throw new Error('子类必须实现getDateFunction方法');
  }

  /**
   * 获取当前时间函数
   */
  getCurrentTimeFunction() {
    throw new Error('子类必须实现getCurrentTimeFunction方法');
  }
}

/**
 * 创建数据库方言
 * @param {Object} connection - 数据库连接对象
 * @returns {ReconDatabaseDialect} 数据库方言实例
 */
function createDialect(connection) {
  // 简单判断: mysql2 连接通常有 config 属性，pg 连接有 connectionParameters
  if (connection.config && connection.config.namedPlaceholders !== undefined) {
    const MySQLDialect = require('./MySQLDialect');
    return new MySQLDialect();
  } else if (connection.connectionParameters || (connection.options && connection.options.dialect === 'postgres')) {
    const PostgreSQLDialect = require('./PostgreSQLDialect');
    return new PostgreSQLDialect();
  } else {
    // 默认尝试 MySQL，或者抛出错误
    const MySQLDialect = require('./MySQLDialect');
    return new MySQLDialect();
  }
}

module.exports = {
  ReconDatabaseDialect,
  createDialect
};
