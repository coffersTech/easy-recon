package tech.coffers.recon.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对账通知日志
 * <p>
 * 存储对账结果的通知信息，包括通知地址、通知状态、通知结果等
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconNotifyLogDO {

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
     * 通知地址
     */
    private String notifyUrl;

    /**
     * 通知状态：0=失败，1=成功
     */
    private Integer notifyStatus;

    /**
     * 通知结果
     */
    private String notifyResult;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
