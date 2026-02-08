/**
 * 对账订单分账子记录实体类
 */
class ReconOrderSplitSub {
  constructor() {
    this.id = null;
    this.orderNo = null;
    this.subOrderNo = null;
    this.merchantId = null;
    this.splitAmount = null;
    this.status = null;
    this.createTime = null;
    this.updateTime = null;
  }
}

module.exports = ReconOrderSplitSub;
