package com.coffers.easy.recon.demo.model;

import java.math.BigDecimal;

/**
 * 演示数据生成请求
 */
public class DemoGenRequest {

    /**
     * 场景类型
     * inference - 基础推断
     * platform - 平台代收
     * multi - 多商户分账
     * refund - 退款流程
     * async - 异步流程
     * exception - 异常处理
     */
    private String type;

    /**
     * 订单支付金额 (元)
     */
    private BigDecimal payAmount;

    /**
     * 商户号 (多个商户号以逗号分隔)
     */
    private String merchantIds;

    /**
     * 订单号前缀
     */
    private String orderPrefix;

    /**
     * 分账比例 (基点)
     */
    private Integer splitRatio;

    /**
     * 分账方数量 (用于控制子订单和分账规则数量)
     */
    private Integer splitCount;

    public Integer getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(Integer splitCount) {
        this.splitCount = splitCount;
    }

    public Integer getSplitRatio() {
        return splitRatio;
    }

    public void setSplitRatio(Integer splitRatio) {
        this.splitRatio = splitRatio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public String getMerchantIds() {
        return merchantIds;
    }

    public void setMerchantIds(String merchantIds) {
        this.merchantIds = merchantIds;
    }

    public String getOrderPrefix() {
        return orderPrefix;
    }

    public void setOrderPrefix(String orderPrefix) {
        this.orderPrefix = orderPrefix;
    }
}
