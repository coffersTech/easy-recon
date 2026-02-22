package tech.coffers.recon.api.result;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单退款分账明细结果
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconOrderRefundDetailResult {

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 退款分账金额 (元)
     */
    private BigDecimal refundSplitAmount;

    /**
     * 退款分账金额 (分)
     */
    private Long refundSplitAmountFen;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
