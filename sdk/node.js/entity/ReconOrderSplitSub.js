/**
 * 对账订单分账子记录实体类
 */
class ReconOrderSplitSub {
  constructor() {
    this.id = null;
    this.orderNo = null;           // 业务主订单号
    this.subOrderNo = null;        // 子订单号
    this.merchantId = null;        // 商户ID
    this.merchantName = null;      // 商户名称 (optional)
    this.merchantOrderNo = null;   // 商户原始订单号 (optional)
    this.splitAmount = null;       // 分账金额
    this.status = null;            // 状态 (1: SUCCESS, 2: FAIL)
    this.createTime = null;
    this.updateTime = null;
  }
}

module.exports = ReconOrderSplitSub;
