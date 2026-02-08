/**
 * 对账异常记录实体类
 */
class ReconException {
  constructor() {
    this.id = null;
    this.orderNo = null;
    this.merchantId = null;
    this.exceptionType = null;
    this.exceptionMsg = null;
    this.exceptionStep = null;
    this.createTime = null;
    this.updateTime = null;
  }
}

module.exports = ReconException;
