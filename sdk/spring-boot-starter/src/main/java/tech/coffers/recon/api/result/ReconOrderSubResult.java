package tech.coffers.recon.api.result;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务子订单记录结果 (意图层)
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconOrderSubResult {

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 子订单号
     */
    private String subOrderNo;

    /**
     * 商户原始订单号
     */
    private String merchantOrderNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 订单金额 (元)
     */
    private BigDecimal orderAmount;

    /**
     * 分账金额 (元)
     */
    private BigDecimal splitAmount;

    /**
     * 手续费 (元)
     */
    private BigDecimal fee;

    /**
     * 订单金额 (分)
     */
    private Long orderAmountFen;

    /**
     * 分账金额 (分)
     */
    private Long splitAmountFen;

    /**
     * 手续费 (分)
     */
    private Long feeFen;

    /**
     * 分账比例 (基点)
     */
    private Integer splitRatio;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
