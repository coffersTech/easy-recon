package tech.coffers.recon.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.coffers.recon.api.enums.SettlementTypeEnum;

import java.time.LocalDateTime;

/**
 * 订单商户维度结算统计 DO
 *
 * @author Ryan
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconOrderMerchantSettlementDO {

    private Long id;

    /**
     * 关联主订单号
     */
    private String orderNo;

    /**
     * 商户 ID
     */
    private String merchantId;

    /**
     * 推算的到账方式
     */
    private SettlementTypeEnum settlementType;

    /**
     * 订单总金额 (单位：分) - 意图层
     */
    private Long orderAmountFen;

    /**
     * 分账总金额 (单位：分) - 事实层
     */
    private Long splitAmountFen;

    /**
     * 分账比例 (基点)
     */
    private Integer splitRatio;

    /**
     * 分账手续费 (单位：分) - 事实层
     */
    private Long splitFeeFen;

    /**
     * 实际到账金额 (单位：分) - 推算 (splitAmount - splitFee)
     */
    private Long arrivalAmountFen;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
