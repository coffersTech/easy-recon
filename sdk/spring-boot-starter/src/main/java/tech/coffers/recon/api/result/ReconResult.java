package tech.coffers.recon.api.result;

import lombok.Data;

/**
 * 对账结果
 *
 * @author Ryan
 * @since 1.0.0
 */
@Data
public class ReconResult {
    private boolean success;
    private int code;
    private String message;
    private String orderNo;

    public static ReconResult success(String orderNo) {
        return success(orderNo, "对账成功");
    }

    public static ReconResult success(String orderNo, String message) {
        ReconResult result = new ReconResult();
        result.setSuccess(true);
        result.setCode(200);
        result.setMessage(message);
        result.setOrderNo(orderNo);
        return result;
    }

    public static ReconResult fail(String orderNo, String message) {
        ReconResult result = new ReconResult();
        result.setSuccess(false);
        result.setCode(500);
        result.setMessage(message);
        result.setOrderNo(orderNo);
        return result;
    }
}
