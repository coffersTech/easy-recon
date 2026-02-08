package tech.coffers.recon.core.service;

import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.repository.ReconRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 实时对账服务
 * <p>
 * 处理实时对账请求，包括订单主记录和分账子记录的处理
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public class RealtimeReconService {

    private final ReconRepository reconRepository;
    private final ExecutorService executorService;
    private final AlarmService alarmService;

    public RealtimeReconService(ReconRepository reconRepository, ExecutorService executorService, AlarmService alarmService) {
        this.reconRepository = reconRepository;
        this.executorService = executorService;
        this.alarmService = alarmService;
    }

    /**
     * 执行实时对账
     *
     * @param orderMainDO   订单主记录
     * @param splitSubDOs   分账子记录列表
     * @return 对账结果
     */
    public boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs) {
        try {
            // 1. 保存订单主记录
            boolean mainSaved = reconRepository.saveOrderMain(orderMainDO);
            if (!mainSaved) {
                return false;
            }

            // 2. 批量保存分账子记录
            if (splitSubDOs != null && !splitSubDOs.isEmpty()) {
                boolean subSaved = reconRepository.batchSaveOrderSplitSub(splitSubDOs);
                if (!subSaved) {
                    return false;
                }
            }

            // 3. 更新对账状态为已对账
            reconRepository.updateReconStatus(orderMainDO.getOrderNo(), 1); // 1: 已对账

            return true;
        } catch (Exception e) {
            // 记录异常并告警
            alarmService.sendAlarm("实时对账失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 异步执行实时对账
     *
     * @param orderMainDO   订单主记录
     * @param splitSubDOs   分账子记录列表
     * @return 异步对账结果
     */
    public CompletableFuture<Boolean> doRealtimeReconAsync(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs) {
        return CompletableFuture.supplyAsync(() -> doRealtimeRecon(orderMainDO, splitSubDOs), executorService);
    }

}
