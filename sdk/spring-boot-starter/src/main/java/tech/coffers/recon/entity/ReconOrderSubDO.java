package tech.coffers.recon.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务子订单记录 (意图层)
 * <p>
 * 存储业务侧的拆分期望，即“原本想分给谁多少钱”。
 * </p>
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconOrderSubDO {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 关联的主业务订单号
     */
    private String orderNo;

    /**
     * 业务定义的子订单号
     */
    private String subOrderNo;

    /**
     * 商户原始订单号
     */
    private String merchantOrderNo;

    /**
     * 预期的收款商户ID
     */
    private String merchantId;

    /**
     * 子订单订单金额 (元)
     */
    private BigDecimal orderAmount;

    /**
     * 子订单订单金额 (分)
     */
    private Long orderAmountFen;

    /**
     * 子订单分账金额 (元)
     */
    private BigDecimal splitAmount;

    /**
     * 子订单分账金额 (分)
     */
    private Long splitAmountFen;

    /**
     * 子订单手续费 (元)
     */
    private BigDecimal fee;

    /**
     * 子订单手续费 (分)
     */
    private Long feeFen;

    /**
     * 分账比例 (基点, e.g. 1000 = 10%)
     */
    private Integer splitRatio;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
        if (orderAmount != null) {
            this.orderAmountFen = orderAmount.multiply(new BigDecimal("100")).longValue();
        } else {
            this.orderAmountFen = null;
        }
    }

    public void setOrderAmountFen(Long orderAmountFen) {
        this.orderAmountFen = orderAmountFen;
        if (orderAmountFen != null) {
            this.orderAmount = BigDecimal.valueOf(orderAmountFen).movePointLeft(2);
        } else {
            this.orderAmount = null;
        }
    }

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

    public void setFee(BigDecimal fee) {
        this.fee = fee;
        if (fee != null) {
            this.feeFen = fee.multiply(new BigDecimal("100")).longValue();
        } else {
            this.feeFen = null;
        }
    }

    public void setFeeFen(Long feeFen) {
        this.feeFen = feeFen;
        if (feeFen != null) {
            this.fee = BigDecimal.valueOf(feeFen).movePointLeft(2);
        } else {
            this.fee = null;
        }
    }
}
