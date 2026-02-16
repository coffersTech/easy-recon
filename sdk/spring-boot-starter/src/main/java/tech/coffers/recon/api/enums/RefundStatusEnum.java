package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 *
 * @author Ryan
 * @since 1.1.2
 */
@Getter
public enum RefundStatusEnum {

    /**
     * 退款中 / 未知
     */
    PROCESSING(0, "退款中"),

    /**
     * 退款成功
     */
    SUCCESS(1, "退款成功"),

    /**
     * 退款失败
     */
    FAILURE(2, "退款失败");

    private final Integer code;
    private final String desc;

    RefundStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PROCESSING;
        }
        for (RefundStatusEnum status : RefundStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PROCESSING;
    }
}
