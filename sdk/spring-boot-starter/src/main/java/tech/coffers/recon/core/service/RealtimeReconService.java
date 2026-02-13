package tech.coffers.recon.core.service;

import lombok.extern.slf4j.Slf4j;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderRefundSplitSubDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.springframework.transaction.annotation.Transactional;

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
     * 对账订单
     *
     * @param orderNo        订单号
     * @param merchantId     商户ID
     * @param payAmount      支付金额
     * @param platformIncome 平台收入
     * @param payFee         支付手续费
     * @param splitDetails   分账详情
     * @param payStatus      支付状态
     * @param splitStatus    分账状态
     * @param notifyStatus   通知状态
     * @return 对账结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconOrder(String orderNo, String merchantId, BigDecimal payAmount, BigDecimal platformIncome,
            BigDecimal payFee, Map<String, BigDecimal> splitDetails, boolean payStatus, boolean splitStatus,
            boolean notifyStatus) {
        // ... (Same implementation as before)
        try {
            // 1. 校验支付状态
            if (!payStatus) {
                recordException(orderNo, merchantId, "支付状态失败，对账失败", 1);
                return ReconResult.fail(orderNo, "支付状态失败，对账失败");
            }

            // 2. 校验分账状态
            if (!splitStatus) {
                recordException(orderNo, merchantId, "分账状态失败，对账失败", 2);
                return ReconResult.fail(orderNo, "分账状态失败，对账失败");
            }

            // 3. 校验通知状态
            if (!notifyStatus) {
                recordException(orderNo, merchantId, "通知状态失败，对账失败", 3);
                return ReconResult.fail(orderNo, "通知状态失败，对账失败");
            }

            // 4. 校验金额 (使用 BigDecimal compareTo 避免精度问题)
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitDetails != null) {
                for (BigDecimal amount : splitDetails.values()) {
                    splitTotal = splitTotal.add(amount);
                }
            }
            BigDecimal calcAmount = splitTotal.add(platformIncome).add(payFee);
            // 容差值比较，使用 BigDecimal 的 subtract 和 abs
            if (payAmount.subtract(calcAmount).abs().compareTo(properties.getAmountTolerance()) > 0) {
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
     * (Delegated from EasyReconApi)
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 对账退款
     *
     * @param orderNo      订单号
     * @param refundAmount 退款金额
     * @param refundTime   退款时间
     * @param refundStatus 退款状态
     * @param splitDetails 退款分账详情
     * @return 对账结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconRefund(String orderNo, BigDecimal refundAmount, LocalDateTime refundTime,
            int refundStatus, Map<String, BigDecimal> splitDetails) {
        try {
            // 1. 查询原订单
            ReconOrderMainDO orderMainDO = reconRepository.getOrderMainByOrderNo(orderNo);
            if (orderMainDO == null) {
                return ReconResult.fail(orderNo, "原订单不存在");
            }

            // 2. 校验退款金额 (退款金额 <= 实付金额)
            if (refundAmount.compareTo(orderMainDO.getPayAmount()) > 0) {
                recordException(orderNo, orderMainDO.getMerchantId(), "退款金额大于实付金额", 4);
                return ReconResult.fail(orderNo, "退款金额大于实付金额");
            }

            // 3. 校验退款分账总额
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitDetails != null) {
                for (BigDecimal amount : splitDetails.values()) {
                    splitTotal = splitTotal.add(amount);
                }
            }
            // 退款分账总额 <= 退款金额 (允许有误差吗？通常退款应该精确匹配，或者小于等于)
            // 这里假设退款分账总额必须等于退款金额（排除平台退款部分？）
            // 简单校验：退款分账总额 <= 退款金额
            if (splitTotal.compareTo(refundAmount) > 0) {
                recordException(orderNo, orderMainDO.getMerchantId(), "退款分账总额大于退款金额", 4);
                return ReconResult.fail(orderNo, "退款分账总额大于退款金额");
            }

            // 4. 更新订单主记录的退款状态
            boolean updateSuccess = reconRepository.updateReconRefundStatus(orderNo, refundStatus, refundAmount,
                    refundTime);
            if (!updateSuccess) {
                return ReconResult.fail(orderNo, "更新退款状态失败");
            }

            // 5. 保存退款分账子记录
            if (splitDetails != null && !splitDetails.isEmpty()) {
                List<ReconOrderRefundSplitSubDO> refundSplitSubDOs = new ArrayList<>();
                for (Map.Entry<String, BigDecimal> entry : splitDetails.entrySet()) {
                    ReconOrderRefundSplitSubDO subDO = new ReconOrderRefundSplitSubDO();
                    subDO.setOrderNo(orderNo);
                    subDO.setMerchantId(entry.getKey());
                    subDO.setRefundSplitAmount(entry.getValue());
                    subDO.setCreateTime(LocalDateTime.now());
                    subDO.setUpdateTime(LocalDateTime.now());
                    refundSplitSubDOs.add(subDO);
                }
                reconRepository.batchSaveOrderRefundSplitSub(refundSplitSubDOs);
            }

            return ReconResult.success(orderNo);

        } catch (Exception e) {
            log.error("退款对账处理异常", e);
            recordException(orderNo, "UNKNOWN", "退款对账处理异常: " + e.getMessage(), 5);
            return ReconResult.fail(orderNo, "退款对账处理异常: " + e.getMessage());
        }
    }

    /**
     * 异步对账退款
     *
     * @param orderNo      订单号
     * @param refundAmount 退款金额
     * @param refundTime   退款时间
     * @param refundStatus 退款状态
     * @param splitDetails 退款分账详情
     * @return 异步对账结果
     */
    public CompletableFuture<ReconResult> reconRefundAsync(String orderNo, BigDecimal refundAmount,
            LocalDateTime refundTime,
            int refundStatus, Map<String, BigDecimal> splitDetails) {
        return CompletableFuture.supplyAsync(
                () -> reconRefund(orderNo, refundAmount, refundTime, refundStatus, splitDetails), executorService);
    }

    /**
     * 重试对账
     *
     * @param orderNo 订单号
     * @return 重试结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean retryRecon(String orderNo) {
        try {
            // 1. 查询订单主记录
            ReconOrderMainDO orderMainDO = reconRepository.getOrderMainByOrderNo(orderNo);
            if (orderMainDO == null) {
                recordException(orderNo, "UNKNOWN", "重试对账失败：订单不存在", 0);
                return false;
            }

            // 2. 检查状态：只有 失败(2) 或 异常(0-初始态，视情况而定) 可以重试
            // 这里假设 1=成功, 2=失败, 0=初始
            if (orderMainDO.getReconStatus() == 1) {
                return true; // 已经成功的直接返回成功
            }

            // 3. 获取分账子记录
            List<ReconOrderSplitSubDO> splitSubDOs = reconRepository.getOrderSplitSubByOrderNo(orderNo);

            // 4. 执行校验逻辑 (复用 TimingReconService 的逻辑，或者提取公共逻辑)
            // 这里为了简单，直接内嵌校验逻辑
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitSubDOs != null) {
                for (ReconOrderSplitSubDO sub : splitSubDOs) {
                    splitTotal = splitTotal.add(sub.getSplitAmount());
                }
            }
            BigDecimal calcAmount = splitTotal.add(orderMainDO.getPlatformIncome()).add(orderMainDO.getPayFee());

            if (orderMainDO.getPayAmount().subtract(calcAmount).abs().compareTo(properties.getAmountTolerance()) > 0) {
                recordException(orderNo, orderMainDO.getMerchantId(), "重试对账失败：金额校验不一致", 4);
                reconRepository.updateReconStatus(orderNo, 2); // 保持/更新为失败
                return false;
            }

            // 5. 更新状态为成功
            return reconRepository.updateReconStatus(orderNo, 1);

        } catch (Exception e) {
            log.error("重试对账异常", e);
            recordException(orderNo, "UNKNOWN", "重试对账异常: " + e.getMessage(), 5);
            return false;
        }
    }

    private void recordException(String orderNo, String merchantId, String msg, int step) {
        exceptionRecordService.recordReconException(orderNo, merchantId, msg, step);
        alarmService.sendReconAlarm(orderNo, merchantId, msg);
    }
}
