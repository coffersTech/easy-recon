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
     * 回调通知中 / 待重试 / 初始状态
     */
    PROCESSING(0, "通知中"),

    /**
     * 接收方成功接收通知并返回成功标识 (如 SUCCESS)
     */
    SUCCESS(1, "通知成功"),

    /**
     * 接收方返回失败标识或通知次数用尽仍未成功
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
