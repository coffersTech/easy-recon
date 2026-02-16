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
     * 自增主键
     */
    private Long id;

    /**
     * 关联的主订单号
     */
    private String orderNo;

    /**
     * 关联的子订单号 (分账单号)
     */
    private String subOrderNo;

    /**
     * 被通知方商户ID
     */
    private String merchantId;

    /**
     * 实际触发通知的回调 URL 地址
     */
    private String notifyUrl;

    /**
     * 通知判定状态 (0:失败, 1:成功)
     */
    private Integer notifyStatus;

    /**
     * 接收方返回的原始 Response 内容或 HTTP 错误描述
     */
    private String notifyResult;

    /**
     * 通知记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

}
