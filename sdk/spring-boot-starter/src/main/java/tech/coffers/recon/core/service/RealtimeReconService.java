package tech.coffers.recon.core.service;

import lombok.extern.slf4j.Slf4j;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderRefundDetailDO;
import tech.coffers.recon.entity.ReconOrderSplitDetailDO;
import tech.coffers.recon.entity.ReconOrderSubDO;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.SettlementTypeEnum;
import tech.coffers.recon.api.model.AbstractReconOrderRequest;
import tech.coffers.recon.api.model.AbstractReconRefundRequest;
import tech.coffers.recon.api.model.ReconNotifyRequest;
import tech.coffers.recon.api.model.ReconOrderSplitRequest;
import tech.coffers.recon.api.model.ReconSubOrderRequest;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import tech.coffers.recon.entity.ReconNotifyLogDO;
import tech.coffers.recon.entity.ReconOrderMerchantSettlementDO;
import tech.coffers.recon.repository.ReconRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
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
    private final ExecutorService executorService;

    public RealtimeReconService(ReconRepository reconRepository, ExceptionRecordService exceptionRecordService,
            AlarmService alarmService, ExecutorService executorService) {
        this.reconRepository = reconRepository;
        this.exceptionRecordService = exceptionRecordService;
        this.alarmService = alarmService;
        this.executorService = executorService;
    }

    /**
     * 对账订单 (DTO)
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconOrder(AbstractReconOrderRequest request) {
        if (request == null) {
            return ReconResult.fail(null, "请求参数不能为空");
        }
        String orderNo = request.getOrderNo();
        try {
            // 1. 金额归一化 (BigDecimal -> Long 分)
            Long[] normalizedAmounts = normalizeRequestAmounts(request);
            Long payAmountFen = normalizedAmounts[0];
            Long platformIncomeFen = normalizedAmounts[1];
            Long payFeeFen = normalizedAmounts[2];

            // 2. 业务状态预校验
            PayStatusEnum payEnum = request.getPayStatus() != null ? request.getPayStatus() : PayStatusEnum.SUCCESS;
            SplitStatusEnum splitEnum = request.getSplitStatus() != null ? request.getSplitStatus()
                    : SplitStatusEnum.SUCCESS;
            NotifyStatusEnum notifyEnum = request.getNotifyStatus() != null ? request.getNotifyStatus()
                    : NotifyStatusEnum.PROCESSING;

            ReconStatusEnum reconStatus = ReconStatusEnum.SUCCESS;
            String failMsg = null;

            if (payEnum == PayStatusEnum.PROCESSING || splitEnum == SplitStatusEnum.PROCESSING
                    || notifyEnum == NotifyStatusEnum.PROCESSING) {
                reconStatus = ReconStatusEnum.PENDING;
            }

            if (payEnum == PayStatusEnum.FAILURE) {
                failMsg = "支付状态失败，对账失败";
                recordException(orderNo, "SELF", failMsg, 1);
                reconStatus = ReconStatusEnum.FAILURE;
            } else if (splitEnum == SplitStatusEnum.FAILURE) {
                failMsg = "分账状态失败，对账失败";
                recordException(orderNo, "SELF", failMsg, 2);
                reconStatus = ReconStatusEnum.FAILURE;
            } else if (notifyEnum == NotifyStatusEnum.FAILURE) {
                failMsg = "通知状态失败，对账失败";
                recordException(orderNo, "SELF", failMsg, 3);
                reconStatus = ReconStatusEnum.FAILURE;
            }

            if (reconStatus == ReconStatusEnum.FAILURE) {
                saveReconData(orderNo, request, payEnum, splitEnum, notifyEnum, reconStatus, 0L, 0L, platformIncomeFen,
                        payFeeFen, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
                return ReconResult.fail(orderNo, failMsg);
            }

            // 3. 场景推断与比例准备
            Map<String, SettlementTypeEnum> settlementTypeMap = inferMerchantSettlementType(request);
            Map<String, Integer> ratioMap = request.getSplitRules() == null ? Collections.emptyMap()
                    : request.getSplitRules().stream()
                            .filter(r -> r.getMerchantId() != null && r.getRatio() != null)
                            .collect(Collectors.toMap(r -> r.getMerchantId(), r -> r.getRatio(), (v1, v2) -> v1));

            Double inferredFeeRate = inferFeeRate(request, request.getSplitDetails());

            // 4. 数据转换 DTO -> DO
            List<ReconOrderSubDO> orderSubDOs = convertToIntentDOs(orderNo, request.getSubOrders(), ratioMap,
                    inferredFeeRate);
            List<ReconOrderSplitDetailDO> splitDetailDOs = convertToFactDOs(orderNo, request.getSplitDetails(),
                    settlementTypeMap, notifyEnum);

            // 5. 聚合商户统计
            List<ReconOrderMerchantSettlementDO> mchSettlements = calculateMerchantSettlements(orderNo, request,
                    request.getSplitDetails());

            // 6. 确定平台留存与汇总金额
            long subOrderTotalFen = orderSubDOs.stream()
                    .mapToLong(s -> s.getOrderAmountFen() != null ? s.getOrderAmountFen() : 0L).sum();
            long splitTotalFen = splitDetailDOs.stream()
                    .mapToLong(s -> s.getSplitAmountFen() != null ? s.getSplitAmountFen() : 0L).sum();

            // 计算事实层总手续费
            long totalFactFeeFen = splitDetailDOs.stream()
                    .mapToLong(s -> s.getSplitFeeFen() != null ? s.getSplitFeeFen() : 0L).sum();

            // 如果请求中未传手续费，从事实明细推断
            if (payFeeFen == null || payFeeFen == 0L) {
                payFeeFen = totalFactFeeFen;
            }

            if (platformIncomeFen == null || platformIncomeFen == 0L) {
                // 核算原则：优先从业务意图层计算利润
                long calculatedProfit = 0L;
                if (orderSubDOs != null && !orderSubDOs.isEmpty()) {
                    calculatedProfit = orderSubDOs.stream()
                            .mapToLong(s -> {
                                long ordValue = s.getOrderAmountFen() != null ? s.getOrderAmountFen() : 0L;
                                long splValue = s.getSplitAmountFen() != null ? s.getSplitAmountFen() : 0L;
                                long feeValue = s.getFeeFen() != null ? s.getFeeFen() : 0L;
                                return ordValue - splValue - feeValue;
                            }).sum();
                }

                // 兜底逻辑：如果意图层计算结果为0，按事实层整体差额补充 (支付额 - 通道费 - 外部实付)
                if (calculatedProfit <= 0) {
                    long pAmtValue = payAmountFen != null ? payAmountFen : 0L;
                    long pFeeValue = payFeeFen != null ? payFeeFen : 0L;
                    long externalRealizedValue = 0L;
                    if (splitDetailDOs != null) {
                        externalRealizedValue = splitDetailDOs.stream()
                                .filter(d -> SettlementTypeEnum
                                        .fromCode(d.getSettlementType()) != SettlementTypeEnum.PLATFORM_COLLECTION)
                                .mapToLong(s -> s.getSplitAmountFen() != null ? s.getSplitAmountFen() : 0L).sum();
                    }
                    calculatedProfit = Math.max(0L, pAmtValue - pFeeValue - externalRealizedValue);
                }
                platformIncomeFen = calculatedProfit;
            }

            // 7. 金额多维核账
            if (reconStatus == ReconStatusEnum.SUCCESS) {
                failMsg = performAmountValidation(orderNo, payAmountFen, platformIncomeFen, payFeeFen, splitDetailDOs,
                        settlementTypeMap, orderSubDOs, request.getSubOrders(), mchSettlements);
                if (failMsg != null) {
                    reconStatus = ReconStatusEnum.FAILURE;
                    recordException(orderNo, "SELF", failMsg, 4);
                }
            }

            // 8. 数据持久化
            saveReconData(orderNo, request, payEnum, splitEnum, notifyEnum, reconStatus, subOrderTotalFen,
                    splitTotalFen,
                    platformIncomeFen, payFeeFen, orderSubDOs, mchSettlements, splitDetailDOs);

            if (reconStatus == ReconStatusEnum.FAILURE) {
                return ReconResult.fail(orderNo, failMsg != null ? failMsg : "对账失败：业务状态异常");
            }
            return ReconResult.success(orderNo);

        } catch (Exception e) {
            log.error("对账处理异常 orderNo={}", orderNo, e);
            recordException(orderNo, "SELF", "对账处理异常: " + e.getMessage(), 5);
            return ReconResult.fail(orderNo, "对账处理异常: " + e.getMessage());
        }
    }

    /**
     * 保存对账数据
     * 
     * @param orderNo           订单号
     * @param request           对账请求
     * @param payEnum           支付状态
     * @param splitEnum         分账状态
     * @param notifyEnum        通知状态
     * @param reconStatus       对账状态
     * @param subOrderTotalFen  子订单总金额
     * @param splitTotalFen     分账总金额
     * @param platformIncomeFen 平台留存金额
     * @param orderSubDOs       子订单列表
     * @param settlementDOs     结算单列表
     * @param splitDetailDOs    分账明细列表
     */
    private void saveReconData(String orderNo, AbstractReconOrderRequest request, PayStatusEnum payEnum,
            SplitStatusEnum splitEnum, NotifyStatusEnum notifyEnum, ReconStatusEnum reconStatus,
            long subOrderTotalFen, long splitTotalFen, Long platformIncomeFen, Long payFeeFen,
            List<ReconOrderSubDO> orderSubDOs, List<ReconOrderMerchantSettlementDO> settlementDOs,
            List<ReconOrderSplitDetailDO> splitDetailDOs) {

        // 4. 保存订单主记录
        ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
        orderMainDO.setOrderNo(orderNo);
        request.populateAmounts(orderMainDO);

        if (request.getPlatformIncomeFen() == null && request.getPlatformIncome() == null) {
            if (platformIncomeFen != null) {
                orderMainDO.setPlatformIncomeFen(platformIncomeFen);
                orderMainDO.setPlatformIncome(BigDecimal.valueOf(platformIncomeFen).movePointLeft(2));
            }
        }

        if (payFeeFen != null) {
            orderMainDO.setPayFeeFen(payFeeFen);
            orderMainDO.setPayFee(BigDecimal.valueOf(payFeeFen).movePointLeft(2));
        }

        orderMainDO.setSplitTotalAmountFen(splitTotalFen);
        orderMainDO.setPayStatus(payEnum.getCode());
        orderMainDO.setSplitStatus(splitEnum.getCode());
        orderMainDO.setNotifyStatus(notifyEnum.getCode());
        orderMainDO.setReconStatus(reconStatus.getCode());
        orderMainDO.setCreateTime(LocalDateTime.now());
        orderMainDO.setUpdateTime(LocalDateTime.now());
        reconRepository.saveOrderMain(orderMainDO);

        // 5. 保存业务子单
        if (orderSubDOs != null && !orderSubDOs.isEmpty()) {
            reconRepository.batchSaveOrderSub(orderSubDOs);
        }

        // 6. 保存分账结算记录
        if (settlementDOs != null && !settlementDOs.isEmpty()) {
            reconRepository.batchSaveOrderMerchantSettlement(settlementDOs);
        }

        // 7. 保存分账事实明细
        if (splitDetailDOs != null && !splitDetailDOs.isEmpty()) {
            reconRepository.batchSaveOrderSplitDetail(splitDetailDOs);
        }
    }

    /**
     * 异步对账订单 (DTO)
     */
    public CompletableFuture<ReconResult> reconOrderAsync(AbstractReconOrderRequest request) {
        return CompletableFuture.supplyAsync(() -> reconOrder(request), executorService);
    }

    /**
     * (Delegated from EasyReconApi)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSubDO> orderSubDOs,
            List<ReconOrderSplitDetailDO> splitDetailDOs) {
        try {
            boolean mainSaved = reconRepository.saveOrderMain(orderMainDO);
            if (!mainSaved)
                return false;

            if (orderSubDOs != null && !orderSubDOs.isEmpty()) {
                reconRepository.batchSaveOrderSub(orderSubDOs);
            }

            if (splitDetailDOs != null && !splitDetailDOs.isEmpty()) {
                boolean detailSaved = reconRepository.batchSaveOrderSplitDetail(splitDetailDOs);
                if (!detailSaved)
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
            List<ReconOrderSubDO> orderSubDOs,
            List<ReconOrderSplitDetailDO> splitDetailDOs) {
        return CompletableFuture.supplyAsync(() -> doRealtimeRecon(orderMainDO, orderSubDOs, splitDetailDOs),
                executorService);
    }

    /**
     * 对账退款 (DTO)
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconRefund(AbstractReconRefundRequest request) {
        if (request == null) {
            return ReconResult.fail(null, "请求参数不能为空");
        }

        String orderNo = request.getOrderNo();
        if ((orderNo == null || orderNo.isEmpty()) && request.getMerchantId() != null) {
            if (request.getSubOrderNo() != null) {
                orderNo = reconRepository.findOrderNoBySub(request.getMerchantId(), request.getSubOrderNo());
            } else if (request.getMerchantOrderNo() != null) {
                orderNo = reconRepository.findOrderNoByMerchantOrder(request.getMerchantId(),
                        request.getMerchantOrderNo());
            }
        }

        if (orderNo == null || orderNo.isEmpty()) {
            return ReconResult.fail(null, "无法定位主订单号");
        }

        try {
            ReconOrderMainDO orderMainDO = reconRepository.getOrderMainByOrderNo(orderNo);
            if (orderMainDO == null) {
                return ReconResult.fail(orderNo, "退款对账失败：订单主记录不存在");
            }

            // 更新退款事实明细
            if (request.getSplitDetails() != null && !request.getSplitDetails().isEmpty()) {
                List<ReconOrderRefundDetailDO> refundDetailDOs = request.getSplitDetails().stream()
                        .map(sub -> {
                            ReconOrderRefundDetailDO detail = new ReconOrderRefundDetailDO();
                            detail.setOrderNo(orderMainDO.getOrderNo());
                            detail.setMerchantId(sub.getMerchantId());

                            // 优先使用分单位，避免精度丢失
                            if (sub.getRefundSplitAmountFen() != null) {
                                detail.setRefundSplitAmountFen(sub.getRefundSplitAmountFen());
                            } else {
                                detail.setRefundSplitAmount(sub.getRefundSplitAmount());
                            }

                            detail.setCreateTime(LocalDateTime.now());
                            detail.setUpdateTime(LocalDateTime.now());
                            return detail;
                        }).collect(Collectors.toList());
                reconRepository.batchSaveOrderRefundDetail(refundDetailDOs);
            }

            // 更新主订单退款状态
            orderMainDO.setRefundStatus(request.getRefundStatus().getCode());
            orderMainDO.setRefundAmount(request.getRefundAmount());
            orderMainDO.setRefundTime(request.getRefundTime());
            reconRepository.updateReconRefundStatus(orderMainDO);

            return ReconResult.success(orderNo);
        } catch (Exception e) {
            log.error("退款对账异常", e);
            return ReconResult.fail(orderNo, "退款对账异常: " + e.getMessage());
        }
    }

    /**
     * 异步对账退款 (DTO)
     */
    public CompletableFuture<ReconResult> reconRefundAsync(AbstractReconRefundRequest request) {
        return CompletableFuture.supplyAsync(() -> reconRefund(request), executorService);
    }

    /**
     * 对账通知回调 (DTO)
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconNotify(ReconNotifyRequest request) {
        if (request == null) {
            return ReconResult.fail(null, "请求参数不能为空");
        }
        return reconNotify(request.getOrderNo(), request.getMerchantId(), request.getSubOrderNo(),
                request.getMerchantOrderNo(), request.getNotifyUrl(), request.getNotifyStatus(),
                request.getNotifyResult());
    }

    /**
     * 异步对账通知回调 (DTO)
     */
    public CompletableFuture<ReconResult> reconNotifyAsync(ReconNotifyRequest request) {
        return CompletableFuture.supplyAsync(() -> reconNotify(request), executorService);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReconResult reconNotify(String orderNo, String merchantId, String subOrderNo, String merchantOrderNo,
            String notifyUrl, NotifyStatusEnum notifyStatus, String notifyResult) {
        try {
            if ((orderNo == null || orderNo.isEmpty()) && merchantId != null) {
                if (subOrderNo != null) {
                    orderNo = reconRepository.findOrderNoBySub(merchantId, subOrderNo);
                } else if (merchantOrderNo != null) {
                    orderNo = reconRepository.findOrderNoByMerchantOrder(merchantId, merchantOrderNo);
                }

                if (orderNo == null) {
                    return ReconResult.fail(null, "无法根据商户号和子订单标识定位主订单");
                }
            }

            if (orderNo == null || orderNo.isEmpty()) {
                return ReconResult.fail(null, "订单号不能为空");
            }

            NotifyStatusEnum notifyEnum = notifyStatus != null ? notifyStatus : NotifyStatusEnum.PROCESSING;

            if (merchantId != null && !"SELF".equals(merchantId)) {
                reconRepository.updateSplitDetailNotifyStatus(orderNo, merchantId, notifyEnum.getCode(),
                        notifyResult);
            }

            boolean allNotified = reconRepository.isAllSplitSubNotified(orderNo);
            if (allNotified) {
                reconRepository.updateNotifyStatus(orderNo, NotifyStatusEnum.SUCCESS.getCode(),
                        "All merchants notified");
            } else if (notifyEnum == NotifyStatusEnum.FAILURE) {
                reconRepository.updateNotifyStatus(orderNo, NotifyStatusEnum.FAILURE.getCode(),
                        "Merchant " + merchantId + " notify failed");
            }

            ReconNotifyLogDO notifyLogDO = new ReconNotifyLogDO();
            notifyLogDO.setOrderNo(orderNo);
            notifyLogDO.setSubOrderNo(subOrderNo);
            notifyLogDO.setMerchantId(merchantId);
            notifyLogDO.setNotifyUrl(notifyUrl);
            notifyLogDO.setNotifyStatus(notifyEnum.getCode());
            notifyLogDO.setNotifyResult(notifyResult);
            notifyLogDO.setCreateTime(LocalDateTime.now());
            notifyLogDO.setUpdateTime(LocalDateTime.now());
            reconRepository.saveNotifyLog(notifyLogDO);

            boolean retrySuccess = retryRecon(orderNo);

            return retrySuccess ? ReconResult.success(orderNo)
                    : ReconResult.success(orderNo, "通知状态已更新，等待所有业务闭环");
        } catch (Exception e) {
            log.error("通知回调处理异常", e);
            return ReconResult.fail(orderNo, "通知处理异常: " + e.getMessage());
        }
    }

    public CompletableFuture<ReconResult> reconNotifyBySubAsync(String merchantId, String subOrderNo,
            String notifyUrl, NotifyStatusEnum notifyStatus, String notifyResult) {
        return CompletableFuture.supplyAsync(
                () -> reconNotify(null, merchantId, subOrderNo, null, notifyUrl, notifyStatus, notifyResult),
                executorService);
    }

    public CompletableFuture<ReconResult> reconNotifyByMerchantOrderAsync(String merchantId, String merchantOrderNo,
            String notifyUrl, NotifyStatusEnum notifyStatus, String notifyResult) {
        return CompletableFuture.supplyAsync(
                () -> reconNotify(null, merchantId, null, merchantOrderNo, notifyUrl, notifyStatus, notifyResult),
                executorService);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean retryRecon(String orderNo) {
        try {
            ReconOrderMainDO orderMainDO = reconRepository.getOrderMainByOrderNo(orderNo);
            if (orderMainDO == null) {
                recordException(orderNo, "SELF", "重试对账失败：订单不存在", 0);
                return false;
            }

            if (orderMainDO.getReconStatus() == ReconStatusEnum.SUCCESS.getCode()) {
                return true;
            }

            if (orderMainDO.getPayStatus() == PayStatusEnum.PROCESSING.getCode() ||
                    orderMainDO.getSplitStatus() == SplitStatusEnum.PROCESSING.getCode() ||
                    orderMainDO.getNotifyStatus() == NotifyStatusEnum.PROCESSING.getCode()) {
                return false;
            }

            List<ReconOrderSplitDetailDO> splitDetailDOs = reconRepository.getOrderSplitDetailByOrderNo(orderNo);

            // 重新推断当前事实对应的到账方式进行校验

            if (!verifyMacroMatch(orderMainDO.getPayAmountFen(), orderMainDO.getPlatformIncomeFen(),
                    orderMainDO.getPayFeeFen(), splitDetailDOs)) {
                recordException(orderNo, "SELF", "重试对账失败：金额校验不平", 4);
                reconRepository.updateReconStatus(orderNo, ReconStatusEnum.FAILURE);
                return false;
            }

            return reconRepository.updateReconStatus(orderNo, ReconStatusEnum.SUCCESS);

        } catch (Exception e) {
            log.error("重试对账异常", e);
            recordException(orderNo, "SELF", "重试对账异常: " + e.getMessage(), 5);
            return false;
        }
    }

    private void recordException(String orderNo, String merchantId, String msg, int step) {
        exceptionRecordService.recordReconException(orderNo, merchantId, msg, step);
        alarmService.sendReconAlarm(orderNo, merchantId, msg);
    }

    /**
     * 校验宏观金额是否匹配 (支持混合模式)
     */
    private boolean verifyMacroMatch(Long payAmountFen, Long platformIncomeFen, Long payFeeFen,
            List<ReconOrderSplitDetailDO> splitDetailDOs) {
        long payAmtFen = payAmountFen != null ? payAmountFen : 0L;
        long platIncFen = platformIncomeFen != null ? platformIncomeFen : 0L;
        long pFeeFen = payFeeFen != null ? payFeeFen : 0L;

        // 核心修正逻辑：
        // 1. 如果事实层明细中包含了总额中转账号 (PLATFORM_COLLECTION)，其实收金额(splitAmountFen) 就是总额的体现。
        // 2. 如果只有业务商户的事实明细，则需要加回平台留存和手续费。
        long sumFactSplitsFen = 0L;
        long sumPlatformCollectedGrossFen = 0L;

        if (splitDetailDOs != null) {
            for (ReconOrderSplitDetailDO fact : splitDetailDOs) {
                long splAmt = fact.getSplitAmountFen() != null ? fact.getSplitAmountFen() : 0L;
                sumFactSplitsFen += splAmt;

                SettlementTypeEnum type = SettlementTypeEnum.fromCode(fact.getSettlementType());
                // 平台代收 (1) 和 全额到商户 (2) 均属于总额类事实，其金额本质上是支付总额（Gross）
                if (type == SettlementTypeEnum.PLATFORM_COLLECTION || type == SettlementTypeEnum.DIRECT_TO_MERCHANT) {
                    sumPlatformCollectedGrossFen += splAmt;
                }
            }
        }

        // 1. 如果存在总额类事实（平台代收或全额到商户），且金额基本等于总支付额，则认为宏观已平
        if (sumPlatformCollectedGrossFen > 0 && Math.abs(payAmtFen - sumPlatformCollectedGrossFen) <= 1) {
            return true;
        }

        // 2. 通用资金守恒公式 (Ledger Balance)：
        // 支付总额 == Σ(所有分账事实总额) + 平台利润 + 通道总手续费
        return Math.abs(payAmtFen - (sumFactSplitsFen + platIncFen + pFeeFen)) <= 1;
    }

    /**
     * 计算并生成商户维度的结算统计信息
     * <p>
     * 该方法对比“意图层”(Intents) 与“事实层”(Facts)，为订单涉及的每个商户计算结算数据和判定结算方式。
     * 核心逻辑基于商户 ID 聚合，分别计算订单金额、分账金额、手续费，并据此推断该商户的到账类型。
     * </p>
     *
     * @param orderNo 主订单号
     * @param request 对账请求对象 (包含意图子单列表)
     * @param facts   分账事实明细列表 (来自支付平台回调)
     * @return 该订单涉及的所有商户结算统计列表
     */
    private List<ReconOrderMerchantSettlementDO> calculateMerchantSettlements(String orderNo,
            AbstractReconOrderRequest request, List<ReconOrderSplitRequest> facts) {
        List<ReconSubOrderRequest> intents = request.getSubOrders();
        Map<String, List<ReconSubOrderRequest>> intentMap = intents == null ? Collections.emptyMap()
                : intents.stream().filter(s -> s.getMerchantId() != null)
                        .collect(Collectors.groupingBy(ReconSubOrderRequest::getMerchantId));

        Map<String, List<ReconOrderSplitRequest>> factMap = facts == null ? Collections.emptyMap()
                : facts.stream().filter(s -> s.getMerchantId() != null)
                        .collect(Collectors.groupingBy(ReconOrderSplitRequest::getMerchantId));

        // 提取分账比例规则: merchantId -> ratio
        Map<String, Integer> ratioMap = Collections.emptyMap();
        if (request.getSplitRules() != null) {
            ratioMap = request.getSplitRules().stream()
                    .filter(r -> r.getMerchantId() != null && r.getRatio() != null)
                    .collect(Collectors.toMap(
                            tech.coffers.recon.api.model.ReconSplitRuleRequest::getMerchantId,
                            tech.coffers.recon.api.model.ReconSplitRuleRequest::getRatio,
                            (v1, v2) -> v1 // 冲突时保留前者
                    ));
        }

        Set<String> allMerchants = new HashSet<>();
        allMerchants.addAll(intentMap.keySet());
        allMerchants.addAll(factMap.keySet());
        allMerchants.addAll(ratioMap.keySet()); // 也包含只有规则的商户

        List<ReconOrderMerchantSettlementDO> results = new ArrayList<>();
        for (String mchId : allMerchants) {
            long orderAmt = intentMap.getOrDefault(mchId, Collections.emptyList()).stream()
                    .mapToLong(s -> s.getOrderAmountFen() != null ? s.getOrderAmountFen() : 0L).sum();

            // 如果 orderAmt 为 0，尝试取 getSplitAmountFen (兼容不同字段命名习惯)
            if (orderAmt == 0) {
                orderAmt = intentMap.getOrDefault(mchId, Collections.emptyList()).stream()
                        .mapToLong(s -> s.getSplitAmountFen() != null ? s.getSplitAmountFen() : 0L).sum();
            }

            long splitAmt = factMap.getOrDefault(mchId, Collections.emptyList()).stream()
                    .mapToLong(s -> s.getSplitAmountFen() != null ? s.getSplitAmountFen() : 0L).sum();

            long splitFee = factMap.getOrDefault(mchId, Collections.emptyList()).stream()
                    .mapToLong(s -> s.getSplitFeeFen() != null ? s.getSplitFeeFen() : 0L).sum();

            Integer ratio = ratioMap.get(mchId);
            SettlementTypeEnum type;
            // 场景判定基于 意图(Intents) 与 事实(Facts) 的关系：
            if (!intentMap.containsKey(mchId) && !factMap.containsKey(mchId)) {
                // 只有规则，没有意图和事实 -> 忽略
                continue;
            } else if (!intentMap.containsKey(mchId) || !factMap.containsKey(mchId)) {
                // 1. 平台代收：意图中有商户但事实中没有，或者事实中有商户(如平台收款号)但意图中没有
                type = SettlementTypeEnum.PLATFORM_COLLECTION;
            } else {
                // 2. 全额到账商户：subOrders 中商户的订单总额 == splitDetails 中相应商户的收款总额
                if (orderAmt == splitAmt) {
                    type = SettlementTypeEnum.DIRECT_TO_MERCHANT;
                } else if (orderAmt < splitAmt) {
                    // 3. 空中分账：subOrders 中商户的订单总额 < splitDetails 中相应商户的收款总额
                    type = SettlementTypeEnum.REALTIME_SPLIT;
                } else {
                    // 订单总额 > 事实收款总额 (例如手续费已扣除)，作为异常处理或归类为 REALTIME_SPLIT
                    type = SettlementTypeEnum.REALTIME_SPLIT;
                }
            }

            results.add(ReconOrderMerchantSettlementDO.builder()
                    .orderNo(orderNo)
                    .merchantId(mchId)
                    .settlementType(type)
                    .orderAmountFen(orderAmt)
                    .splitAmountFen(splitAmt)
                    .splitFeeFen(splitFee)
                    .splitRatio(ratio) // 记录使用的比例
                    .arrivalAmountFen(splitAmt - splitFee)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build());
        }
        return results;
    }

    /**
     * 智能推断全量订单的到账方式 (Settlement Type)
     * <p>
     * 该方法从宏观层面分析一个对账请求的结算特征。它通过对比“意图层”(Intents, 子订单) 与 “事实层”(Facts, 分账明细)
     * 的商户分布和金额特征，判定该笔订单整体属于哪种到账模式。
     * </p>
     * <ul>
     * <li>{@link SettlementTypeEnum#REALTIME_SPLIT}: 实时分账 -
     * 事实层涉及多个收款方，或单收款方但存在平台分润。</li>
     * <li>{@link SettlementTypeEnum#DIRECT_TO_MERCHANT}: 全额到商户 -
     * 直连模式，订单总额全量实时进入商户余额。</li>
     * <li>{@link SettlementTypeEnum#PLATFORM_COLLECTION}: 平台代收 -
     * 资金进入平台大账户，或意图与事实收款方严重不匹配。</li>
     * </ul>
     *
     * @param request 对账请求
     * @return 推断出的到账方式
     */
    private Map<String, SettlementTypeEnum> inferMerchantSettlementType(AbstractReconOrderRequest request) {
        // 1. 获取事实层数据 (Facts)
        List<ReconOrderSplitRequest> facts = request.getSplitDetails();
        if (facts == null || facts.isEmpty()) {
            return Collections.emptyMap();
        }

        // 聚合事实层商户收款金额 (用于精度校验或扩展)
        Map<String, Long> factSplitAmountMap = new HashMap<>();
        for (ReconOrderSplitRequest fact : facts) {
            if (fact.getMerchantId() != null && fact.getSplitAmountFen() != null) {
                factSplitAmountMap.put(fact.getMerchantId(),
                        factSplitAmountMap.getOrDefault(fact.getMerchantId(), 0L) + fact.getSplitAmountFen());
            }
        }

        // 2. 获取意图层数据 (Intents)
        List<ReconSubOrderRequest> subOrders = request.getSubOrders();
        // 聚合意图层商户订单金额
        Map<String, Long> merchantOrderAmountMap = new HashMap<>();

        if (subOrders != null && !subOrders.isEmpty()) {
            for (ReconSubOrderRequest subOrder : subOrders) {
                String merchantId = subOrder.getMerchantId();
                Long orderAmountFen = subOrder.getOrderAmountFen();
                if (merchantId != null && orderAmountFen != null) {
                    merchantOrderAmountMap.put(merchantId,
                            merchantOrderAmountMap.getOrDefault(merchantId, 0L) + orderAmountFen);
                }
            }
        }

        // Also include merchants from facts even if they are not in intent
        for (String factMchId : factSplitAmountMap.keySet()) {
            if (!merchantOrderAmountMap.containsKey(factMchId)) {
                merchantOrderAmountMap.put(factMchId, 0L);
            }
        }

        Map<String, SettlementTypeEnum> results = new HashMap<>();

        for (String merchantId : merchantOrderAmountMap.keySet()) {
            if (merchantId == null)
                continue;
            Long merchantOrderAmount = merchantOrderAmountMap.get(merchantId);
            Long merchantFactAmount = factSplitAmountMap.getOrDefault(merchantId, 0L);

            if (merchantFactAmount == 0) {
                // 有意图无事实 -> 平台代收
                results.put(merchantId, SettlementTypeEnum.PLATFORM_COLLECTION);
            } else if (merchantOrderAmount == 0 && merchantFactAmount > 0) {
                // 无意图有事实 -> 典型为平台手续费账号 -> 平台代收
                results.put(merchantId, SettlementTypeEnum.PLATFORM_COLLECTION);
            } else if (merchantOrderAmount.equals(merchantFactAmount)) {
                // 订单金额等于收款金额，全额到商户
                results.put(merchantId, SettlementTypeEnum.DIRECT_TO_MERCHANT);
            } else {
                // 订单金额不等于收款金额（无论大小），归类为实时分账
                results.put(merchantId, SettlementTypeEnum.REALTIME_SPLIT);
            }
        }

        return results;
    }

    /**
     * 提取 DTO 中的金额并归一化为分 (Long)
     */
    private Long[] normalizeRequestAmounts(AbstractReconOrderRequest request) {
        Long payAmtFen = request.getPayAmountFen();
        if (payAmtFen == null && request.getPayAmount() != null) {
            payAmtFen = request.getPayAmount().multiply(new BigDecimal("100")).longValue();
        }
        Long platIncFen = request.getPlatformIncomeFen();
        if (platIncFen == null && request.getPlatformIncome() != null) {
            platIncFen = request.getPlatformIncome().multiply(new BigDecimal("100")).longValue();
        }
        Long pFeeFen = request.getPayFeeFen();
        if (pFeeFen == null && request.getPayFee() != null) {
            pFeeFen = request.getPayFee().multiply(new BigDecimal("100")).longValue();
        }
        return new Long[] { payAmtFen, platIncFen, pFeeFen };
    }

    /**
     * 转换意向子订单 DTO 为 DO 列表
     */
    private List<ReconOrderSubDO> convertToIntentDOs(String orderNo, List<ReconSubOrderRequest> subOrders,
            Map<String, Integer> ratioMap, Double inferredFeeRate) {
        List<ReconOrderSubDO> orderSubDOs = new ArrayList<>();
        if (subOrders == null)
            return orderSubDOs;

        for (ReconSubOrderRequest subReq : subOrders) {
            ReconOrderSubDO subDO = new ReconOrderSubDO();
            subDO.setOrderNo(orderNo);
            subDO.setMerchantId(subReq.getMerchantId());
            subDO.setSubOrderNo(subReq.getSubOrderNo());
            subDO.setMerchantOrderNo(subReq.getMerchantOrderNo());

            // 设置订单金额
            long currentSubAmtFen = 0L;
            if (subReq.getOrderAmountFen() != null && subReq.getOrderAmountFen() != 0L) {
                currentSubAmtFen = subReq.getOrderAmountFen();
                subDO.setOrderAmountFen(currentSubAmtFen);
            } else if (subReq.getOrderAmount() != null) {
                subDO.setOrderAmount(subReq.getOrderAmount());
                currentSubAmtFen = subReq.getOrderAmount().multiply(new BigDecimal("100")).longValue();
                subDO.setOrderAmountFen(currentSubAmtFen);
            } else if (subReq.getSplitAmountFen() != null && subReq.getSplitAmountFen() != 0L) {
                // 如果订单金额缺失但分账金额存在，至少订单金额等于分账金额
                currentSubAmtFen = subReq.getSplitAmountFen();
                subDO.setOrderAmountFen(currentSubAmtFen);
            } else if (subReq.getSplitAmount() != null) {
                currentSubAmtFen = subReq.getSplitAmount().multiply(new BigDecimal("100")).longValue();
                subDO.setOrderAmountFen(currentSubAmtFen);
            }

            // 设置分账金额
            if (subReq.getSplitAmountFen() != null && subReq.getSplitAmountFen() != 0L) {
                subDO.setSplitAmountFen(subReq.getSplitAmountFen());
            } else if (subReq.getSplitAmount() != null && subReq.getSplitAmount().compareTo(BigDecimal.ZERO) != 0) {
                subDO.setSplitAmountFen(subReq.getSplitAmount().multiply(new BigDecimal(100)).longValue());
            } else if (ratioMap.containsKey(subReq.getMerchantId()) && currentSubAmtFen > 0) {
                long inferredSplit = currentSubAmtFen * ratioMap.get(subReq.getMerchantId()) / 10000;
                subDO.setSplitAmountFen(inferredSplit);
                subDO.setSplitAmount(BigDecimal.valueOf(inferredSplit).movePointLeft(2));
            } else {
                subDO.setSplitAmountFen(0L);
            }

            // 设置手续费
            if (subReq.getFeeFen() != null && subReq.getFeeFen() != 0L) {
                subDO.setFeeFen(subReq.getFeeFen());
            } else if (subReq.getFee() != null && subReq.getFee().compareTo(BigDecimal.ZERO) != 0) {
                subDO.setFeeFen(subReq.getFee().multiply(new BigDecimal(100)).longValue());
            } else if (inferredFeeRate != null && subDO.getSplitAmountFen() != null && subDO.getSplitAmountFen() > 0) {
                long inferredFee = (long) (subDO.getSplitAmountFen() * inferredFeeRate);
                subDO.setFeeFen(inferredFee);
                subDO.setFee(BigDecimal.valueOf(inferredFee).movePointLeft(2));
            } else {
                subDO.setFeeFen(0L);
            }
            subDO.setCreateTime(LocalDateTime.now());
            subDO.setUpdateTime(LocalDateTime.now());

            if (ratioMap.containsKey(subReq.getMerchantId())) {
                subDO.setSplitRatio(ratioMap.get(subReq.getMerchantId()));
            }

            orderSubDOs.add(subDO);
        }
        return orderSubDOs;
    }

    /**
     * 转换事实明细 DTO 为 DO 列表
     */
    private List<ReconOrderSplitDetailDO> convertToFactDOs(String orderNo, List<ReconOrderSplitRequest> splitDetails,
            Map<String, SettlementTypeEnum> settlementTypeMap, NotifyStatusEnum notifyEnum) {
        List<ReconOrderSplitDetailDO> splitDetailDOs = new ArrayList<>();
        if (splitDetails == null)
            return splitDetailDOs;

        for (ReconOrderSplitRequest subReq : splitDetails) {
            ReconOrderSplitDetailDO subDO = new ReconOrderSplitDetailDO();
            subDO.setOrderNo(orderNo);
            subDO.setMerchantId(subReq.getMerchantId());

            if (subReq.getSplitAmountFen() != null) {
                subDO.setSplitAmountFen(subReq.getSplitAmountFen());
            } else if (subReq.getSplitAmount() != null) {
                subDO.setSplitAmountFen(subReq.getSplitAmount().multiply(new BigDecimal(100)).longValue());
            }

            if (subReq.getArrivalAmountFen() != null) {
                subDO.setArrivalAmountFen(subReq.getArrivalAmountFen());
            } else if (subReq.getArrivalAmount() != null) {
                subDO.setArrivalAmountFen(subReq.getArrivalAmount().multiply(new BigDecimal(100)).longValue());
            }

            if (subReq.getSplitFeeFen() != null) {
                subDO.setSplitFeeFen(subReq.getSplitFeeFen());
            } else if (subReq.getSplitFee() != null) {
                subDO.setSplitFeeFen(subReq.getSplitFee().multiply(new BigDecimal(100)).longValue());
            }

            subDO.setNotifyStatus(notifyEnum.getCode());
            subDO.setCreateTime(LocalDateTime.now());
            subDO.setUpdateTime(LocalDateTime.now());

            SettlementTypeEnum sType = settlementTypeMap.get(subReq.getMerchantId());
            subDO.setSettlementType(sType != null ? sType.getCode() : SettlementTypeEnum.UNKNOWN.getCode());

            splitDetailDOs.add(subDO);
        }
        return splitDetailDOs;
    }

    /**
     * 推算手续费比例
     */
    private Double inferFeeRate(AbstractReconOrderRequest request, List<ReconOrderSplitRequest> splitDetails) {
        if (request.getPayFeeRate() != null) {
            return request.getPayFeeRate() / 10000.0;
        }
        if (splitDetails != null && !splitDetails.isEmpty()) {
            long totalFactAmt = 0L;
            long totalFactFee = 0L;
            for (ReconOrderSplitRequest fact : splitDetails) {
                totalFactAmt += (fact.getSplitAmountFen() != null ? fact.getSplitAmountFen() : 0L);
                totalFactFee += (fact.getSplitFeeFen() != null ? fact.getSplitFeeFen() : 0L);
            }
            if (totalFactAmt > 0) {
                return (double) totalFactFee / totalFactAmt;
            }
        }
        return null;
    }

    /**
     * 执行宏观和微观金额校验
     */
    private String performAmountValidation(String orderNo, Long payAmountFen, Long platformIncomeFen, Long payFeeFen,
            List<ReconOrderSplitDetailDO> splitDetailDOs, Map<String, SettlementTypeEnum> settlementTypeMap,
            List<ReconOrderSubDO> orderSubDOs, List<ReconSubOrderRequest> subOrders,
            List<ReconOrderMerchantSettlementDO> mchSettlements) {

        long payAmtFen = payAmountFen != null ? payAmountFen : 0L;
        long platIncFen = platformIncomeFen != null ? platformIncomeFen : 0L;
        long pFeeFen = payFeeFen != null ? payFeeFen : 0L;

        // 1. 宏观校验
        if (!verifyMacroMatch(payAmountFen, platformIncomeFen, payFeeFen, splitDetailDOs)) {
            String msg = String.format("宏观金额校验不符。支付(%d) != [商户汇总 + 平台留存(%d) + 手续费(%d)]。请确认分账明细中是否包含总额中转账号。",
                    payAmtFen, platIncFen, pFeeFen);
            return msg;
        }

        // 2. 微观校验 (商户维度)
        // 核心原则：只对非“平台代收”模式的商户进行 1:1 事实匹配。支持 1:N (一个事实对应多个子单意图)
        if (!orderSubDOs.isEmpty() && !splitDetailDOs.isEmpty()) {
            // 聚合事实层：merchantId -> sum(fact.splitAmount)
            Map<String, Long> factMap = splitDetailDOs.stream()
                    .filter(s -> s.getMerchantId() != null)
                    .collect(Collectors.groupingBy(ReconOrderSplitDetailDO::getMerchantId,
                            Collectors.summingLong(s -> s.getSplitAmountFen() == null ? 0L : s.getSplitAmountFen())));

            // 聚合意图层：merchantId -> { sum(orderAmount), sum(splitAmount) }
            Map<String, Long> mchOrderAmtMap = orderSubDOs.stream()
                    .filter(s -> s.getMerchantId() != null)
                    .collect(Collectors.groupingBy(ReconOrderSubDO::getMerchantId,
                            Collectors.summingLong(s -> s.getOrderAmountFen() == null ? 0L : s.getOrderAmountFen())));

            Map<String, Long> mchSplitAmtMap = orderSubDOs.stream()
                    .filter(s -> s.getMerchantId() != null)
                    .collect(Collectors.groupingBy(ReconOrderSubDO::getMerchantId,
                            Collectors.summingLong(s -> s.getSplitAmountFen() == null ? 0L : s.getSplitAmountFen())));

            Set<String> allMchs = new HashSet<>();
            allMchs.addAll(mchOrderAmtMap.keySet());
            allMchs.addAll(factMap.keySet());

            for (String mchId : allMchs) {
                SettlementTypeEnum type = settlementTypeMap.get(mchId);
                // 如果是平台代收，商户账上通常没有对应事实明细，跳过微观匹配（由宏观中转包校验确保）
                if (type == SettlementTypeEnum.PLATFORM_COLLECTION) {
                    continue;
                }

                Long factAmountFen = factMap.getOrDefault(mchId, 0L);
                long targetReconAmtFen;

                if (type == SettlementTypeEnum.DIRECT_TO_MERCHANT) {
                    // 如果是全额到账 (Rule 2)，比对的是该商户的所有订单原始金额之和
                    targetReconAmtFen = mchOrderAmtMap.getOrDefault(mchId, 0L);
                } else {
                    // 默认/实时分账 (Rule 3)，比对的是该商户的所有预计分账金额之和
                    targetReconAmtFen = mchSplitAmtMap.getOrDefault(mchId, 0L);
                }

                if (Math.abs(targetReconAmtFen - factAmountFen) > 1) {
                    return String.format("商户[%s]微观核账不符。意向(%d分)与事实(%d分)不匹配",
                            mchId, targetReconAmtFen, factAmountFen);
                }
            }
        }
        return null; // 校验通过
    }
}
