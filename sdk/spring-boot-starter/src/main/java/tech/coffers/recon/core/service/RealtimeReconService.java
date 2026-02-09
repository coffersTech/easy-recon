package tech.coffers.recon.core.service;

import lombok.extern.slf4j.Slf4j;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 实时对账服务
 *
 * @author Ryan
 * @since 1.0.0
 */
@Slf4j
public class RealtimeReconService {

    private final ReconRepository reconRepository;
    private final ExceptionRecordService exceptionRecordService;
    private final AlarmService alarmService;
    private final ReconSdkProperties properties; // Kept for reconOrder
    private final ExecutorService executorService; // Added back for async

    public RealtimeReconService(ReconRepository reconRepository, ExceptionRecordService exceptionRecordService,
            AlarmService alarmService, ReconSdkProperties properties, ExecutorService executorService) {
        this.reconRepository = reconRepository;
        this.exceptionRecordService = exceptionRecordService;
        this.alarmService = alarmService;
        this.properties = properties;
        this.executorService = executorService;
    }

    /**
     * 对账订单 (New method for Test satisfaction)
     */
    public ReconResult reconOrder(String orderNo, String merchantId, BigDecimal payAmount, BigDecimal platformIncome,
            BigDecimal payFee, Map<String, BigDecimal> splitDetails, boolean payStatus, boolean notifyStatus) {
        // ... (Same implementation as before)
        try {
            // 1. 校验支付状态
            if (!payStatus) {
                recordException(orderNo, merchantId, "支付状态失败，对账失败", 1);
                return ReconResult.fail(orderNo, "支付状态失败，对账失败");
            }

            // 2. 校验通知状态
            if (!notifyStatus) {
                recordException(orderNo, merchantId, "通知状态失败，对账失败", 3);
                return ReconResult.fail(orderNo, "通知状态失败，对账失败");
            }

            // 3. 校验金额
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitDetails != null) {
                for (BigDecimal amount : splitDetails.values()) {
                    splitTotal = splitTotal.add(amount);
                }
            }
            BigDecimal calcAmount = splitTotal.add(platformIncome).add(payFee);
            if (payAmount.subtract(calcAmount).abs().doubleValue() > properties.getAmountTolerance()) {
                recordException(orderNo, merchantId, "金额校验失败，实付金额与计算金额不一致", 4);
                return ReconResult.fail(orderNo, "金额校验失败，实付金额与计算金额不一致");
            }

            // 4. 保存订单主记录
            ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
            orderMainDO.setOrderNo(orderNo);
            orderMainDO.setMerchantId(merchantId);
            orderMainDO.setPayAmount(payAmount);
            orderMainDO.setPlatformIncome(platformIncome);
            orderMainDO.setPayFee(payFee);
            orderMainDO.setSplitTotalAmount(splitTotal);
            orderMainDO.setReconStatus(1); // 成功
            orderMainDO.setCreateTime(LocalDateTime.now());
            orderMainDO.setUpdateTime(LocalDateTime.now());
            reconRepository.saveOrderMain(orderMainDO);

            // 5. 保存分账子记录
            if (splitDetails != null && !splitDetails.isEmpty()) {
                List<ReconOrderSplitSubDO> splitSubDOs = new ArrayList<>();
                for (Map.Entry<String, BigDecimal> entry : splitDetails.entrySet()) {
                    ReconOrderSplitSubDO subDO = new ReconOrderSplitSubDO();
                    subDO.setOrderNo(orderNo);
                    subDO.setMerchantId(entry.getKey());
                    subDO.setSplitAmount(entry.getValue());
                    subDO.setCreateTime(LocalDateTime.now());
                    subDO.setUpdateTime(LocalDateTime.now());
                    splitSubDOs.add(subDO);
                }
                reconRepository.batchSaveOrderSplitSub(splitSubDOs);
            }

            return ReconResult.success(orderNo);

        } catch (Exception e) {
            log.error("对账处理异常", e);
            recordException(orderNo, merchantId, "对账处理异常: " + e.getMessage(), 5);
            return ReconResult.fail(orderNo, "对账处理异常: " + e.getMessage());
        }
    }

    /**
     * (Restored for EasyReconTemplate compatibility)
     */
    public boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs) {
        try {
            boolean mainSaved = reconRepository.saveOrderMain(orderMainDO);
            if (!mainSaved)
                return false;

            if (splitSubDOs != null && !splitSubDOs.isEmpty()) {
                boolean subSaved = reconRepository.batchSaveOrderSplitSub(splitSubDOs);
                if (!subSaved)
                    return false;
            }
            reconRepository.updateReconStatus(orderMainDO.getOrderNo(), 1);
            return true;
        } catch (Exception e) {
            alarmService.sendReconAlarm(orderMainDO.getOrderNo(), orderMainDO.getMerchantId(),
                    "实时对账失败: " + e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Boolean> doRealtimeReconAsync(ReconOrderMainDO orderMainDO,
            List<ReconOrderSplitSubDO> splitSubDOs) {
        return CompletableFuture.supplyAsync(() -> doRealtimeRecon(orderMainDO, splitSubDOs), executorService);
    }

    private void recordException(String orderNo, String merchantId, String msg, int step) {
        exceptionRecordService.recordReconException(orderNo, merchantId, msg, step);
        alarmService.sendReconAlarm(orderNo, merchantId, msg);
    }
}
