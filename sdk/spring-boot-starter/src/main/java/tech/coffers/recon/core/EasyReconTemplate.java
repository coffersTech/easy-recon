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
     * @param orderMainDO   订单主记录
     * @param splitSubDOs   分账子记录列表
     * @return 对账结果
     */
    public boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs) {
        return realtimeReconService.doRealtimeRecon(orderMainDO, splitSubDOs);
    }

    /**
     * 异步执行实时对账
     *
     * @param orderMainDO   订单主记录
     * @param splitSubDOs   分账子记录列表
     * @return 异步对账结果
     */
    public CompletableFuture<Boolean> doRealtimeReconAsync(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs) {
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

}
