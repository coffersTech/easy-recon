package tech.coffers.recon.api;

import tech.coffers.recon.api.result.ReconResult;
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
    public boolean reconRefund(String orderNo, BigDecimal refundAmount, LocalDateTime refundTime,
            int refundStatus, Map<String, BigDecimal> splitDetails) {
        ReconResult result = realtimeReconService.reconRefund(orderNo, refundAmount,
                refundTime, refundStatus, splitDetails);
        return result.isSuccess();
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
    public CompletableFuture<Boolean> reconRefundAsync(String orderNo, BigDecimal refundAmount,
            LocalDateTime refundTime, int refundStatus,
            Map<String, BigDecimal> splitDetails) {
        return realtimeReconService
                .reconRefundAsync(orderNo, refundAmount, refundTime, refundStatus, splitDetails)
                .thenApply(ReconResult::isSuccess);
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
    public Integer getReconStatus(String orderNo) {
        return reconRepository.getReconStatus(orderNo);
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
     * 手动重试对账
     *
     * @param orderNo 订单号
     * @return 重试结果
     */
    public boolean retryRecon(String orderNo) {
        return realtimeReconService.retryRecon(orderNo);
    }

}
