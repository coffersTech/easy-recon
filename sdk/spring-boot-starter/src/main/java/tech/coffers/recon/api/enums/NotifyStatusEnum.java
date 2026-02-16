package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 通知回调状态枚举
 *
 * @author Ryan
 * @since 1.1.2
 */
@Getter
public enum NotifyStatusEnum {

    /**
     * 通知中 / 未知
     */
    PROCESSING(0, "通知中"),

    /**
     * 通知成功
     */
    SUCCESS(1, "通知成功"),

    /**
     * 通知失败
     */
    FAILURE(2, "通知失败");

    private final Integer code;
    private final String desc;

    NotifyStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotifyStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PROCESSING;
        }
        for (NotifyStatusEnum status : NotifyStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PROCESSING;
    }
}
