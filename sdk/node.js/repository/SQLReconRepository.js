/**
 * SQL实现的对账存储库
 */
const ReconOrderMain = require('../entity/ReconOrderMain');
const ReconOrderSplitSub = require('../entity/ReconOrderSplitSub');
const ReconException = require('../entity/ReconException');

class SQLReconRepository {
  /**
   * 构造函数
   * @param {Object} connection - 数据库连接对象
   * @param {Object} dialect - 数据库方言
   */
  constructor(connection, dialect) {
    this.connection = connection;
    this.dialect = dialect;
  }

  /**
   * 保存对账订单主记录
   * @param {ReconOrderMain} orderMain - 对账订单主记录
   * @returns {Promise<boolean>} 保存结果
   */
  async saveOrderMain(orderMain) {
    const sql = `INSERT INTO recon_order_main 
                (order_no, merchant_id, merchant_name, order_amount, actual_amount, recon_status, 
                order_time, pay_time, recon_time, create_time, update_time) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`;

    try {
      await this.connection.execute(sql, [
        orderMain.orderNo,
        orderMain.merchantId,
        orderMain.merchantName,
        orderMain.orderAmount,
        orderMain.actualAmount,
        orderMain.reconStatus,
        orderMain.orderTime,
        orderMain.payTime,
        orderMain.reconTime,
        orderMain.createTime,
        orderMain.updateTime
      ]);
      return true;
    } catch (error) {
      console.error('保存订单主记录失败:', error);
      return false;
    }
  }

  /**
   * 批量保存分账子记录
   * @param {Array<ReconOrderSplitSub>} splitSubs - 分账子记录列表
   * @returns {Promise<boolean>} 保存结果
   */
  async batchSaveOrderSplitSub(splitSubs) {
    if (!splitSubs || splitSubs.length === 0) {
      return true;
    }

    const sql = `INSERT INTO recon_order_split_sub 
                (order_no, sub_order_no, merchant_id, split_amount, status, create_time, update_time) 
                VALUES (?, ?, ?, ?, ?, ?, ?)`;

    try {
      for (const sub of splitSubs) {
        await this.connection.execute(sql, [
          sub.orderNo,
          sub.subOrderNo,
          sub.merchantId,
          sub.splitAmount,
          sub.status,
          sub.createTime,
          sub.updateTime
        ]);
      }
      return true;
    } catch (error) {
      console.error('批量保存分账子记录失败:', error);
      return false;
    }
  }

  /**
   * 保存异常记录
   * @param {ReconException} exception - 异常记录
   * @returns {Promise<boolean>} 保存结果
   */
  async saveException(exception) {
    const sql = `INSERT INTO recon_exception 
                (order_no, merchant_id, exception_type, exception_msg, exception_step, 
                create_time, update_time) 
                VALUES (?, ?, ?, ?, ?, ?, ?)`;

    try {
      await this.connection.execute(sql, [
        exception.orderNo,
        exception.merchantId,
        exception.exceptionType,
        exception.exceptionMsg,
        exception.exceptionStep,
        exception.createTime,
        exception.updateTime
      ]);
      return true;
    } catch (error) {
      console.error('保存异常记录失败:', error);
      return false;
    }
  }

  /**
   * 批量保存异常记录
   * @param {Array<ReconException>} exceptions - 异常记录列表
   * @returns {Promise<boolean>} 保存结果
   */
  async batchSaveException(exceptions) {
    if (!exceptions || exceptions.length === 0) {
      return true;
    }

    const sql = `INSERT INTO recon_exception 
                (order_no, merchant_id, exception_type, exception_msg, exception_step, 
                create_time, update_time) 
                VALUES (?, ?, ?, ?, ?, ?, ?)`;

    try {
      for (const exception of exceptions) {
        await this.connection.execute(sql, [
          exception.orderNo,
          exception.merchantId,
          exception.exceptionType,
          exception.exceptionMsg,
          exception.exceptionStep,
          exception.createTime,
          exception.updateTime
        ]);
      }
      return true;
    } catch (error) {
      console.error('批量保存异常记录失败:', error);
      return false;
    }
  }

  /**
   * 根据订单号查询对账订单主记录
   * @param {string} orderNo - 订单号
   * @returns {Promise<ReconOrderMain|null>} 对账订单主记录
   */
  async getOrderMainByOrderNo(orderNo) {
    const sql = 'SELECT * FROM recon_order_main WHERE order_no = ?';

    try {
      const [rows] = await this.connection.execute(sql, [orderNo]);
      if (rows.length > 0) {
        return this._mapToOrderMain(rows[0]);
      }
      return null;
    } catch (error) {
      console.error('查询订单主记录失败:', error);
      return null;
    }
  }

