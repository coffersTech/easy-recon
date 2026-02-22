package tech.coffers.recon.api;

import tech.coffers.recon.api.result.*;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.model.AbstractReconOrderRequest;
import tech.coffers.recon.api.model.AbstractReconRefundRequest;
import tech.coffers.recon.api.model.ReconNotifyRequest;
import tech.coffers.recon.core.service.RealtimeReconService;
import tech.coffers.recon.core.service.TimingReconService;
import tech.coffers.recon.entity.*;
import tech.coffers.recon.repository.ReconRepository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Easy Recon API
 * <p>
 * 对外暴露的统一 API 入口，提供对账和查询能力。
 * 替代原 EasyReconTemplate。
 * </p>
 *
 * @author Ryan
 * @since 1.1.0
 */
public class EasyReconApi {

    private final RealtimeReconService realtimeReconService;
    private final TimingReconService timingReconService;
    private final ReconRepository reconRepository;

    public EasyReconApi(RealtimeReconService realtimeReconService, TimingReconService timingReconService,
            ReconRepository reconRepository) {
        this.realtimeReconService = realtimeReconService;
        this.timingReconService = timingReconService;
        this.reconRepository = reconRepository;
    }

    // ==================== 实时对账 ====================

    /**
     * 对账订单 (DTO)
     *
     * @param request 对账请求
     * @return 对账结果
     */
    public ReconResult reconOrder(AbstractReconOrderRequest request) {
        return realtimeReconService.reconOrder(request);
    }

    /**
     * 异步对账订单 (DTO)
     *
     * @param request 对账请求
     * @return 对账结果异步句柄
     */
    public CompletableFuture<ReconResult> reconOrderAsync(AbstractReconOrderRequest request) {
        return realtimeReconService.reconOrderAsync(request);
    }

    /**
     * 异步处理对账通知回调 (DTO)
     */
    public CompletableFuture<ReconResult> reconNotifyAsync(ReconNotifyRequest request) {
        return realtimeReconService.reconNotifyAsync(request);
    }

