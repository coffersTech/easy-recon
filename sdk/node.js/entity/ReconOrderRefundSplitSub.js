/**
 * 对账订单退款分账子记录实体类
 */
class ReconOrderRefundSplitSub {
    constructor() {
        this.id = null;
        this.orderNo = null;             // 主订单号
        this.subOrderNo = null;          // 子订单号
        this.merchantId = null;          // 商户ID
        this.merchantName = null;        // 商户名称
        this.refundSplitAmount = null;   // 退款分账金额 (BigDecimal/string)
        this.status = null;              // 状态 (1: SUCCESS, 2: FAIL)
        this.createTime = null;          // 创建时间
        this.updateTime = null;          // 更新时间

        // Additional fields for identification/context if needed
        this.merchantOrderNo = null;     // 商户原始订单号 (optional)
    }
}

module.exports = ReconOrderRefundSplitSub;
