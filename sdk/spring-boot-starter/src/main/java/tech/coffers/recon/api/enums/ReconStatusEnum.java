package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 对账状态枚举
 *
 * @author Ryan
 * @since 1.1.0
 */
@Getter
public enum ReconStatusEnum {

    /**
     * 待对账
     */
    PENDING(0, "待对账"),

    /**
     * 对账成功
     */
    SUCCESS(1, "成功"),

    /**
     * 对账失败
     */
    FAILURE(2, "失败");

    private final Integer code;
    private final String desc;

    ReconStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 对账状态枚举，若未匹配则返回 null
     */
    public static ReconStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconStatusEnum status : ReconStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
