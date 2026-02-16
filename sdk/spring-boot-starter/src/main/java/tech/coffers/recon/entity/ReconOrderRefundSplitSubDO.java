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
     * 自增主键
     */
    private Long id;

    /**
     * 关联的业务订单号
     */
    private String orderNo;

    /**
     * 被退款扣减的商户ID
     */
    private String merchantId;

    /**
     * 本次退至商户侧的分账金额 (元)
     */
    private BigDecimal refundSplitAmount;

    /**
     * 退款分账金额 (分)
     */
    private Long refundSplitAmountFen;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    public void setRefundSplitAmount(BigDecimal refundSplitAmount) {
        this.refundSplitAmount = refundSplitAmount;
        if (refundSplitAmount != null) {
            this.refundSplitAmountFen = refundSplitAmount.multiply(new BigDecimal("100")).longValue();
        } else {
            this.refundSplitAmountFen = null;
        }
    }

    public void setRefundSplitAmountFen(Long refundSplitAmountFen) {
        this.refundSplitAmountFen = refundSplitAmountFen;
        if (refundSplitAmountFen != null) {
            this.refundSplitAmount = new BigDecimal(refundSplitAmountFen).divide(new BigDecimal("100"));
        } else {
            this.refundSplitAmount = null;
        }
    }

}
