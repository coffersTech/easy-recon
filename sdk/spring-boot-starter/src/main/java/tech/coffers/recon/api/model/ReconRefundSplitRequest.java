package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退款分账明细请求 DTO
 *
 * @author Ryan
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconRefundSplitRequest {

    /**
     * 被退款扣减的商户ID
     */
    private String merchantId;

    /**
     * 本次退至商户侧的分账金额 (元)
     */
    private BigDecimal refundSplitAmount;

    /**
     * 本次退至商户侧的分账金额 (分)
     */
    private Long refundSplitAmountFen;

}
