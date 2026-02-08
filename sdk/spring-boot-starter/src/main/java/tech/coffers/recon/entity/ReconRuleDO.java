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
     * 主键ID
     */
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型：1=金额规则，2=状态规则，3=其他规则
     */
    private Integer ruleType;

    /**
     * 规则表达式
     */
    private String ruleExpression;

    /**
     * 规则描述
     */
    private String ruleDesc;

    /**
     * 是否启用：1=启用，0=禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
