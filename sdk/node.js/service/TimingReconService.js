/**
 * 定时对账服务
 */
class TimingReconService {
  /**
   * 构造函数
   * @param {Object} reconRepository - 对账存储库
   * @param {Object} alarmService - 告警服务
   */
  constructor(reconRepository, alarmService) {
    this.reconRepository = reconRepository;
    this.alarmService = alarmService;
  }

  /**
   * 执行定时对账
   * @param {string} dateStr - 对账日期（YYYY-MM-DD）
   * @returns {Promise<boolean>} 对账结果
   */
  async doTimingRecon(dateStr) {
    try {
      let totalProcessed = 0;
      const limit = 100;
      let offset = 0;

      while (true) {
        // 查询待核账订单
        const pendingOrders = await this.reconRepository.getPendingReconOrders(dateStr, offset, limit);
        if (!pendingOrders || pendingOrders.length === 0) {
          break;
        }

        // 处理每个待核账订单
        for (const order of pendingOrders) {
          try {
            await this.processPendingOrder(order);
            totalProcessed++;
          } catch (error) {
            this.alarmService.sendAlarm(`处理订单 ${order.orderNo} 失败: ${error.message}`);
          }
        }

        offset += limit;
      }

      this.alarmService.sendAlarm(`定时对账完成，共处理 ${totalProcessed} 笔订单`);
      return true;
    } catch (error) {
      this.alarmService.sendAlarm(`定时对账失败: ${error.message}`);
      return false;
    }
  }

  /**
   * 处理待核账订单
   * @param {Object} order - 待核账订单
   * @returns {Promise<void>}
   * @private
   */
  async processPendingOrder(order) {
    // 这里可以添加更复杂的对账逻辑
    // 例如：与第三方支付平台对账、金额校验等

    // 更新对账状态为已对账
    await this.reconRepository.updateReconStatus(order.orderNo, 1); // 1: 已对账
  }
}

module.exports = TimingReconService;
