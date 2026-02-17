/**
 * 定时对账服务
 */
class TimingReconService {
  /**
   * 构造函数
   * @param {Object} reconRepository - 对账存储库
   * @param {Object} alarmService - 告警服务
   * @param {Object} config - 配置
   */
  constructor(reconRepository, alarmService, config) {
    this.reconRepository = reconRepository;
    this.alarmService = alarmService;
    this.config = config || {};
  }

  /**
   * 执行定时对账
   * @param {string} dateStr - 对账日期（YYYY-MM-DD）
   * @returns {Promise<boolean>} 对账结果
   */
  async doTimingRecon(dateStr) {
    try {
      let totalProcessed = 0;
      const batchSize = this.config.batchSize || 100;
      let offset = 0;

      this.alarmService.sendAlarm(`开始定时对账任务: ${dateStr}`);

      while (true) {
        // 查询待核账订单
        const pendingOrders = await this.reconRepository.getPendingReconOrders(dateStr, offset, batchSize);
        if (!pendingOrders || pendingOrders.length === 0) {
          break;
        }

        // 处理每个待核账订单
        for (const order of pendingOrders) {
          try {
            await this.processPendingOrder(order);
            totalProcessed++;
          } catch (error) {
            this.alarmService.sendAlarm(`定时处理订单 ${order.orderNo} 失败: ${error.message}`);
          }
        }

        offset += batchSize;
        // Safety break to prevent infinite loop if something is wrong with pagination or data
        if (totalProcessed > 100000) {
          this.alarmService.sendAlarm(`单次定时对账超过10万条，强制停止`);
          break;
        }
      }

      this.alarmService.sendAlarm(`定时对账完成，共处理 ${totalProcessed} 笔订单`);

      // Generate Daily Summary
      await this.generateDailySummary(dateStr);

      return true;
    } catch (error) {
      this.alarmService.sendAlarm(`定时对账失败: ${error.message}`);
      return false;
    }
  }

  /**
   * 处理待核账订单
   * @param {Object} order - 待核账订单
   * @private
   */
  async processPendingOrder(order) {
    // 实际业务中，这里应该调用第三方渠道查询接口
    // 为了简化SDK演示，这里假设只要金额正确就视为对账成功

    // Simulate check
    if (order.orderAmount == order.payAmount) {
      await this.reconRepository.updateReconStatus(order.orderNo, 1); // 1: SUCCESS
    } else {
      // Mark as FAIL or Exception
      await this.reconRepository.updateReconStatus(order.orderNo, 2); // 2: FAIL
    }
  }

  /**
   * 生成每日汇总
   * @param {string} dateStr
   */
  async generateDailySummary(dateStr) {
    // This would require a repository method to aggregate stats
    // For now, leaving as placeholder or implementing basic log
    // console.log(`Summary for ${dateStr} generated.`);
  }
}

module.exports = TimingReconService;
