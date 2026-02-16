package tech.coffers.recon.api.enums;

import lombok.Getter;

/**
 * 分账状态枚举
 *
 * @author Ryan
 * @since 1.1.2
 */
@Getter
public enum SplitStatusEnum {

    /**
     * 分账任务处理中 / 初始待同步
     */
    PROCESSING(0, "分账中"),

    /**
     * 分账指令已成功执行且接收方已入账
     */
    SUCCESS(1, "分账成功"),

    /**
     * 分账指令执行失败或已发生冲正
     */
    FAILURE(2, "分账失败");

    private final Integer code;
    private final String desc;

    SplitStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SplitStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PROCESSING;
        }
        for (SplitStatusEnum status : SplitStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PROCESSING;
    }
}
