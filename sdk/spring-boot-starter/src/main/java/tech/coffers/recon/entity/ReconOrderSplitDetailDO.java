package tech.coffers.recon.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单分账事实明细 (事实层)
 * <p>
 * 存储从支付平台返回的实际分账详情。
 * </p>
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconOrderSplitDetailDO {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 关联的主业务订单号
     */
    private String orderNo;

    /**
     * 分账接收方商户ID
     */
    private String merchantId;

    /**
     * 本次分账的金额 (元)
     */
    private BigDecimal splitAmount;

    /**
     * 分账金额 (分)
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
     * 核心通知侧业务状态 (0:处理中 1:成功 2:失败)
     */
    private Integer notifyStatus;

    /**
     * 到账方式 (1:平台代收 2:全额到商户 3:空中分账)
     */
    private Integer settlementType;

    /**
     * 实际到账金额 (元)
     */
    private BigDecimal arrivalAmount;

    /**
     * 实际到账金额 (分)
     */
    private Long arrivalAmountFen;

    /**
     * 分账手续费/扣费 (元)
     */
    private BigDecimal splitFee;

    /**
     * 分账手续费 (分)
     */
    private Long splitFeeFen;

    /**
     * 原始返回信息或错误描述
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
            this.splitAmount = BigDecimal.valueOf(splitAmountFen).movePointLeft(2);
        } else {
            this.splitAmount = null;
        }
    }

    public void setArrivalAmount(BigDecimal arrivalAmount) {
        this.arrivalAmount = arrivalAmount;
        if (arrivalAmount != null) {
            this.arrivalAmountFen = arrivalAmount.multiply(new BigDecimal("100")).longValue();
        } else {
            this.arrivalAmountFen = null;
        }
    }

    public void setArrivalAmountFen(Long arrivalAmountFen) {
        this.arrivalAmountFen = arrivalAmountFen;
        if (arrivalAmountFen != null) {
            this.arrivalAmount = BigDecimal.valueOf(arrivalAmountFen).movePointLeft(2);
        } else {
            this.arrivalAmount = null;
        }
    }

    public void setSplitFee(BigDecimal splitFee) {
        this.splitFee = splitFee;
        if (splitFee != null) {
            this.splitFeeFen = splitFee.multiply(new BigDecimal("100")).longValue();
        } else {
            this.splitFeeFen = null;
        }
    }

    public void setSplitFeeFen(Long splitFeeFen) {
        this.splitFeeFen = splitFeeFen;
        if (splitFeeFen != null) {
            this.splitFee = BigDecimal.valueOf(splitFeeFen).movePointLeft(2);
        } else {
            this.splitFee = null;
        }
    }
}
