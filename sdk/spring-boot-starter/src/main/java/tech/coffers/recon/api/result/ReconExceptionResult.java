package tech.coffers.recon.api.result;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对账异常记录结果
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconExceptionResult {

    /**
     * 关联的业务订单号
     */
    private String orderNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 异常消息
     */
    private String exceptionMsg;

    /**
     * 异常步骤 (1:支付, 2:分账, 3:通知, 4:金额, 5:其他)
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
