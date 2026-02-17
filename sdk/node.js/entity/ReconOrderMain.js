/**
 * 对账订单主记录实体类
 */
class ReconOrderMain {
  constructor() {
    this.id = null;
    this.orderNo = null;           // 业务订单号
    this.merchantId = null;        // 商户ID
    this.merchantName = null;      // 商户名称
    this.orderAmount = null;       // 订单金额 (BigDecimal/string)
    this.payAmount = null;         // 支付金额 (BigDecimal/string)
    this.platformIncome = null;    // 平台收入 (BigDecimal/string)
    this.payFee = null;            // 支付手续费 (BigDecimal/string)
    this.refundAmount = null;      // 退款金额 (BigDecimal/string)
    this.actualAmount = null;      // 实付金额 (BigDecimal/string)

    // Status code fields
    this.payStatus = null;         // 支付状态 Enum (1: SUCCESS, 2: FAIL)
    this.splitStatus = null;       // 分账状态 Enum
    this.notifyStatus = null;      // 通知状态 Enum
    this.refundStatus = null;      // 退款状态 Enum (0: NO, 1: PARTIAL, 2: FULL, 3: FAIL)
    this.reconStatus = null;       // 对账状态 Enum (0: PENDING, 1: SUCCESS, 2: FAIL)

    // Time fields
    this.orderTime = null;         // 下单时间 (Date/string)
    this.payTime = null;           // 支付时间
    this.refundTime = null;        // 退款时间
    this.reconTime = null;         // 对账时间
    this.createTime = null;        // 创建时间
    this.updateTime = null;        // 更新时间
  }
}

module.exports = ReconOrderMain;
