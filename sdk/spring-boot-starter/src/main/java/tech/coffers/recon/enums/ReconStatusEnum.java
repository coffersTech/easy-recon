package tech.coffers.recon.enums;

/**
 * 对账状态枚举
 * <p>
 * 定义对账的各种状态
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public enum ReconStatusEnum {

    /**
     * 待对账
     */
    PENDING(0, "待对账"),

    /**
     * 对账成功
     */
    SUCCESS(1, "对账成功"),

    /**
     * 对账失败
     */
    FAILURE(2, "对账失败");

    private final Integer code;
    private final String desc;

    ReconStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据编码获取枚举
     */
    public static ReconStatusEnum getByCode(Integer code) {
        for (ReconStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

}
