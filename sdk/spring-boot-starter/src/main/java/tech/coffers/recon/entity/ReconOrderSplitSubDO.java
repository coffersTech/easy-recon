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
     * 自增主键
     */
    private Long id;

    /**
     * 关联的主业务订单号
     */
    private String orderNo;

    /**
     * 子订单号 (分账单号)，各商户维度下的唯一标识
     */
    private String subOrderNo;

    /**
     * 分账接收方商户ID
     */
    private String merchantId;

    /**
     * 商户原始订单号
     */
    private String merchantOrderNo;

    /**
     * 本次分账的金额 (元)
     */
    private BigDecimal splitAmount;

    /**
     * 分账金额 (分) - 持久化字段
     */
    private Long splitAmountFen;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 该子商户侧的通知回调闭环状态 (0:失败, 1:成功, 2:待处理)
     */
    private Integer notifyStatus;

    /**
     * 对应该子商户分账通知的原始返回信息或错误描述
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
