package tech.coffers.recon.enums;

/**
 * 异常步骤枚举
 * <p>
 * 定义对账过程中异常发生的步骤
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public enum ExceptionStepEnum {

    /**
     * 支付状态异常
     */
    PAY_STATUS(1, "支付状态"),

    /**
     * 分账状态异常
     */
    SPLIT_STATUS(2, "分账状态"),

    /**
     * 通知状态异常
     */
    NOTIFY_STATUS(3, "通知状态"),

    /**
     * 金额校验异常
     */
    AMOUNT_CHECK(4, "金额校验"),

    /**
     * 其他异常
     */
    OTHER(5, "其他");

    private final Integer code;
    private final String desc;

    ExceptionStepEnum(Integer code, String desc) {
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
    public static ExceptionStepEnum getByCode(Integer code) {
        for (ExceptionStepEnum step : values()) {
            if (step.code.equals(code)) {
                return step;
            }
        }
        return null;
    }

}