    public boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSubDO> orderSubDOs,
            List<ReconOrderSplitDetailDO> splitDetailDOs) {
        return realtimeReconService.doRealtimeRecon(orderMainDO, orderSubDOs, splitDetailDOs);
    }

    public CompletableFuture<Boolean> doRealtimeReconAsync(ReconOrderMainDO orderMainDO,
            List<ReconOrderSubDO> orderSubDOs,
            List<ReconOrderSplitDetailDO> splitDetailDOs) {
        return realtimeReconService.doRealtimeReconAsync(orderMainDO, orderSubDOs, splitDetailDOs);
    }

    // ==================== 退款对账 ====================

    /**
     * 执行退款对账 (DTO)
     *
     * @param request 退款请求
     * @return 对账结果
     */
    public ReconResult reconRefund(AbstractReconRefundRequest request) {
        return realtimeReconService.reconRefund(request);
    }

    /**
     * 异步执行退款对账 (DTO)
     */
    public CompletableFuture<ReconResult> reconRefundAsync(AbstractReconRefundRequest request) {
        return realtimeReconService.reconRefundAsync(request);
    }

    // ==================== 定时对账触发 ====================

    public boolean doTimingRecon(String dateStr) {
        return timingReconService.doTimingRecon(dateStr);
    }

    public boolean doTimingRefundRecon(String dateStr) {
        return timingReconService.doTimingRefundRecon(dateStr);
    }

    // ==================== 查询能力 ====================

    public ReconStatusEnum getReconStatus(String orderNo) {
        Integer code = reconRepository.getReconStatus(orderNo);
        return ReconStatusEnum.fromCode(code);
    }

    public ReconOrderMainResult getOrderMain(String orderNo) {
        ReconOrderMainDO orderMainDO = reconRepository.getOrderMainByOrderNo(orderNo);
        return mapToOrderMainResult(orderMainDO);
    }

    public List<ReconExceptionResult> getReconExceptions(String orderNo) {
        List<ReconExceptionDO> exceptions = reconRepository.getExceptionsByOrderNo(orderNo);
        if (exceptions == null) {
            return Collections.emptyList();
        }
        return exceptions.stream().map(this::mapToExceptionResult).collect(Collectors.toList());
    }

    public ReconSummaryResult getReconSummary(String dateStr) {
        ReconSummaryDO summaryDO = reconRepository.getReconSummary(dateStr);
        return mapToSummaryResult(summaryDO);
    }

    public List<ReconOrderSplitDetailResult> getSplitDetails(String orderNo) {
        List<ReconOrderSplitDetailDO> details = reconRepository.getOrderSplitDetailByOrderNo(orderNo);
        if (details == null) {
            return Collections.emptyList();
        }
        return details.stream().map(this::mapToSplitDetailResult).collect(Collectors.toList());
    }

    public List<ReconOrderSubResult> getOrderSubs(String orderNo) {
        List<ReconOrderSubDO> subs = reconRepository.getOrderSubByOrderNo(orderNo);
        if (subs == null) {
            return Collections.emptyList();
        }
        return subs.stream().map(this::mapToOrderSubResult).collect(Collectors.toList());
    }

    public List<ReconOrderRefundDetailResult> getRefundSplitDetails(String orderNo) {
        List<ReconOrderRefundDetailDO> details = reconRepository.getOrderRefundDetailByOrderNo(orderNo);
        if (details == null) {
            return Collections.emptyList();
        }
        return details.stream().map(this::mapToRefundDetailResult).collect(Collectors.toList());
    }

    public List<ReconNotifyLogResult> getNotifyLogs(String orderNo) {
        List<ReconNotifyLogDO> logs = reconRepository.getNotifyLogsByOrderNo(orderNo);
        if (logs == null) {
            return Collections.emptyList();
        }
        return logs.stream().map(this::mapToNotifyLogResult).collect(Collectors.toList());
    }

    public PageResult<ReconOrderMainResult> listOrdersByDate(String dateStr, ReconStatusEnum reconStatus, int page,
            int size) {
        int offset = (page - 1) * size;
        List<ReconOrderMainDO> list = reconRepository.getOrderMainByDate(dateStr, reconStatus, offset, size);
        long total = reconRepository.countOrderMainByDate(dateStr, reconStatus);

        List<ReconOrderMainResult> resultList = list.stream()
                .map(this::mapToOrderMainResult)
                .collect(Collectors.toList());

        return PageResult.of(resultList, total, page, size);
    }

    public PageResult<ReconExceptionResult> listExceptions(String merchantId, String startDate, String endDate,
            Integer exceptionStep, int page, int size) {
        int offset = (page - 1) * size;
        List<ReconExceptionDO> list = reconRepository.getExceptionRecords(merchantId, startDate, endDate, exceptionStep,
                offset, size);
        long total = reconRepository.countExceptionRecords(merchantId, startDate, endDate, exceptionStep);

        List<ReconExceptionResult> resultList = list.stream()
                .map(this::mapToExceptionResult)
                .collect(Collectors.toList());

        return PageResult.of(resultList, total, page, size);
    }

    // ==================== 私有映射逻辑 ====================

    private ReconOrderMainResult mapToOrderMainResult(ReconOrderMainDO doObj) {
        if (doObj == null)
            return null;
        ReconOrderMainResult res = new ReconOrderMainResult();
        res.setOrderNo(doObj.getOrderNo());
        res.setPayAmount(doObj.getPayAmount());
        res.setPlatformIncome(doObj.getPlatformIncome());
        res.setPayFee(doObj.getPayFee());
        res.setSplitTotalAmount(doObj.getSplitTotalAmount());
        res.setPayAmountFen(doObj.getPayAmountFen());
        res.setPlatformIncomeFen(doObj.getPlatformIncomeFen());
        res.setPayFeeFen(doObj.getPayFeeFen());
        res.setSplitTotalAmountFen(doObj.getSplitTotalAmountFen());
        res.setReconStatus(doObj.getReconStatus());
        res.setPayStatus(doObj.getPayStatus());
        res.setSplitStatus(doObj.getSplitStatus());
        res.setNotifyStatus(doObj.getNotifyStatus());
        res.setNotifyResult(doObj.getNotifyResult());
        res.setRefundAmount(doObj.getRefundAmount());
        res.setRefundAmountFen(doObj.getRefundAmountFen());
        res.setRefundStatus(doObj.getRefundStatus());
        res.setRefundTime(doObj.getRefundTime());
        res.setCreateTime(doObj.getCreateTime());
        res.setUpdateTime(doObj.getUpdateTime());
        return res;
    }

    private ReconExceptionResult mapToExceptionResult(ReconExceptionDO doObj) {
        if (doObj == null)
            return null;
        ReconExceptionResult res = new ReconExceptionResult();
        res.setOrderNo(doObj.getOrderNo());
        res.setMerchantId(doObj.getMerchantId());
        res.setExceptionMsg(doObj.getExceptionMsg());
        res.setExceptionStep(doObj.getExceptionStep());
        res.setCreateTime(doObj.getCreateTime());
        res.setUpdateTime(doObj.getUpdateTime());
        return res;
    }

    private ReconSummaryResult mapToSummaryResult(ReconSummaryDO doObj) {
        if (doObj == null)
            return null;
        ReconSummaryResult res = new ReconSummaryResult();
        res.setSummaryDate(doObj.getSummaryDate());
        res.setTotalOrders(doObj.getTotalOrders());
        res.setSuccessCount(doObj.getSuccessCount());
        res.setFailCount(doObj.getFailCount());
        res.setInitCount(doObj.getInitCount());
        res.setTotalAmount(doObj.getTotalAmount());
        res.setTotalAmountFen(doObj.getTotalAmountFen());
        return res;
    }

    private ReconOrderSplitDetailResult mapToSplitDetailResult(ReconOrderSplitDetailDO doObj) {
        if (doObj == null)
            return null;
        ReconOrderSplitDetailResult res = new ReconOrderSplitDetailResult();
        res.setOrderNo(doObj.getOrderNo());
        res.setMerchantId(doObj.getMerchantId());
        res.setSplitAmount(doObj.getSplitAmount());
        res.setSplitAmountFen(doObj.getSplitAmountFen());
        res.setNotifyStatus(doObj.getNotifyStatus());
        res.setSettlementType(doObj.getSettlementType());
        res.setArrivalAmount(doObj.getArrivalAmount());
        res.setArrivalAmountFen(doObj.getArrivalAmountFen());
        res.setSplitFee(doObj.getSplitFee());
        res.setSplitFeeFen(doObj.getSplitFeeFen());
        res.setNotifyResult(doObj.getNotifyResult());
        res.setCreateTime(doObj.getCreateTime());
        res.setUpdateTime(doObj.getUpdateTime());
        return res;
    }

    private ReconOrderSubResult mapToOrderSubResult(ReconOrderSubDO doObj) {
        if (doObj == null)
            return null;
        ReconOrderSubResult res = new ReconOrderSubResult();
        res.setOrderNo(doObj.getOrderNo());
        res.setSubOrderNo(doObj.getSubOrderNo());
        res.setMerchantOrderNo(doObj.getMerchantOrderNo());
        res.setMerchantId(doObj.getMerchantId());
        res.setOrderAmount(doObj.getOrderAmount());
        res.setOrderAmountFen(doObj.getOrderAmountFen());
        res.setSplitAmount(doObj.getSplitAmount());
        res.setSplitAmountFen(doObj.getSplitAmountFen());
        res.setFee(doObj.getFee());
        res.setFeeFen(doObj.getFeeFen());
        res.setSplitRatio(doObj.getSplitRatio());
        res.setCreateTime(doObj.getCreateTime());
        res.setUpdateTime(doObj.getUpdateTime());
        return res;
    }

    private ReconOrderRefundDetailResult mapToRefundDetailResult(ReconOrderRefundDetailDO doObj) {
        if (doObj == null)
            return null;
        ReconOrderRefundDetailResult res = new ReconOrderRefundDetailResult();
        res.setOrderNo(doObj.getOrderNo());
        res.setMerchantId(doObj.getMerchantId());
        res.setRefundSplitAmount(doObj.getRefundSplitAmount());
        res.setRefundSplitAmountFen(doObj.getRefundSplitAmountFen());
        res.setCreateTime(doObj.getCreateTime());
        res.setUpdateTime(doObj.getUpdateTime());
        return res;
    }

    private ReconNotifyLogResult mapToNotifyLogResult(ReconNotifyLogDO doObj) {
        if (doObj == null)
            return null;
        ReconNotifyLogResult res = new ReconNotifyLogResult();
        res.setOrderNo(doObj.getOrderNo());
        res.setSubOrderNo(doObj.getSubOrderNo());
        res.setMerchantId(doObj.getMerchantId());
        res.setNotifyUrl(doObj.getNotifyUrl());
        res.setNotifyStatus(doObj.getNotifyStatus());
        res.setNotifyResult(doObj.getNotifyResult());
        res.setCreateTime(doObj.getCreateTime());
        res.setUpdateTime(doObj.getUpdateTime());
        return res;
    }

}
