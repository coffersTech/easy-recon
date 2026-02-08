/**
 * 实时对账服务
 */
class RealtimeReconService {
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
   * 执行实时对账
   * @param {Object} orderMain - 订单主记录
   * @param {Array} splitSubs - 分账子记录列表
   * @returns {Promise<boolean>} 对账结果
   */
  async doRealtimeRecon(orderMain, splitSubs) {
    try {
      // 1. 保存订单主记录
      const mainSaved = await this.reconRepository.saveOrderMain(orderMain);
      if (!mainSaved) {
        this.alarmService.sendAlarm('保存订单主记录失败');
        return false;
      }

      // 2. 批量保存分账子记录
      if (splitSubs && splitSubs.length > 0) {
        const subSaved = await this.reconRepository.batchSaveOrderSplitSub(splitSubs);
        if (!subSaved) {
          this.alarmService.sendAlarm('批量保存分账子记录失败');
          return false;
        }
      }

      // 3. 更新对账状态为已对账
      const statusUpdated = await this.reconRepository.updateReconStatus(orderMain.orderNo, 1); // 1: 已对账
      if (!statusUpdated) {
        this.alarmService.sendAlarm('更新对账状态失败');
        return false;
      }

      return true;
    } catch (error) {
      this.alarmService.sendAlarm(`实时对账失败: ${error.message}`);
      return false;
    }
  }
}

module.exports = RealtimeReconService;
