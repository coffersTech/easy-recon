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
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付金额（分）
     */
    private Long payAmountFen;

    /**
     * 平台收入
     */
    private BigDecimal platformIncome;

    /**
     * 平台收入（分）
     */
    private Long platformIncomeFen;

    /**
     * 支付手续费
     */
    private BigDecimal payFee;

    /**
     * 支付手续费（分）
     */
    private Long payFeeFen;

    /**
     * 分账总金额
     */
    private BigDecimal splitTotalAmount;

    /**
     * 分账总金额（分）
     */
    private Long splitTotalAmountFen;

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

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款金额（分）
     */
    private Long refundAmountFen;

    /**
     * 退款状态：0=未退款，1=部分退款，2=全额退款
     */
    private Integer refundStatus;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    // ==================== 自动转换逻辑 ====================

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
        if (payAmount != null) {
            this.payAmountFen = payAmount.multiply(new BigDecimal("100")).longValue();
        }
    }

    public void setPayAmountFen(Long payAmountFen) {
        this.payAmountFen = payAmountFen;
        if (payAmountFen != null) {
            this.payAmount = new BigDecimal(payAmountFen).divide(new BigDecimal("100"));
        }
    }

    public void setPlatformIncome(BigDecimal platformIncome) {
        this.platformIncome = platformIncome;
        if (platformIncome != null) {
            this.platformIncomeFen = platformIncome.multiply(new BigDecimal("100")).longValue();
        }
    }

    public void setPlatformIncomeFen(Long platformIncomeFen) {
        this.platformIncomeFen = platformIncomeFen;
        if (platformIncomeFen != null) {
            this.platformIncome = new BigDecimal(platformIncomeFen).divide(new BigDecimal("100"));
        }
    }

    public void setPayFee(BigDecimal payFee) {
        this.payFee = payFee;
        if (payFee != null) {
            this.payFeeFen = payFee.multiply(new BigDecimal("100")).longValue();
        }
    }

    public void setPayFeeFen(Long payFeeFen) {
        this.payFeeFen = payFeeFen;
        if (payFeeFen != null) {
            this.payFee = new BigDecimal(payFeeFen).divide(new BigDecimal("100"));
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
