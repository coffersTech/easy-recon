/**
 * 对账通知日志实体类
 */
class ReconNotifyLog {
    constructor() {
        this.id = null;
        this.orderNo = null;           // 业务主订单号
        this.merchantId = null;        // 商户ID
        this.subOrderNo = null;        // 子订单号 (optional)
        this.merchantOrderNo = null;   // 商户原始订单号 (optional)
        this.notifyUrl = null;         // 通知地址
        this.notifyStatus = null;      // 通知状态 (SUCCESS/FAILURE/PROCESSING)
        this.notifyResult = null;      // 通知返回结果 (text/json)
        this.notifyCount = null;       // 通知次数
        this.createTime = null;
        this.updateTime = null;
    }
}

module.exports = ReconNotifyLog;
