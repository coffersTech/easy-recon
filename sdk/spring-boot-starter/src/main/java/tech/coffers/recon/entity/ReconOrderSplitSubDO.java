package tech.coffers.recon.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单分账子记录
 * <p>
 * 存储订单的分账详情，包括每个商户的分账金额
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconOrderSplitSubDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 分账金额
     */
    private BigDecimal splitAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
