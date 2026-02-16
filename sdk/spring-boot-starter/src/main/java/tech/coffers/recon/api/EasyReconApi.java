package tech.coffers.recon.api;

import tech.coffers.recon.api.result.PageResult;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.core.service.RealtimeReconService;
import tech.coffers.recon.core.service.TimingReconService;
import tech.coffers.recon.entity.ReconExceptionDO;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.entity.ReconSummaryDO;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    public ReconResult reconOrder(String orderNo, BigDecimal payAmount, BigDecimal platformIncome,
            BigDecimal payFee, List<ReconOrderSplitSubDO> splitDetails, PayStatusEnum payStatus,
            SplitStatusEnum splitStatus, NotifyStatusEnum notifyStatus) {
        return realtimeReconService.reconOrder(orderNo, payAmount, platformIncome, payFee, splitDetails,
                payStatus, splitStatus, notifyStatus);
    }

    /**
     * 异步对账订单
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
    public CompletableFuture<ReconResult> reconOrderAsync(String orderNo, BigDecimal payAmount,
            BigDecimal platformIncome,
            BigDecimal payFee, List<ReconOrderSplitSubDO> splitDetails, PayStatusEnum payStatus,
            SplitStatusEnum splitStatus, NotifyStatusEnum notifyStatus) {
        return realtimeReconService.reconOrderAsync(orderNo, payAmount, platformIncome, payFee, splitDetails,
                payStatus, splitStatus, notifyStatus);
    }

    /**
     * 对账通知回调
     *
     * @param orderNo      订单号
     * @param merchantId   商户号
     * @param notifyUrl    通知地址
     * @param notifyStatus 通知状态
     * @param notifyResult 通知返回结果
     * @return 对账结果
     */
    public ReconResult reconNotify(String orderNo, String merchantId, String notifyUrl,
            NotifyStatusEnum notifyStatus, String notifyResult) {
        return realtimeReconService.reconNotify(orderNo, merchantId, notifyUrl, notifyStatus, notifyResult);
    }

    /**
     * 对账通知回调 (带子订单号)
     */
    public ReconResult reconNotify(String orderNo, String merchantId, String subOrderNo, String notifyUrl,
            NotifyStatusEnum notifyStatus, String notifyResult) {
        return realtimeReconService.reconNotify(orderNo, merchantId, subOrderNo, notifyUrl, notifyStatus, notifyResult);
    }

    /**
     * 异步对账通知回调
     */
    public CompletableFuture<ReconResult> reconNotifyAsync(String orderNo, String merchantId, String notifyUrl,
            NotifyStatusEnum notifyStatus, String notifyResult) {
        return realtimeReconService.reconNotifyAsync(orderNo, merchantId, notifyUrl, notifyStatus, notifyResult);
    }

    /**
     * 异步对账通知回调 (带子订单号)
     */
    public CompletableFuture<ReconResult> reconNotifyAsync(String orderNo, String merchantId, String subOrderNo,
            String notifyUrl, NotifyStatusEnum notifyStatus, String notifyResult) {
        return realtimeReconService.reconNotifyAsync(orderNo, merchantId, subOrderNo, notifyUrl, notifyStatus,
                notifyResult);
    }

    /**
     * 执行实时对账
     *
     * @param orderMainDO 订单主记录
     * @param splitSubDOs 分账子记录列表
     * @return 对账结果 (true: 成功, false: 失败)
     */
    public boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs) {
        return realtimeReconService.doRealtimeRecon(orderMainDO, splitSubDOs);
    }

    /**
     * 异步执行实时对账
     *
     * @param orderMainDO 订单主记录
     * @param splitSubDOs 分账子记录列表
     * @return 异步对账结果
     */
    public CompletableFuture<Boolean> doRealtimeReconAsync(ReconOrderMainDO orderMainDO,
            List<ReconOrderSplitSubDO> splitSubDOs) {
        return realtimeReconService.doRealtimeReconAsync(orderMainDO, splitSubDOs);
    }

    // ==================== 退款对账 ====================

    /**
     * 执行退款对账
     *
     * @param orderNo      订单号
     * @param refundAmount 退款金额
     * @param refundTime   退款时间
     * @param refundStatus 退款状态
     * @param splitDetails 退款分账详情
     * @return 对账结果 (true: 成功, false: 失败)
     */
    public ReconResult reconRefund(String orderNo, BigDecimal refundAmount, LocalDateTime refundTime,
            RefundStatusEnum refundStatus, Map<String, BigDecimal> splitDetails) {
        return realtimeReconService.reconRefund(orderNo, refundAmount,
                refundTime, refundStatus, splitDetails);
    }

    /**
     * 异步执行退款对账
     *
     * @param orderNo      订单号
     * @param refundAmount 退款金额
     * @param refundTime   退款时间
     * @param refundStatus 退款状态
     * @param splitDetails 退款分账详情
     * @return 异步对账结果
     */
    public CompletableFuture<ReconResult> reconRefundAsync(String orderNo, BigDecimal refundAmount,
            LocalDateTime refundTime, RefundStatusEnum refundStatus,
            Map<String, BigDecimal> splitDetails) {
        return realtimeReconService
                .reconRefundAsync(orderNo, refundAmount, refundTime, refundStatus, splitDetails);
    }

    // ==================== 定时对账触发 ====================

    /**
     * 执行定时对账
     *
     * @param dateStr 对账日期（yyyy-MM-dd）
     * @return 对账结果
     */
    public boolean doTimingRecon(String dateStr) {
        return timingReconService.doTimingRecon(dateStr);
    }

    /**
     * 执行定时退款对账
     *
     * @param dateStr 对账日期（yyyy-MM-dd）
     * @return 对账结果
     */
    public boolean doTimingRefundRecon(String dateStr) {
        return timingReconService.doTimingRefundRecon(dateStr);
    }

    // ==================== 查询能力 ====================

    /**
     * 查询对账状态
     *
     * @param orderNo 订单号
     * @return 对账状态 (可能为 null)
     */
    public ReconStatusEnum getReconStatus(String orderNo) {
        Integer code = reconRepository.getReconStatus(orderNo);
        return ReconStatusEnum.fromCode(code);
    }

    /**
     * 查询订单主记录
     *
     * @param orderNo 订单号
     * @return 订单主记录
     */
    public ReconOrderMainDO getOrderMain(String orderNo) {
        return reconRepository.getOrderMainByOrderNo(orderNo);
    }

    /**
     * 查询对账异常历史
     *
     * @param orderNo 订单号
     * @return 异常记录列表
     */
    public List<ReconExceptionDO> getReconExceptions(String orderNo) {
        return reconRepository.getExceptionsByOrderNo(orderNo);
    }

    /**
     * 获取对账统计数据
     *
     * @param dateStr 日期 (yyyy-MM-dd)
     * @return 统计数据
     */
    public ReconSummaryDO getReconSummary(String dateStr) {
        return reconRepository.getReconSummary(dateStr);
    }

    /**
     * 分页查询日期对账订单
     *
     * @param dateStr     日期 (yyyy-MM-dd)
     * @param reconStatus 对账状态
     * @param page        页码 (1-based)
     * @param size        每页大小
     * @return 分页结果
     */
    public PageResult<ReconOrderMainDO> listOrdersByDate(String dateStr, ReconStatusEnum reconStatus, int page,
            int size) {
        int offset = (page - 1) * size;
        List<ReconOrderMainDO> list = reconRepository.getOrderMainByDate(dateStr, reconStatus, offset, size);
        long total = reconRepository.countOrderMainByDate(dateStr, reconStatus);
        return PageResult.of(list, total, page, size);
    }

    /**
     * 分页查询异常记录
     *
     * @param merchantId    商户ID
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param exceptionStep 异常步骤
     * @param page          页码 (1-based)
     * @param size          每页大小
     * @return 分页结果
     */
    public PageResult<ReconExceptionDO> listExceptions(String merchantId, String startDate, String endDate,
            Integer exceptionStep, int page, int size) {
        int offset = (page - 1) * size;
        List<ReconExceptionDO> list = reconRepository.getExceptionRecords(merchantId, startDate, endDate, exceptionStep,
                offset, size);
        long total = reconRepository.countExceptionRecords(merchantId, startDate, endDate, exceptionStep);
        return PageResult.of(list, total, page, size);
    }

}
