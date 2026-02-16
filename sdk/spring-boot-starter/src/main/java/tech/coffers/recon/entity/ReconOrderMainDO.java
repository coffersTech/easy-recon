package tech.coffers.recon.entity;

import lombok.Data;
import tech.coffers.recon.api.enums.ReconStatusEnum;

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
     * 主键ID (数据库自增)
     */
    private Long id;

    /**
     * 业务订单号 (系统内唯一)
     */
    private String orderNo;

    /**
     * 实付金额 (元)
     */
    private BigDecimal payAmount;

    /**
     * 支付金额 (分) - 存储于数据库提高精确度
     */
    private Long payAmountFen;

    /**
     * 平台预留/收入金额 (元)
     */
    private BigDecimal platformIncome;

    /**
     * 平台收入 (分)
     */
    private Long platformIncomeFen;

    /**
     * 支付渠道收取的结算手续费 (元)
     */
    private BigDecimal payFee;

    /**
     * 支付手续费 (分)
     */
    private Long payFeeFen;

    /**
     * 累计的分账总金额 (元) - 逻辑上应等于所有分账项之和
     */
    private BigDecimal splitTotalAmount;

    /**
     * 分账总金额 (分)
     */
    private Long splitTotalAmountFen;

    /**
     * 最终对账核对结果状态
     * 
     * @see tech.coffers.recon.api.enums.ReconStatusEnum
     */
    private Integer reconStatus;

    /**
     * 核心支付侧业务状态 (0:处理中 1:成功 2:失败)
     */
    private Integer payStatus;

    /**
     * 核心分账侧业务状态 (0:处理中 1:成功 2:失败)
     */
    private Integer splitStatus;

    /**
     * 核心通知侧业务状态 (0:处理中 1:成功 2:失败)
     */
    private Integer notifyStatus;

    /**
     * 记录最后一次通知失败的具体原始原因或成功标识
     */
    private String notifyResult;

    /**
     * 辅助方法：快速获取对账状态枚举对象
     *
     * @return ReconStatusEnum
     */
    public ReconStatusEnum getReconStatusEnum() {
        return ReconStatusEnum.fromCode(reconStatus);
    }

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后一次状态更新或数据变动时间
     */
    private LocalDateTime updateTime;

    /**
     * 累计退款成功的金额 (元)
     */
    private BigDecimal refundAmount;

    /**
     * 累计退款金额 (分)
     */
    private Long refundAmountFen;

    /**
     * 退款对账状态 (0:未退款 1:部分退 2:全额退)
     */
    private Integer refundStatus;

    /**
     * 最后一次退款成功的业务处理时间
     */
    private LocalDateTime refundTime;

    // ==================== 自动转换逻辑 ====================

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
        if (payAmount != null) {
            this.payAmountFen = payAmount.multiply(new BigDecimal("100")).longValue();
        } else {
            this.payAmountFen = null;
        }
    }

    public void setPayAmountFen(Long payAmountFen) {
        this.payAmountFen = payAmountFen;
        if (payAmountFen != null) {
            this.payAmount = new BigDecimal(payAmountFen).divide(new BigDecimal("100"));
        } else {
            this.payAmount = null;
        }
    }

    public void setPlatformIncome(BigDecimal platformIncome) {
        this.platformIncome = platformIncome;
        if (platformIncome != null) {
            this.platformIncomeFen = platformIncome.multiply(new BigDecimal("100")).longValue();
        } else {
            this.platformIncomeFen = null;
        }
    }

    public void setPlatformIncomeFen(Long platformIncomeFen) {
        this.platformIncomeFen = platformIncomeFen;
        if (platformIncomeFen != null) {
            this.platformIncome = new BigDecimal(platformIncomeFen).divide(new BigDecimal("100"));
        } else {
            this.platformIncome = null;
        }
    }

    public void setPayFee(BigDecimal payFee) {
        this.payFee = payFee;
        if (payFee != null) {
            this.payFeeFen = payFee.multiply(new BigDecimal("100")).longValue();
        } else {
            this.payFeeFen = null;
        }
    }

    public void setPayFeeFen(Long payFeeFen) {
        this.payFeeFen = payFeeFen;
        if (payFeeFen != null) {
            this.payFee = new BigDecimal(payFeeFen).divide(new BigDecimal("100"));
        } else {
            this.payFee = null;
        }
    }

    public void setSplitTotalAmount(BigDecimal splitTotalAmount) {
        this.splitTotalAmount = splitTotalAmount;
        if (splitTotalAmount != null) {
            this.splitTotalAmountFen = splitTotalAmount.multiply(new BigDecimal("100")).longValue();
        }
    }

    public void setSplitTotalAmountFen(Long splitTotalAmountFen) {
        this.splitTotalAmountFen = splitTotalAmountFen;
        if (splitTotalAmountFen != null) {
            this.splitTotalAmount = new BigDecimal(splitTotalAmountFen).divide(new BigDecimal("100"));
        }
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
        if (refundAmount != null) {
            this.refundAmountFen = refundAmount.multiply(new BigDecimal("100")).longValue();
        }
    }

    public void setRefundAmountFen(Long refundAmountFen) {
        this.refundAmountFen = refundAmountFen;
        if (refundAmountFen != null) {
            this.refundAmount = new BigDecimal(refundAmountFen).divide(new BigDecimal("100"));
        }
    }

}
