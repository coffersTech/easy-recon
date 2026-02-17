/**
 * 实时对账服务
 */
const Decimal = require('decimal.js');

class RealtimeReconService {
  /**
   * 构造函数
   * @param {Object} reconRepository - 对账存储库
   * @param {Object} alarmService - 告警服务
   * @param {Object} config - 配置对象
   */
  constructor(reconRepository, alarmService, config) {
    this.reconRepository = reconRepository;
    this.alarmService = alarmService;
    this.config = config || {};
  }

  /**
   * 执行实时对账 (保存订单及分账)
   * @param {Object} orderMain - 订单主记录
   * @param {Array} splitSubs - 分账子记录列表
   * @returns {Promise<boolean>} 对账结果
   */
  async reconOrder(orderMain, splitSubs) {
    try {
      // 1. 金额校验 (简单示例: orderAmount == payAmount)
      const orderAmount = new Decimal(orderMain.orderAmount || 0);
      const payAmount = new Decimal(orderMain.payAmount || 0);

      // Error tolerance check
      const tolerance = this.config.amountTolerance || new Decimal('0.01');
      if (orderAmount.sub(payAmount).abs().greaterThan(tolerance)) {
        await this.reconRepository.saveException({
          orderNo: orderMain.orderNo,
          merchantId: orderMain.merchantId,
          exceptionType: 1, // Amount Mismatch
          exceptionMsg: `金额不一致: 订单${orderAmount} vs 支付${payAmount}`,
          exceptionStep: 1  // Recon Step
        });
        // Don't fail the save, just mark exception? Or fail? 
        // Usually we save the order as PENDING/FAIL recon status.
        orderMain.reconStatus = 2; // FAIL
      } else {
        orderMain.reconStatus = 1; // SUCCESS
      }

      // 2. 保存订单主记录
      const mainSaved = await this.reconRepository.saveOrderMain(orderMain);
      if (!mainSaved) {
        this.alarmService.sendAlarm(`保存订单主记录失败: ${orderMain.orderNo}`);
        return false;
      }

      // 3. 批量保存分账子记录
      if (splitSubs && splitSubs.length > 0) {
        const subSaved = await this.reconRepository.batchSaveOrderSplitSub(splitSubs);
        if (!subSaved) {
          this.alarmService.sendAlarm(`批量保存分账子记录失败: ${orderMain.orderNo}`);
          return false;
        }
      }

      return true;
    } catch (error) {
      this.alarmService.sendAlarm(`实时对账异常 ${orderMain.orderNo}: ${error.message}`);
      return false;
    }
  }

  /**
   * 退款对账
   * @param {string} orderNo - 订单号
   * @param {string|number} refundAmount - 本次退款金额
   * @param {Array} refundApps - 退款分账子记录
   */
  async reconRefund(orderNo, refundAmount, refundApps) {
    try {
      const orderMain = await this.reconRepository.getOrderMainByOrderNo(orderNo);
      if (!orderMain) {
        this.alarmService.sendAlarm(`退款对账失败: 未找到订单 ${orderNo}`);
        return false;
      }

      // Update refund amount
      const currentRefund = new Decimal(orderMain.refundAmount || 0);
      const newRefund = currentRefund.add(new Decimal(refundAmount));
      orderMain.refundAmount = newRefund.toString();

      const payAmount = new Decimal(orderMain.payAmount || 0);

      if (newRefund.greaterThan(payAmount)) {
        this.alarmService.sendAlarm(`退款金额超出支付金额: ${orderNo}`);
        orderMain.refundStatus = 3; // FAIL
      } else if (newRefund.equals(payAmount)) {
        orderMain.refundStatus = 2; // FULL REFUND
      } else {
        orderMain.refundStatus = 1; // PARTIAL REFUND
      }

      // Update Order Main
      const updated = await this.reconRepository.saveOrderMain(orderMain);
      if (!updated) return false;

      // Save refund splits
      if (refundApps && refundApps.length > 0) {
        // Ensure orderNo is set
        refundApps.forEach(sub => sub.orderNo = orderNo);
        await this.reconRepository.batchSaveOrderRefundSplitSub(refundApps);
      }

      return true;
    } catch (e) {
      this.alarmService.sendAlarm(`退款对账异常 ${orderNo}: ${e.message}`);
      return false;
    }
  }

  /**
   * 通知对账
   */
  async reconNotify(notifyLog) {
    try {
      await this.reconRepository.saveNotifyLog(notifyLog);

      // Update order status based on notify Logic
      // Usually notifies are "Payment Success" notifies.
      // If notifyStatus is SUCCESS (1), we update order notify_status
      if (notifyLog.notifyStatus === 1 && notifyLog.orderNo) {
        const orderMain = await this.reconRepository.getOrderMainByOrderNo(notifyLog.orderNo);
        if (orderMain) {
          orderMain.notifyStatus = 1; // Success
          await this.reconRepository.saveOrderMain(orderMain); // or specific update
        }
      }
      return true;
    } catch (e) {
      this.alarmService.sendAlarm(`通知对账异常 ${notifyLog.orderNo}: ${e.message}`);
      return false;
    }
  }
}

module.exports = RealtimeReconService;
