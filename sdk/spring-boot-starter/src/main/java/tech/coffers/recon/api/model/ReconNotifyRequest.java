package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.coffers.recon.api.enums.NotifyStatusEnum;

/**
 * 对账通知回调请求模型
 *
 * @author Ryan
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconNotifyRequest {

    /**
     * 业务主订单号
     */
    private String orderNo;

    /**
     * 商户号 (SELF 代表主订单，其他代表子订单商户)
     */
    private String merchantId;

    /**
     * 子订单号 (选填)
     */
    private String subOrderNo;

    /**
     * 商户原始订单号 (选填)
     */
    private String merchantOrderNo;

    /**
     * 接收通知的地址 (选填)
     */
    private String notifyUrl;

    /**
     * 通知后的最终状态
     */
    private NotifyStatusEnum notifyStatus;

    /**
     * 通知返回的原始报文
     */
    private String notifyResult;

}
