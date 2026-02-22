package tech.coffers.recon.api.result;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对账通知日志结果
 *
 * @author Ryan
 * @since 1.1.0
 */
@Data
public class ReconNotifyLogResult {

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 子订单号
     */
    private String subOrderNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 通知地址
     */
    private String notifyUrl;

    /**
     * 通知状态 (0:失败, 1:成功)
     */
    private Integer notifyStatus;

    /**
     * 通知结果/原始返回
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
