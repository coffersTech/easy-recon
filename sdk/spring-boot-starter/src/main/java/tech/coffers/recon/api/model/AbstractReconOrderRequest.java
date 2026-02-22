package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.entity.ReconOrderMainDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单对账请求抽象基类
 *
 * @author Ryan
 * @since 1.2.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractReconOrderRequest {

    /**
     * 业务订单号 (必填)
     */
    private String orderNo;

    /**
     * 业务子订单信息列表
     */
    private List<ReconSubOrderRequest> subOrders;

    /**
     * 支付分账执行明细 (通常来自支付平台回调)
     */
    private List<ReconOrderSplitRequest> splitDetails;

    /**
     * 分账规则列表 (商户分账比例)
     */
    private List<ReconSplitRuleRequest> splitRules;

    /**
     * 支付状态
     */
    @Builder.Default
    private PayStatusEnum payStatus = PayStatusEnum.SUCCESS;

    /**
     * 分账状态
     */
    @Builder.Default
    private SplitStatusEnum splitStatus = SplitStatusEnum.SUCCESS;

    /**
     * 通知状态
     */
    @Builder.Default
    private NotifyStatusEnum notifyStatus = NotifyStatusEnum.SUCCESS;

    /**
     * 支付手续费率 (基点: 10000 = 100%)
     */
    private Integer payFeeRate;

    /**
     * 获取支付金额 (标准化为元)
     */
    public abstract BigDecimal getPayAmount();

    /**
     * 获取支付金额 (单位：分)
     */
    public abstract Long getPayAmountFen();

    /**
     * 获取平台收入 (标准化为元)
     */
    public abstract BigDecimal getPlatformIncome();

    /**
     * 获取平台收入 (单位：分)
     */
    public abstract Long getPlatformIncomeFen();

    /**
     * 获取支付手续费 (标准化为元)
     */
    public abstract BigDecimal getPayFee();

    /**
     * 获取支付手续费 (单位：分)
     */
    public abstract Long getPayFeeFen();

    /**
     * 将请求中的金额填充到 DO 中 (维持原生单位)
     *
     * @param orderMainDO 目标 DO
     */
    public abstract void populateAmounts(ReconOrderMainDO orderMainDO);

}
