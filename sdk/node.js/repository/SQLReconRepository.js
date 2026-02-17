/**
 * SQL实现的对账存储库
 */
const ReconOrderMain = require('../entity/ReconOrderMain');
const ReconOrderSplitSub = require('../entity/ReconOrderSplitSub');
const ReconOrderRefundSplitSub = require('../entity/ReconOrderRefundSplitSub');
const ReconNotifyLog = require('../entity/ReconNotifyLog');
const ReconSummary = require('../entity/ReconSummary');
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
    this.isPg = dialect.getDatabaseType() === 'postgresql';
  }

  // --- Helper to execute SQL (mysql2 supports execute, pg supports query with placeholders differently) ---
  // For simplicity assuming mysql2 'execute' style or wrapping pg. 
  // Real implementation needs adapter. Here assuming a compatible `execute(sql, params)` method exists on connection.

  /**
   * Internal helper to execute SQL compatible with both mysql2 and pg
   * @param {string} sql 
   * @param {Array} params 
   */
  async _execute(sql, params) {
    if (this.isPg) {
      // Convert ? to $1, $2...
      let paramIdx = 1;
      const pgSql = sql.replace(/\?/g, () => `$${paramIdx++}`);

      // PG uses .query()
      const result = await this.connection.query(pgSql, params);

      // Mimic mysql2 return signature [rows] or [result] where result has affectedRows
      // For Select: result.rows
      // For Insert/Update: result.rowCount -> we map to object with affectedRows
      if (Array.isArray(result.rows)) {
        // attach affectedRows for compatibility if needed, or just return rows
        // But for Update we need affectedRows. 
        // pg result object has rowCount.
        // We return [result.rows] to match [rows] destructuring
        // But if it's an update, result.rows might be empty.
        const resObj = result.rows;
        resObj.affectedRows = result.rowCount; // monkey patch for code needing affectedRows
        return [resObj];
      }
      return [result];
    } else {
      try {
        return await this.connection.execute(sql, params);
      } catch (e) {
        // Fallback for mysql2 non-prepared statements if execute fails or if using pool in some way
        return await this.connection.query(sql, params);
      }
    }
  }

  /**
   * 保存对账订单主记录
   */
  async saveOrderMain(orderMain) {
    // Upsert logic is better but standard insert is fine for now
    // Note: Node.js mysql2 driver '?' placeholders, pg uses $1, $2. 
    // The dialect abstraction should handle query generation or we use a basic ORM query builder.
    // For this implementation, we assume the user provides a connection wrapper or we handle simple SQL.
    // To match Go/Java, we just use placeholders ? and assume the driver or dialect handles it.
    // But PG uses $1. We might need dialect to format query.
    // For brevity, we assume MySQL syntax here mainly, or use a helper. 
    // Let's assume standard ? for now as in the original code.

    const sql = `INSERT INTO easy_recon_order_main 
      (order_no, merchant_id, merchant_name, order_amount, pay_amount, platform_income, pay_fee, refund_amount, actual_amount,
       pay_status, split_status, notify_status, refund_status, recon_status,
       order_time, pay_time, refund_time, recon_time, create_time, update_time) 
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON DUPLICATE KEY UPDATE 
      pay_status=VALUES(pay_status), split_status=VALUES(split_status), notify_status=VALUES(notify_status), 
      refund_status=VALUES(refund_status), recon_status=VALUES(recon_status), update_time=NOW()`;
    // Note: ON DUPLICATE KEY UPDATE is MySQL specific. PG uses ON CONFLICT.
    // Ideally we split this by dialect, but for now implementing basic Insert.

    // Using standard Insert for now, logic level handling of existing orders might be needed if not using Upsert.
    // But original code used Insert.

    const insertSql = `INSERT INTO easy_recon_order_main 
      (order_no, merchant_id, merchant_name, order_amount, pay_amount, platform_income, pay_fee, refund_amount, actual_amount,
       pay_status, split_status, notify_status, refund_status, recon_status,
       order_time, pay_time, refund_time, recon_time, create_time, update_time) 
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`;

    try {
      await this._execute(insertSql, [
        orderMain.orderNo, orderMain.merchantId, orderMain.merchantName, orderMain.orderAmount,
        orderMain.payAmount, orderMain.platformIncome, orderMain.payFee, orderMain.refundAmount, orderMain.actualAmount,
        orderMain.payStatus, orderMain.splitStatus, orderMain.notifyStatus, orderMain.refundStatus, orderMain.reconStatus,
        orderMain.orderTime, orderMain.payTime, orderMain.refundTime, orderMain.reconTime,
        orderMain.createTime || new Date(), orderMain.updateTime || new Date()
      ]);
      return true;
    } catch (e) {
      // Simple fallback update if duplicate
      if (e.code === 'ER_DUP_ENTRY' || e.code === '23505') { // 23505 is PG unique violation
        return this.updateOrderMain(orderMain);
      }
      console.error('Save Order Main Error', e);
      return false;
    }
  }

  async updateOrderMain(orderMain) {
    const sql = `UPDATE easy_recon_order_main SET 
        pay_status=?, split_status=?, notify_status=?, refund_status=?, recon_status=?,
        refund_amount=?, update_time=?
        WHERE order_no=?`;
    try {
      await this._execute(sql, [
        orderMain.payStatus, orderMain.splitStatus, orderMain.notifyStatus,
        orderMain.refundStatus, orderMain.reconStatus, orderMain.refundAmount,
        new Date(), orderMain.orderNo
      ]);
      return true;
    } catch (e) {
      console.error("Update Order Error", e);
      return false;
    }
  }

  /**
   * 批量保存分账子记录
   */
  async batchSaveOrderSplitSub(splitSubs) {
    if (!splitSubs || splitSubs.length === 0) return true;
    const sql = `INSERT INTO easy_recon_order_split_sub 
      (order_no, sub_order_no, merchant_id, merchant_order_no, split_amount, status, create_time, update_time) 
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)`;

    try {
      for (const sub of splitSubs) {
        await this._execute(sql, [
          sub.orderNo, sub.subOrderNo, sub.merchantId, sub.merchantOrderNo,
          sub.splitAmount, sub.status, new Date(), new Date()
        ]);
      }
      return true;
    } catch (error) {
      console.error('Batch Save Split Error', error);
      return false;
    }
  }

  /**
   * 批量保存退款分账子记录
   */
  async batchSaveOrderRefundSplitSub(refundSubs) {
    if (!refundSubs || refundSubs.length === 0) return true;
    const sql = `INSERT INTO easy_recon_order_refund_split_sub
        (order_no, sub_order_no, merchant_id, refund_split_amount, status, create_time, update_time)
        VALUES (?, ?, ?, ?, ?, ?, ?)`;
    try {
      for (const sub of refundSubs) {
        await this._execute(sql, [
          sub.orderNo, sub.subOrderNo, sub.merchantId, sub.refundSplitAmount,
          sub.status, new Date(), new Date()
        ]);
      }
      return true;
    } catch (e) {
      console.error('Batch Save Refund Split Error', e);
      return false;
    }
  }

  /**
   * 保存通知日志
   */
  async saveNotifyLog(log) {
    const sql = `INSERT INTO easy_recon_notify_log
        (order_no, merchant_id, sub_order_no, merchant_order_no, notify_url, notify_status, notify_result, notify_count, create_time, update_time)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`;
    try {
      await this._execute(sql, [
        log.orderNo, log.merchantId, log.subOrderNo, log.merchantOrderNo,
        log.notifyUrl, log.notifyStatus, log.notifyResult, log.notifyCount || 1,
        new Date(), new Date()
      ]);
      return true;
    } catch (e) {
      console.error('Save Notify Log Error', e);
      return false;
    }
  }

  /**
   * 保存异常记录
   */
  async saveException(exception) {
    const sql = `INSERT INTO easy_recon_exception 
      (order_no, merchant_id, exception_type, exception_msg, exception_step, create_time, update_time) 
      VALUES (?, ?, ?, ?, ?, ?, ?)`;
    try {
      await this._execute(sql, [
        exception.orderNo, exception.merchantId, exception.exceptionType,
        exception.exceptionMsg, exception.exceptionStep, new Date(), new Date()
      ]);
      return true;
    } catch (error) {
      console.error('Save Exception Error', error);
      return false;
    }
  }

  async getOrderMainByOrderNo(orderNo) {
    const sql = 'SELECT * FROM easy_recon_order_main WHERE order_no = ?';
    try {
      const [rows] = await this._execute(sql, [orderNo]);
      return (rows && rows.length > 0) ? this._mapToOrderMain(rows[0]) : null;
    } catch (e) {
      console.error("Get Order Error", e);
      return null;
    }
  }

  async getOrderSplitSubByOrderNo(orderNo) {
    const sql = 'SELECT * FROM easy_recon_order_split_sub WHERE order_no = ?';
    try {
      const [rows] = await this._execute(sql, [orderNo]);
      return rows.map(r => this._mapToOrderSplitSub(r));
    } catch (e) { return []; }
  }

  async getOrderRefundSplitSubByOrderNo(orderNo) {
    const sql = 'SELECT * FROM easy_recon_order_refund_split_sub WHERE order_no = ?';
    try {
      const [rows] = await this._execute(sql, [orderNo]);
      return rows.map(r => this._mapToOrderRefundSplitSub(r));
    } catch (e) { return []; }
  }

  async getNotifyLogsByOrderNo(orderNo) {
    const sql = 'SELECT * FROM easy_recon_notify_log WHERE order_no = ?';
    try {
      const [rows] = await this._execute(sql, [orderNo]);
      return rows.map(r => this._mapToNotifyLog(r));
    } catch (e) { return []; }
  }

  async getReconStatus(orderNo) {
    const sql = 'SELECT recon_status FROM easy_recon_order_main WHERE order_no = ?';
    try {
      const [rows] = await this._execute(sql, [orderNo]);
      return (rows && rows.length > 0) ? rows[0].recon_status : null;
    } catch (e) { return null; }
  }

  async getPendingReconOrders(dateStr, offset, limit) {
    const sql = `SELECT * FROM easy_recon_order_main 
                   WHERE ${this.dialect.getDateFunction()}(order_time) = ? AND recon_status = 0
                   LIMIT ? OFFSET ?`;
    try {
      const [rows] = await this._execute(sql, [dateStr, limit, offset]);
      return rows.map(r => this._mapToOrderMain(r));
    } catch (e) { return []; }
  }

  async updateReconStatus(orderNo, status) {
    const sql = `UPDATE easy_recon_order_main SET recon_status = ?, update_time = NOW() WHERE order_no = ?`;
    try {
      const [res] = await this._execute(sql, [status, orderNo]);
      return res.affectedRows > 0;
    } catch (e) { return false; }
  }

  // --- Mappers ---
  _mapToOrderMain(row) {
    const o = new ReconOrderMain();
    o.id = row.id; o.orderNo = row.order_no; o.merchantId = row.merchant_id;
    o.orderAmount = row.order_amount; o.payAmount = row.pay_amount;
    o.platformIncome = row.platform_income; o.payFee = row.pay_fee;
    o.refundAmount = row.refund_amount; o.actualAmount = row.actual_amount;
    o.payStatus = row.pay_status; o.splitStatus = row.split_status;
    o.notifyStatus = row.notify_status; o.refundStatus = row.refund_status;
    o.reconStatus = row.recon_status;
    o.orderTime = row.order_time;
    return o;
  }

  _mapToOrderSplitSub(row) {
    const s = new ReconOrderSplitSub();
    s.orderNo = row.order_no; s.subOrderNo = row.sub_order_no;
    s.merchantId = row.merchant_id; s.splitAmount = row.split_amount;
    s.merchantOrderNo = row.merchant_order_no;
    return s;
  }

  _mapToOrderRefundSplitSub(row) {
    const s = new ReconOrderRefundSplitSub();
    s.orderNo = row.order_no; s.subOrderNo = row.sub_order_no;
    s.merchantId = row.merchant_id; s.refundSplitAmount = row.refund_split_amount;
    return s;
  }

  _mapToNotifyLog(row) {
    const n = new ReconNotifyLog();
    n.orderNo = row.order_no; n.notifyUrl = row.notify_url;
    n.notifyStatus = row.notify_status; n.notifyResult = row.notify_result;
    return n;
  }
}

module.exports = SQLReconRepository;
