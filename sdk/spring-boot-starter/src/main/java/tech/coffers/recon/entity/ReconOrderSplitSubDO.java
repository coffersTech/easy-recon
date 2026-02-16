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
     * 子订单号
     */
    private String subOrderNo;

    /**
     * 商户ID (分账接收方)
     */
    private String merchantId;

    /**
     * 分账金额
     */
    private BigDecimal splitAmount;

    /**
     * 分账金额（分）
     */
    private Long splitAmountFen;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 通知状态 (0:失败, 1:成功, 2:待处理)
     */
    private Integer notifyStatus;

    /**
     * 通知返回结果
     */
    private String notifyResult;

    public void setSplitAmount(BigDecimal splitAmount) {
        this.splitAmount = splitAmount;
        if (splitAmount != null) {
            this.splitAmountFen = splitAmount.multiply(new BigDecimal("100")).longValue();
        } else {
            this.splitAmountFen = null;
        }
    }

    public void setSplitAmountFen(Long splitAmountFen) {
        this.splitAmountFen = splitAmountFen;
        if (splitAmountFen != null) {
            this.splitAmount = new BigDecimal(splitAmountFen).divide(new BigDecimal("100"));
        } else {
            this.splitAmount = null;
        }
    }

}
