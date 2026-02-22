package tech.coffers.recon.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分账规则请求对象
 * <p>
 * 用于指定商户的分账比例规则
 * </p>
 *
 * @author Ryan
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconSplitRuleRequest {

    /**
     * 商户号 (必填)
     */
    private String merchantId;

    /**
     * 分账比例 (基点，例如 7500 代表 75.00%)
     */
    private Integer ratio;

}
