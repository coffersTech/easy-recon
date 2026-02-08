/**
 * 对账SDK核心模板类
 */
class EasyReconTemplate {
  /**
   * 构造函数
   * @param {Object} realtimeReconService - 实时对账服务
   * @param {Object} timingReconService - 定时对账服务
   */
  constructor(realtimeReconService, timingReconService) {
    this.realtimeReconService = realtimeReconService;
    this.timingReconService = timingReconService;
  }

  /**
   * 执行实时对账
   * @param {Object} orderMain - 订单主记录
   * @param {Array} splitSubs - 分账子记录列表
   * @returns {Promise<boolean>} 对账结果
   */
  async doRealtimeRecon(orderMain, splitSubs) {
    return this.realtimeReconService.doRealtimeRecon(orderMain, splitSubs);
  }

  /**
   * 执行定时对账
   * @param {string} dateStr - 对账日期（YYYY-MM-DD）
   * @returns {Promise<boolean>} 对账结果
   */
  async doTimingRecon(dateStr) {
    return this.timingReconService.doTimingRecon(dateStr);
  }
}

module.exports = EasyReconTemplate;
