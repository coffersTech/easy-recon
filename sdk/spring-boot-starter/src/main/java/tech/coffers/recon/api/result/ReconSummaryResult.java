package tech.coffers.recon.api.result;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 对账汇总统计结果
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconSummaryResult {
    /**
     * 统计日期
     */
    private LocalDate summaryDate;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 成功订单数
     */
    private Integer successCount;

    /**
     * 失败订单数
     */
    private Integer failCount;

    /**
     * 初始订单数
     */
    private Integer initCount;

    /**
     * 当日成交总额 (元)
     */
    private BigDecimal totalAmount;

    /**
     * 当日成交总额 (分)
     */
    private Long totalAmountFen;
}
