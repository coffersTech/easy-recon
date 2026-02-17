/**
 * 对账汇总实体类
 */
class ReconSummary {
    constructor() {
        this.id = null;
        this.reconDate = null;         // 对账日期 (YYYY-MM-DD string)
        this.totalOrders = 0;          // 总订单数
        this.totalAmount = 0;          // 总金额
        this.successCount = 0;         // 成功笔数
        this.failCount = 0;            // 失败笔数
        this.exceptionCount = 0;       // 异常笔数
        this.createTime = null;
        this.updateTime = null;
    }
}

module.exports = ReconSummary;
