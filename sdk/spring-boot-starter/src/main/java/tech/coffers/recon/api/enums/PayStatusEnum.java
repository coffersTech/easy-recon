package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 *
 * @author Ryan
 * @since 1.1.2
 */
@Getter
public enum PayStatusEnum {

    /**
     * 支付中 / 未知 / 初始状态
     */
    PROCESSING(0, "支付中"),

    /**
     * 支付渠道已确认支付成功
     */
    SUCCESS(1, "支付成功"),

    /**
     * 支付渠道明确返回支付失败或已关闭
     */
    FAILURE(2, "支付失败");

    private final Integer code;
    private final String desc;

    PayStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PayStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PROCESSING;
        }
        for (PayStatusEnum status : PayStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PROCESSING;
    }
}
