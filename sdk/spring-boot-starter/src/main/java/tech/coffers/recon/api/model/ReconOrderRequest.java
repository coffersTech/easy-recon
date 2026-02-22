package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tech.coffers.recon.entity.ReconOrderMainDO;

import java.math.BigDecimal;

/**
 * 订单对账请求 (元单位实现)
 *
 * @author Ryan
 * @since 1.2.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReconOrderRequest extends AbstractReconOrderRequest {

    /**
     * 支付金额 (元)
     */
    private BigDecimal payAmount;

    /**
     * 平台收入 (元)
     */
    private BigDecimal platformIncome;

    /**
     * 支付手续费 (元)
     */
    @Builder.Default
    private BigDecimal payFee = BigDecimal.ZERO;

    @Override
    public BigDecimal getPayAmount() {
        return payAmount;
    }

    @Override
    public BigDecimal getPlatformIncome() {
        return platformIncome;
    }

    @Override
    public BigDecimal getPayFee() {
        return payFee;
    }

    @Override
    public Long getPayAmountFen() {
        return payAmount == null ? null : payAmount.multiply(new BigDecimal("100")).longValue();
    }

    @Override
    public Long getPlatformIncomeFen() {
        return platformIncome == null ? null : platformIncome.multiply(new BigDecimal("100")).longValue();
    }

    @Override
    public Long getPayFeeFen() {
        return payFee == null ? 0L : payFee.multiply(new BigDecimal("100")).longValue();
    }

    @Override
    public void populateAmounts(ReconOrderMainDO orderMainDO) {
        if (orderMainDO == null)
            return;
        orderMainDO.setPayAmount(payAmount);
        if (platformIncome != null) {
            orderMainDO.setPlatformIncome(platformIncome);
        }
        orderMainDO.setPayFee(payFee);
    }
}
