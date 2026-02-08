package tech.coffers.recon.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对账异常记录
 * <p>
 * 存储对账过程中产生的异常信息，包括异常步骤、异常消息等
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconExceptionDO {

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
     * 异常信息
     */
    private String exceptionMsg;

    /**
     * 异常步骤：1=支付状态，2=分账状态，3=通知状态，4=金额校验，5=其他
     */
    private Integer exceptionStep;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
