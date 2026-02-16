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
     * 对账统计的目标日期
     */
    private LocalDate summaryDate;

    /**
     * 当日总计待处理或已处理的业务订单总数
     */
    private Integer totalOrders;

    /**
     * 对账判定为“成功”的订单总数
     */
    private Integer successCount;

    /**
     * 对账判定为“失败”或已产生异常记录的订单总数
     */
    private Integer failCount;

    /**
     * 尚未开始核账或处于初始同步状态的订单数
     */
    private Integer initCount;

    /**
     * 当日交易成功的总金额累计 (元)
     */
    private BigDecimal totalAmount;
}
