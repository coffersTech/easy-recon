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
 * 订单对账请求 (分单位实现)
 *
 * @author Ryan
 * @since 1.2.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReconOrderFenRequest extends AbstractReconOrderRequest {

    /**
     * 支付金额 (分)
     */
    private Long payAmountFen;

    /**
     * 平台收入 (分)
     */
    private Long platformIncomeFen;

    /**
     * 支付手续费 (分)
     */
    @Builder.Default
    private Long payFeeFen = 0L;

    @Override
    public BigDecimal getPayAmount() {
        return payAmountFen == null ? null : BigDecimal.valueOf(payAmountFen).movePointLeft(2);
    }

    @Override
    public BigDecimal getPlatformIncome() {
        return platformIncomeFen == null ? null : BigDecimal.valueOf(platformIncomeFen).movePointLeft(2);
    }

    @Override
    public BigDecimal getPayFee() {
        return payFeeFen == null ? BigDecimal.ZERO : BigDecimal.valueOf(payFeeFen).movePointLeft(2);
    }

    @Override
    public Long getPayAmountFen() {
        return payAmountFen;
    }

    @Override
    public Long getPlatformIncomeFen() {
        return platformIncomeFen;
    }

    @Override
    public Long getPayFeeFen() {
        return payFeeFen;
    }

    @Override
    public void populateAmounts(ReconOrderMainDO orderMainDO) {
        if (orderMainDO == null)
            return;
        orderMainDO.setPayAmountFen(payAmountFen);
        if (platformIncomeFen != null) {
            orderMainDO.setPlatformIncomeFen(platformIncomeFen);
        }
        orderMainDO.setPayFeeFen(payFeeFen);
    }
}
