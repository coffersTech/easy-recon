package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 业务处理状态枚举
 *
 * @author Ryan
 * @since 1.1.1
 */
@Getter
public enum BusinessStatusEnum {

    /**
     * 处理中 / 未知
     */
    PROCESSING(0, "处理中"),

    /**
     * 成功
     */
    SUCCESS(1, "成功"),

    /**
     * 失败
     */
    FAILURE(2, "失败");

    private final Integer code;
    private final String desc;

    BusinessStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BusinessStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PROCESSING;
        }
        for (BusinessStatusEnum status : BusinessStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PROCESSING;
    }
}
