package tech.coffers.recon.core;

import tech.coffers.recon.core.service.RealtimeReconService;
import tech.coffers.recon.core.service.TimingReconService;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 对账SDK核心模板类
 * <p>
 * 提供对账SDK的主要入口方法，包括实时对账、定时对账等功能
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public class EasyReconTemplate {

    private final RealtimeReconService realtimeReconService;
    private final TimingReconService timingReconService;

    public EasyReconTemplate(RealtimeReconService realtimeReconService, TimingReconService timingReconService) {
        this.realtimeReconService = realtimeReconService;
        this.timingReconService = timingReconService;
    }

    /**
     * 执行实时对账
     *
     * @param orderMainDO 订单主记录
     * @param splitSubDOs 分账子记录列表
     * @return 对账结果
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
     * 执行退款对账
     *
     * @param orderNo      订单号
     * @param refundAmount 退款金额
     * @param refundTime   退款时间
     * @param refundStatus 退款状态
     * @param splitDetails 退款分账详情
     * @return 对账结果
     */
    public boolean reconRefund(String orderNo, java.math.BigDecimal refundAmount, java.time.LocalDateTime refundTime,
            int refundStatus, java.util.Map<String, java.math.BigDecimal> splitDetails) {
        tech.coffers.recon.api.result.ReconResult result = realtimeReconService.reconRefund(orderNo, refundAmount,
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
    public CompletableFuture<Boolean> reconRefundAsync(String orderNo, java.math.BigDecimal refundAmount,
            java.time.LocalDateTime refundTime, int refundStatus,
            java.util.Map<String, java.math.BigDecimal> splitDetails) {
        // 使用 supplyAsync 的回调链将 ReconResult 转换为 Boolean
        return realtimeReconService
                .reconRefundAsync(orderNo, refundAmount, refundTime, refundStatus, splitDetails)
                .thenApply(tech.coffers.recon.api.result.ReconResult::isSuccess);
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

}