  /**
   * 根据订单号查询分账子记录
   * @param {string} orderNo - 订单号
   * @returns {Promise<Array<ReconOrderSplitSub>>} 分账子记录列表
   */
  async getOrderSplitSubByOrderNo(orderNo) {
    const sql = 'SELECT * FROM recon_order_split_sub WHERE order_no = ?';

    try {
      const [rows] = await this.connection.execute(sql, [orderNo]);
      const result = [];
      for (const row of rows) {
        result.push(this._mapToOrderSplitSub(row));
      }
      return result;
    } catch (error) {
      console.error('查询分账子记录失败:', error);
      return [];
    }
  }

  /**
   * 查询指定日期的待核账订单（分页）
   * @param {string} dateStr - 日期字符串
   * @param {number} offset - 偏移量
   * @param {number} limit - 限制数量
   * @returns {Promise<Array<ReconOrderMain>>} 待核账订单列表
   */
  async getPendingReconOrders(dateStr, offset, limit) {
    const sql = `SELECT * FROM recon_order_main 
                WHERE ${this.dialect.getDateFunction()}(order_time) = ? AND recon_status = 0 
                LIMIT ? OFFSET ?`;

    try {
      const [rows] = await this.connection.execute(sql, [dateStr, limit, offset]);
      const result = [];
      for (const row of rows) {
        result.push(this._mapToOrderMain(row));
      }
      return result;
    } catch (error) {
      console.error('查询待核账订单失败:', error);
      return [];
    }
  }

  /**
   * 更新对账状态
   * @param {string} orderNo - 订单号
   * @param {number} reconStatus - 对账状态
   * @returns {Promise<boolean>} 更新结果
   */
  async updateReconStatus(orderNo, reconStatus) {
    const sql = `UPDATE recon_order_main SET recon_status = ?, update_time = ${this.dialect.getCurrentTimeFunction()} WHERE order_no = ?`;

    try {
      const [result] = await this.connection.execute(sql, [reconStatus, orderNo]);
      return result.affectedRows > 0;
    } catch (error) {
      console.error('更新对账状态失败:', error);
      return false;
    }
  }

  /**
   * 根据商户ID查询对账订单主记录（分页）
   * @param {string} merchantId - 商户ID
   * @param {string} startDate - 开始日期
   * @param {string} endDate - 结束日期
   * @param {number} reconStatus - 对账状态
   * @param {number} offset - 偏移量
   * @param {number} limit - 限制数量
   * @returns {Promise<Array<ReconOrderMain>>} 对账订单主记录列表
   */
  async getOrderMainByMerchantId(merchantId, startDate, endDate, reconStatus, offset, limit) {
    let sql = 'SELECT * FROM recon_order_main WHERE merchant_id = ?';
    const params = [merchantId];

    if (startDate) {
      sql += ` AND ${this.dialect.getDateFunction()}(order_time) >= ?`;
      params.push(startDate);
    }

    if (endDate) {
      sql += ` AND ${this.dialect.getDateFunction()}(order_time) <= ?`;
      params.push(endDate);
    }

    if (reconStatus !== undefined) {
      sql += ' AND recon_status = ?';
      params.push(reconStatus);
    }

    sql += ' LIMIT ? OFFSET ?';
    params.push(limit, offset);

    try {
      const [rows] = await this.connection.execute(sql, params);
      const result = [];
      for (const row of rows) {
        result.push(this._mapToOrderMain(row));
      }
      return result;
    } catch (error) {
      console.error('根据商户ID查询订单主记录失败:', error);
      return [];
    }
  }

  /**
   * 根据日期查询对账订单主记录（分页）
   * @param {string} dateStr - 日期字符串
   * @param {number} reconStatus - 对账状态
   * @param {number} offset - 偏移量
   * @param {number} limit - 限制数量
   * @returns {Promise<Array<ReconOrderMain>>} 对账订单主记录列表
   */
  async getOrderMainByDate(dateStr, reconStatus, offset, limit) {
    let sql = `SELECT * FROM recon_order_main WHERE ${this.dialect.getDateFunction()}(order_time) = ?`;
    const params = [dateStr];

    if (reconStatus !== undefined) {
      sql += ' AND recon_status = ?';
      params.push(reconStatus);
    }

    sql += ' LIMIT ? OFFSET ?';
    params.push(limit, offset);

    try {
      const [rows] = await this.connection.execute(sql, params);
      const result = [];
      for (const row of rows) {
        result.push(this._mapToOrderMain(row));
      }
      return result;
    } catch (error) {
      console.error('根据日期查询订单主记录失败:', error);
      return [];
    }
  }

