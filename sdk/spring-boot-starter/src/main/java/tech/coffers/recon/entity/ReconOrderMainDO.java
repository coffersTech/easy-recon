package tech.coffers.recon.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单主记录
 * <p>
 * 存储订单的对账信息，包括实付金额、平台收入、支付手续费、分账总金额等
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconOrderMainDO {

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
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 平台收入
     */
    private BigDecimal platformIncome;

    /**
     * 支付手续费
     */
    private BigDecimal payFee;

    /**
     * 分账总金额
     */
    private BigDecimal splitTotalAmount;

    /**
     * 对账状态：0=待对账，1=成功，2=失败
     */
    private Integer reconStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
