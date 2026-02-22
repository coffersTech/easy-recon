package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 到账/清算方式枚举
 *
 * @author Ryan
 * @since 1.2.0
 */
@Getter
public enum SettlementTypeEnum {

    /**
     * 平台代收 (资金暂存平台，分账记录仅作为记账事实，无即时入账)
     */
    PLATFORM_COLLECTION(1, "平台代收"),

    /**
     * 全额到商户 (直连模式，资金直送商户余额)
     */
    DIRECT_TO_MERCHANT(2, "全额到商户"),

    /**
     * 空中分账 (通道实时拆分并扣费入账)
     */
    REALTIME_SPLIT(3, "空中分账"),

    /**
     * 错误
     */
    UNKNOWN(0, "未知分账类型");

    private final Integer code;
    private final String desc;

    SettlementTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SettlementTypeEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (SettlementTypeEnum type : SettlementTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
