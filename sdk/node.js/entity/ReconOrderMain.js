/**
 * 对账订单主记录实体类
 */
class ReconOrderMain {
  constructor() {
    this.id = null;
    this.orderNo = null;
    this.merchantId = null;
    this.merchantName = null;
    this.orderAmount = null;
    this.actualAmount = null;
    this.reconStatus = null;
    this.orderTime = null;
    this.payTime = null;
    this.reconTime = null;
    this.createTime = null;
    this.updateTime = null;
  }
}

module.exports = ReconOrderMain;
