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
     * 自增主键
     */
    private Long id;

    /**
     * 关联的业务订单号
     */
    private String orderNo;

    /**
     * 发生异常的相关商户ID (针对平台侧异常可记录为 SELF 或特定 ID)
     */
    private String merchantId;

    /**
     * 详细的异常错误描述信息
     */
    private String exceptionMsg;

    /**
     * 异常发生的核账步骤阶段
     * <ul>
     * <li>1: 支付状态核对阶段</li>
     * <li>2: 分账状态核对阶段</li>
     * <li>3: 通知回调核对阶段</li>
     * <li>4: 金额一致性校验阶段</li>
     * <li>5: 其他/系统异常</li>
     * </ul>
     */
    private Integer exceptionStep;

    /**
     * 异常首次记录时间
     */
    private LocalDateTime createTime;

    /**
     * 最后一次更新时间
     */
    private LocalDateTime updateTime;

}