  /**
   * 查询对账异常记录（分页）
   * @param {string} merchantId - 商户ID
   * @param {string} startDate - 开始日期
   * @param {string} endDate - 结束日期
   * @param {number} exceptionStep - 异常步骤
   * @param {number} offset - 偏移量
   * @param {number} limit - 限制数量
   * @returns {Promise<Array<ReconException>>} 对账异常记录列表
   */
  async getExceptionRecords(merchantId, startDate, endDate, exceptionStep, offset, limit) {
    let sql = 'SELECT * FROM recon_exception WHERE 1=1';
    const params = [];

    if (merchantId) {
      sql += ' AND merchant_id = ?';
      params.push(merchantId);
    }

    if (startDate) {
      sql += ` AND ${this.dialect.getDateFunction()}(create_time) >= ?`;
      params.push(startDate);
    }

    if (endDate) {
      sql += ` AND ${this.dialect.getDateFunction()}(create_time) <= ?`;
      params.push(endDate);
    }

    if (exceptionStep !== undefined) {
      sql += ' AND exception_step = ?';
      params.push(exceptionStep);
    }

    sql += ' LIMIT ? OFFSET ?';
    params.push(limit, offset);

    try {
      const [rows] = await this.connection.execute(sql, params);
      const result = [];
      for (const row of rows) {
        result.push(this._mapToException(row));
      }
      return result;
    } catch (error) {
      console.error('查询异常记录失败:', error);
      return [];
    }
  }

  /**
   * 根据订单号查询对账异常记录
   * @param {string} orderNo - 订单号
   * @returns {Promise<ReconException|null>} 对账异常记录
   */
  async getExceptionByOrderNo(orderNo) {
    const sql = 'SELECT * FROM recon_exception WHERE order_no = ?';

    try {
      const [rows] = await this.connection.execute(sql, [orderNo]);
      if (rows.length > 0) {
        return this._mapToException(rows[0]);
      }
      return null;
    } catch (error) {
      console.error('查询异常记录失败:', error);
      return null;
    }
  }

  /**
   * 将数据库行映射为订单主记录
   * @param {Object} row - 数据库行
   * @returns {ReconOrderMain} 订单主记录
   * @private
   */
  _mapToOrderMain(row) {
    const orderMain = new ReconOrderMain();
    orderMain.id = row.id;
    orderMain.orderNo = row.order_no;
    orderMain.merchantId = row.merchant_id;
    orderMain.merchantName = row.merchant_name;
    orderMain.orderAmount = row.order_amount;
    orderMain.actualAmount = row.actual_amount;
    orderMain.reconStatus = row.recon_status;
    orderMain.orderTime = row.order_time;
    orderMain.payTime = row.pay_time;
    orderMain.reconTime = row.recon_time;
    orderMain.createTime = row.create_time;
    orderMain.updateTime = row.update_time;
    return orderMain;
  }

  /**
   * 将数据库行映射为分账子记录
   * @param {Object} row - 数据库行
   * @returns {ReconOrderSplitSub} 分账子记录
   * @private
   */
  _mapToOrderSplitSub(row) {
    const sub = new ReconOrderSplitSub();
    sub.id = row.id;
    sub.orderNo = row.order_no;
    sub.subOrderNo = row.sub_order_no;
    sub.merchantId = row.merchant_id;
    sub.splitAmount = row.split_amount;
    sub.status = row.status;
    sub.createTime = row.create_time;
    sub.updateTime = row.update_time;
    return sub;
  }

  /**
   * 将数据库行映射为异常记录
   * @param {Object} row - 数据库行
   * @returns {ReconException} 异常记录
   * @private
   */
  _mapToException(row) {
    const exception = new ReconException();
    exception.id = row.id;
    exception.orderNo = row.order_no;
    exception.merchantId = row.merchant_id;
    exception.exceptionType = row.exception_type;
    exception.exceptionMsg = row.exception_msg;
    exception.exceptionStep = row.exception_step;
    exception.createTime = row.create_time;
    exception.updateTime = row.update_time;
    return exception;
  }
}

module.exports = SQLReconRepository;
