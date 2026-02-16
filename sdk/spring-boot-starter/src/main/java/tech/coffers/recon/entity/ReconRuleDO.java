package tech.coffers.recon.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对账规则实体
 * <p>
 * 存储对账规则配置信息
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconRuleDO {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 规则显示名称
     */
    private String ruleName;

    /**
     * 规则类型：
     * <ul>
     * <li>1: 金额一致性规则 (如：实付 = 分账 + 手续费)</li>
     * <li>2: 业务状态同步规则</li>
     * <li>3: 周期性补偿规则</li>
     * </ul>
     */
    private Integer ruleType;

    /**
     * 核心规则表达式 (如逻辑表达式或脚本路径)
     */
    private String ruleExpression;

    /**
     * 详细的规则逻辑说明
     */
    private String ruleDesc;

    /**
     * 规则激活状态 (1:启用, 0:禁用)
     */
    private Integer status;

    /**
     * 规则创建时间
     */
    private LocalDateTime createTime;

    /**
     * 规则最后修改时间
     */
    private LocalDateTime updateTime;

}
