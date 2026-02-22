package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tech.coffers.recon.entity.ReconOrderMainDO;

import java.math.BigDecimal;

/**
 * 退款对账请求 (元单位实现)
 *
 * @author Ryan
 * @since 1.2.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReconRefundRequest extends AbstractReconRefundRequest {

    /**
     * 退款总金额 (元)
     */
    private BigDecimal refundAmount;

    @Override
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    @Override
    public void populateAmounts(ReconOrderMainDO orderMainDO) {
        if (orderMainDO == null)
            return;
        orderMainDO.setRefundAmount(refundAmount);
    }
}
