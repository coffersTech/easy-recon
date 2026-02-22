package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 业务子订单请求 DTO
 * 描述业务上的订单构成
 *
 * @author Ryan
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconSubOrderRequest {

    /**
     * 子订单号
     */
    private String subOrderNo;

    /**
     * 子订单所属商户号
     */
    private String merchantId;

    /**
     * 商户原始订单号
     */
    private String merchantOrderNo;

    /**
     * 子订单订单金额 (元) - 用户支付的部分
     */
    private BigDecimal orderAmount;

    /**
     * 子订单订单金额 (分)
     */
    private Long orderAmountFen;

    /**
     * 子订单分账金额 (元) - 结算给商户的部分
     */
    private BigDecimal splitAmount;

    /**
     * 子订单分账金额 (分)
     */
    private Long splitAmountFen;

    /**
     * 子订单手续费 (元)
     */
    private BigDecimal fee;

    /**
     * 子订单手续费 (分)
     */
    private Long feeFen;

    public BigDecimal getOrderAmount() {
        if (orderAmountFen != null) {
            return BigDecimal.valueOf(orderAmountFen).movePointLeft(2);
        }
        return orderAmount;
    }

    public BigDecimal getSplitAmount() {
        if (splitAmountFen != null) {
            return BigDecimal.valueOf(splitAmountFen).movePointLeft(2);
        }
        return splitAmount;
    }

    public BigDecimal getFee() {
        if (feeFen != null) {
            return BigDecimal.valueOf(feeFen).movePointLeft(2);
        }
        return fee;
    }

    public Long getOrderAmountFen() {
        if (orderAmountFen != null) {
            return orderAmountFen;
        }
        return orderAmount == null ? null : orderAmount.multiply(new BigDecimal("100")).longValue();
    }

    public Long getSplitAmountFen() {
        if (splitAmountFen != null) {
            return splitAmountFen;
        }
        return splitAmount == null ? null : splitAmount.multiply(new BigDecimal("100")).longValue();
    }

    public Long getFeeFen() {
        if (feeFen != null) {
            return feeFen;
        }
        return fee == null ? null : fee.multiply(new BigDecimal("100")).longValue();
    }

}
