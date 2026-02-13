package tech.coffers.recon.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 对账汇总统计实体
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconSummaryDO {
    /**
     * 统计日期
     */
    private LocalDate summaryDate;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 对账成功数
     */
    private Integer successCount;

    /**
     * 对账失败数
     */
    private Integer failCount;

    /**
     * 待对账/初始状态数
     */
    private Integer initCount;

    /**
     * 总交易金额
     */
    private BigDecimal totalAmount;
}
