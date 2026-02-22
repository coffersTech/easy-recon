package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.entity.ReconOrderMainDO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款对账请求抽象基类
 *
 * @author Ryan
 * @since 1.2.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractReconRefundRequest {

    /**
     * 业务主订单号
     */
    private String orderNo;

    /**
     * 商户号 (选填，用于识别场景)
     */
    private String merchantId;

    /**
     * 子订单号 (选填，用于识别场景)
     */
    private String subOrderNo;

    /**
     * 商户原始订单号 (选填，用于识别场景)
     */
    private String merchantOrderNo;

    /**
     * 退款时间
     */
    @Builder.Default
    private LocalDateTime refundTime = LocalDateTime.now();

    /**
     * 退款状态
     */
    @Builder.Default
    private RefundStatusEnum refundStatus = RefundStatusEnum.SUCCESS;

    /**
     * 退款分账详情
     */
    private List<ReconRefundSplitRequest> splitDetails;

    /**
     * 获取退款金额 (标准化为元)
     */
    public abstract BigDecimal getRefundAmount();

    /**
     * 将请求中的金额填充到 DO 中 (维持原生单位)
     *
     * @param orderMainDO 目标 DO
     */
    public abstract void populateAmounts(ReconOrderMainDO orderMainDO);
}
