package tech.coffers.recon.core.service;

import lombok.extern.slf4j.Slf4j;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderRefundSplitSubDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.entity.ReconNotifyLogDO;
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
    private final ReconSdkProperties properties;
    private final ExecutorService executorService;

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
    public ReconResult reconOrder(String orderNo, BigDecimal payAmount, BigDecimal platformIncome,
            BigDecimal payFee, Map<String, BigDecimal> splitDetails, PayStatusEnum payStatus,
            SplitStatusEnum splitStatus, NotifyStatusEnum notifyStatus) {
        try {
            PayStatusEnum payEnum = payStatus != null ? payStatus : PayStatusEnum.PROCESSING;
            SplitStatusEnum splitEnum = splitStatus != null ? splitStatus : SplitStatusEnum.PROCESSING;
            NotifyStatusEnum notifyEnum = notifyStatus != null ? notifyStatus : NotifyStatusEnum.PROCESSING;

            // 1. 校验是否涉及失败 (FAILURE 直接记录异常并返回失败)
            if (payEnum == PayStatusEnum.FAILURE) {
                recordException(orderNo, "SELF", "支付状态失败，对账失败", 1);
                alarmService.sendReconAlarm(orderNo, "SELF", "支付状态失败，对账失败");
                return ReconResult.fail(orderNo, "支付状态失败，对账失败");
            }
            if (splitEnum == SplitStatusEnum.FAILURE) {
                recordException(orderNo, "SELF", "分账状态失败，对账失败", 2);
                alarmService.sendReconAlarm(orderNo, "SELF", "分账状态失败，对账失败");
                return ReconResult.fail(orderNo, "分账状态失败，对账失败");
            }
            if (notifyEnum == NotifyStatusEnum.FAILURE) {
                recordException(orderNo, "SELF", "通知状态失败，对账失败", 3);
                alarmService.sendReconAlarm(orderNo, "SELF", "通知状态失败，对账失败");
                return ReconResult.fail(orderNo, "通知状态失败，对账失败");
            }

            // 确定整体对账状态
            ReconStatusEnum reconStatus = ReconStatusEnum.SUCCESS;
            if (payEnum == PayStatusEnum.PROCESSING || splitEnum == SplitStatusEnum.PROCESSING
                    || notifyEnum == NotifyStatusEnum.PROCESSING) {
                reconStatus = ReconStatusEnum.PENDING;
            }
            // 3. 计算金额 (仅在全部成功时才需要强一致性校验)
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitDetails != null) {
                for (BigDecimal amount : splitDetails.values()) {
                    splitTotal = splitTotal.add(amount);
                }
            }

            if (reconStatus == ReconStatusEnum.SUCCESS) { // Only check amount if all statuses are successful
                BigDecimal calcAmount = splitTotal.add(platformIncome).add(payFee);
                if (payAmount.subtract(calcAmount).abs().compareTo(properties.getAmountTolerance()) > 0) {
                    recordException(orderNo, "SELF", "金额校验失败，实付金额与计算金额不一致", 4);
                    return ReconResult.fail(orderNo, "金额校验失败，实付金额与计算金额不一致");
                }
            }

            // 4. 保存订单主记录
            ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
            orderMainDO.setOrderNo(orderNo);
            orderMainDO.setPayAmount(payAmount);
            orderMainDO.setPlatformIncome(platformIncome);
            orderMainDO.setPayFee(payFee);
            orderMainDO.setSplitTotalAmount(splitTotal);
            orderMainDO.setPayStatus(payEnum.getCode());
            orderMainDO.setSplitStatus(splitEnum.getCode());
            orderMainDO.setNotifyStatus(notifyEnum.getCode());
            orderMainDO.setReconStatus(reconStatus.getCode());
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
            recordException(orderNo, "SELF", "对账处理异常: " + e.getMessage(), 5);
            return ReconResult.fail(orderNo, "对账处理异常: " + e.getMessage());
        }
    }

    /**
     * 异步对账订单
     */
    public CompletableFuture<ReconResult> reconOrderAsync(String orderNo, BigDecimal payAmount,
            BigDecimal platformIncome, BigDecimal payFee, Map<String, BigDecimal> splitDetails,
            PayStatusEnum payStatus, SplitStatusEnum splitStatus, NotifyStatusEnum notifyStatus) {
        return CompletableFuture.supplyAsync(() -> reconOrder(orderNo, payAmount, platformIncome, payFee, splitDetails,
                payStatus, splitStatus, notifyStatus), executorService);
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
            reconRepository.updateReconStatus(orderMainDO.getOrderNo(), ReconStatusEnum.SUCCESS);
            return true;
        } catch (Exception e) {
            alarmService.sendReconAlarm(orderMainDO.getOrderNo(), "SELF",
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
            RefundStatusEnum refundStatus, Map<String, BigDecimal> splitDetails) {
        try {
            RefundStatusEnum refundEnum = refundStatus != null ? refundStatus : RefundStatusEnum.PROCESSING;
            // 1. 查询原订单
            ReconOrderMainDO orderMainDO = reconRepository.getOrderMainByOrderNo(orderNo);
            if (orderMainDO == null) {
                return ReconResult.fail(orderNo, "原订单不存在");
            }

            // 2. 校验退款金额 (退款金额 <= 实付金额)
            if (refundAmount.compareTo(orderMainDO.getPayAmount()) > 0) {
                recordException(orderNo, "SELF", "退款金额大于实付金额", 4);
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
                recordException(orderNo, "SELF", "退款分账总额大于退款金额", 4);
                return ReconResult.fail(orderNo, "退款分账总额大于退款金额");
            }

            // 4. 更新订单主记录的退款状态
            boolean updateSuccess = reconRepository.updateReconRefundStatus(orderNo, refundEnum.getCode(), refundAmount,
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
            recordException(orderNo, "SELF", "退款对账处理异常: " + e.getMessage(), 5);
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
            LocalDateTime refundTime, RefundStatusEnum refundStatus, Map<String, BigDecimal> splitDetails) {
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
                recordException(orderNo, "SELF", "重试对账失败：订单不存在", 0);
                return false;
            }

            // 2. 检查状态：只有 失败(2) 或 待处理(PENDING/0) 可以重试
            if (orderMainDO.getReconStatus() == ReconStatusEnum.SUCCESS.getCode()) {
                return true;
            }

            // 3. 检查业务处理状态
            if (orderMainDO.getPayStatus() == PayStatusEnum.PROCESSING.getCode() ||
                    orderMainDO.getSplitStatus() == SplitStatusEnum.PROCESSING.getCode() ||
                    orderMainDO.getNotifyStatus() == NotifyStatusEnum.PROCESSING.getCode()) {
                // 仍有处理中的步骤，对账保持 PENDING
                return false;
            }

            // 4. 获取分账子记录
            List<ReconOrderSplitSubDO> splitSubDOs = reconRepository.getOrderSplitSubByOrderNo(orderNo);

            // 5. 执行校验逻辑
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitSubDOs != null) {
                for (ReconOrderSplitSubDO sub : splitSubDOs) {
                    splitTotal = splitTotal.add(sub.getSplitAmount());
                }
            }
            BigDecimal calcAmount = splitTotal.add(orderMainDO.getPlatformIncome()).add(orderMainDO.getPayFee());

            if (orderMainDO.getPayAmount().subtract(calcAmount).abs().compareTo(properties.getAmountTolerance()) > 0) {
                recordException(orderNo, "SELF", "重试对账失败：金额校验不一致", 4);
                reconRepository.updateReconStatus(orderNo, ReconStatusEnum.FAILURE);
                return false;
            }

            // 6. 更新状态为成功
            return reconRepository.updateReconStatus(orderNo, ReconStatusEnum.SUCCESS);

        } catch (Exception e) {
            log.error("重试对账异常", e);
            recordException(orderNo, "SELF", "重试对账异常: " + e.getMessage(), 5);
            return false;
        }
    }

    /**
     * 对账通知回调
     *
     * @param orderNo      订单号
     * @param merchantId   商户号
     * @param notifyUrl    通知地址
     * @param notifyStatus 通知状态 (0: 失败, 1: 成功, 2: 重新通知中)
     * @param notifyResult 通知返回结果
     * @return 对账结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconNotify(String orderNo, String merchantId, String notifyUrl, NotifyStatusEnum notifyStatus,
            String notifyResult) {
        try {
            NotifyStatusEnum notifyEnum = notifyStatus != null ? notifyStatus : NotifyStatusEnum.PROCESSING;

            // 1. 更新数据库中的通知状态 (使用专门的字段)
            boolean updated = reconRepository.updateNotifyStatus(orderNo, notifyEnum.getCode(), notifyResult);

            // 2. 记录通知详细日志
            ReconNotifyLogDO notifyLogDO = new ReconNotifyLogDO();
            notifyLogDO.setOrderNo(orderNo);
            notifyLogDO.setMerchantId(merchantId);
            notifyLogDO.setNotifyUrl(notifyUrl);
            notifyLogDO.setNotifyStatus(notifyEnum.getCode());
            notifyLogDO.setNotifyResult(notifyResult);
            notifyLogDO.setCreateTime(LocalDateTime.now());
            notifyLogDO.setUpdateTime(LocalDateTime.now());
            reconRepository.saveNotifyLog(notifyLogDO);

            // 3. 自动触发一次重对账检查
            boolean retrySuccess = retryRecon(orderNo);

            return retrySuccess ? ReconResult.success(orderNo)
                    : ReconResult.success(orderNo, "通知状态已更新，由于其他业务状态未完成，对账仍处于 Pending");
        } catch (Exception e) {
            log.error("通知回调处理异常", e);
            return ReconResult.fail(orderNo, "通知处理异常: " + e.getMessage());
        }
    }

    /**
     * 异步对账通知回调
     */
    public CompletableFuture<ReconResult> reconNotifyAsync(String orderNo, String merchantId, String notifyUrl,
            NotifyStatusEnum notifyStatus, String notifyResult) {
        return CompletableFuture.supplyAsync(
                () -> reconNotify(orderNo, merchantId, notifyUrl, notifyStatus, notifyResult), executorService);
    }

    private void recordException(String orderNo, String merchantId, String msg, int step) {
        exceptionRecordService.recordReconException(orderNo, merchantId, msg, step);
        alarmService.sendReconAlarm(orderNo, merchantId, msg);
    }
}
