package tech.coffers.recon.api.result;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账订单分账事实明细结果
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconOrderSplitDetailResult {

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 分账金额 (元)
     */
    private BigDecimal splitAmount;

    /**
     * 通知状态
     */
    private Integer notifyStatus;

    /**
     * 清算类型 (1:平台代收 2:直通商户 3:空中分账)
     */
    private Integer settlementType;

    /**
     * 实际到账金额 (元)
     */
    private BigDecimal arrivalAmount;

    /**
     * 分账手续费 (元)
     */
    private BigDecimal splitFee;

    /**
     * 分账金额 (分)
     */
    private Long splitAmountFen;

    /**
     * 实际到账金额 (分)
     */
    private Long arrivalAmountFen;

    /**
     * 分账手续费 (分)
     */
    private Long splitFeeFen;

    /**
     * 通知结果/错误信息
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
