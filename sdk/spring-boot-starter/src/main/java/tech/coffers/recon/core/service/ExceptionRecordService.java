package tech.coffers.recon.core.service;

import tech.coffers.recon.entity.ReconExceptionDO;
import tech.coffers.recon.repository.ReconRepository;

import java.time.LocalDateTime;

/**
 * 异常记录服务
 *
 * @author Ryan
 * @since 1.0.0
 */
public class ExceptionRecordService {

    private final ReconRepository reconRepository;

    public ExceptionRecordService(ReconRepository reconRepository) {
        this.reconRepository = reconRepository;
    }

    /**
     * 记录对账异常
     *
     * @param orderNo       订单号
     * @param merchantId    商户ID
     * @param exceptionMsg  异常信息
     * @param exceptionStep 异常步骤
     */
    public void recordReconException(String orderNo, String merchantId, String exceptionMsg, int exceptionStep) {
        ReconExceptionDO exceptionDO = new ReconExceptionDO();
        exceptionDO.setOrderNo(orderNo);
        exceptionDO.setMerchantId(merchantId);
        exceptionDO.setExceptionMsg(exceptionMsg);
        exceptionDO.setExceptionStep(exceptionStep);
        exceptionDO.setCreateTime(LocalDateTime.now());
        exceptionDO.setUpdateTime(LocalDateTime.now());
        reconRepository.saveException(exceptionDO);
    }
}
