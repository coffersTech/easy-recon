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
 * MySQL方言实现
 */
class MySQLDialect extends ReconDatabaseDialect {
  getDatabaseType() {
    return 'mysql';
  }

  getDateFunction() {
    return 'DATE';
  }

  getCurrentTimeFunction() {
    return 'NOW()';
  }
}

/**
 * PostgreSQL方言实现
 */
class PostgreSQLDialect extends ReconDatabaseDialect {
  getDatabaseType() {
    return 'postgresql';
  }

  getDateFunction() {
    return 'DATE';
  }

  getCurrentTimeFunction() {
    return 'CURRENT_TIMESTAMP';
  }
}

/**
 * 创建数据库方言
 * @param {Object} connection - 数据库连接对象
 * @returns {ReconDatabaseDialect} 数据库方言实例
 */
function createDialect(connection) {
  // 通过连接对象判断数据库类型
  if (connection.constructor.name.includes('MySQL')) {
    return new MySQLDialect();
  } else if (connection.constructor.name.includes('Client') && connection.connectionParameters) {
    // PostgreSQL连接对象
    return new PostgreSQLDialect();
  } else {
    // 默认返回MySQL方言
    return new MySQLDialect();
  }
}

module.exports = {
  ReconDatabaseDialect,
  MySQLDialect,
  PostgreSQLDialect,
  createDialect
};
