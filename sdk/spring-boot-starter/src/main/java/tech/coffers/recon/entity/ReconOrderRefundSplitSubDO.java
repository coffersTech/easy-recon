package tech.coffers.recon.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单退款分账子记录
 * <p>
 * 存储订单的退款分账信息
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconOrderRefundSplitSubDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商户ID (分账接收方/退款方)
     */
    private String merchantId;

    /**
     * 退款分账金额
     */
    private BigDecimal refundSplitAmount;

    /**
     * 退款分账金额（分）
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

    public void setRefundSplitAmount(BigDecimal refundSplitAmount) {
        this.refundSplitAmount = refundSplitAmount;
        if (refundSplitAmount != null) {
            this.refundSplitAmountFen = refundSplitAmount.multiply(new BigDecimal("100")).longValue();
        }
    }

    public void setRefundSplitAmountFen(Long refundSplitAmountFen) {
        this.refundSplitAmountFen = refundSplitAmountFen;
        if (refundSplitAmountFen != null) {
            this.refundSplitAmount = new BigDecimal(refundSplitAmountFen).divide(new BigDecimal("100"));
        }
    }

}
