package tech.coffers.recon.api.result;

import lombok.Data;
import tech.coffers.recon.api.enums.ReconStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单主记录结果
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconOrderMainResult {

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 实付金额 (元)
     */
    private BigDecimal payAmount;

    /**
     * 平台收入金额 (元)
     */
    private BigDecimal platformIncome;

    /**
     * 支付手续费 (元)
     */
    private BigDecimal payFee;

    /**
     * 分账总金额 (元)
     */
    private BigDecimal splitTotalAmount;

    /**
     * 实付金额 (分)
     */
    private Long payAmountFen;

    /**
     * 平台收入金额 (分)
     */
    private Long platformIncomeFen;

    /**
     * 支付手续费 (分)
     */
    private Long payFeeFen;

    /**
     * 分账总金额 (分)
     */
    private Long splitTotalAmountFen;

    /**
     * 对账状态码
     */
    private Integer reconStatus;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 分账状态
     */
    private Integer splitStatus;

    /**
     * 通知状态
     */
    private Integer notifyStatus;

    /**
     * 通知结果
     */
    private String notifyResult;

    /**
     * 退款金额 (元)
     */
    private BigDecimal refundAmount;

    /**
     * 退款金额 (分)
     */
    private Long refundAmountFen;

    /**
     * 退款状态
     */
    private Integer refundStatus;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public ReconStatusEnum getReconStatusEnum() {
        return ReconStatusEnum.fromCode(reconStatus);
    }
}
