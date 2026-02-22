package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付分账执行明细 DTO
 * 描述支付平台回调或主动查询到的实际资金分发事实
 *
 * @author Ryan
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconOrderSplitRequest {

    /**
     * 分账接收方商户ID
     */
    private String merchantId;

    /**
     * 本次分账的金额 (元)
     */
    private BigDecimal splitAmount;

    /**
     * 本次分账的金额 (分)
     */
    private Long splitAmountFen;

    /**
     * 实际到账金额 (元)
     */
    private BigDecimal arrivalAmount;

    /**
     * 实际到账金额 (分)
     */
    private Long arrivalAmountFen;

    /**
     * 分账手续费/扣费 (元)
     */
    private BigDecimal splitFee;

    /**
     * 分账手续费/扣费 (分)
     */
    private Long splitFeeFen;

    public BigDecimal getSplitAmount() {
        if (splitAmountFen != null) {
            return BigDecimal.valueOf(splitAmountFen).movePointLeft(2);
        }
        return splitAmount;
    }

    public BigDecimal getArrivalAmount() {
        if (arrivalAmountFen != null) {
            return BigDecimal.valueOf(arrivalAmountFen).movePointLeft(2);
        }
        return arrivalAmount;
    }

    public BigDecimal getSplitFee() {
        if (splitFeeFen != null) {
            return BigDecimal.valueOf(splitFeeFen).movePointLeft(2);
        }
        return splitFee;
    }

    public Long getSplitAmountFen() {
        if (splitAmountFen != null) {
            return splitAmountFen;
        }
        return splitAmount == null ? 0L : splitAmount.multiply(new BigDecimal("100")).longValue();
    }

    public Long getArrivalAmountFen() {
        if (arrivalAmountFen != null) {
            return arrivalAmountFen;
        }
        return arrivalAmount == null ? 0L : arrivalAmount.multiply(new BigDecimal("100")).longValue();
    }

    public Long getSplitFeeFen() {
        if (splitFeeFen != null) {
            return splitFeeFen;
        }
        return splitFee == null ? 0L : splitFee.multiply(new BigDecimal("100")).longValue();
    }

}
