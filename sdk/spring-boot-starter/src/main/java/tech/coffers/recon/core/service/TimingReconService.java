package tech.coffers.recon.core.service;

import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.repository.ReconRepository;

import java.util.List;

/**
 * 定时对账服务
 * <p>
 * 处理定时核账任务，对未对账的订单进行批量处理
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public class TimingReconService {

    private final ReconRepository reconRepository;
    private final AlarmService alarmService;

    public TimingReconService(ReconRepository reconRepository, AlarmService alarmService) {
        this.reconRepository = reconRepository;
        this.alarmService = alarmService;
    }

    /**
     * 执行定时对账
     *
     * @param dateStr 对账日期（yyyy-MM-dd）
     * @return 对账结果
     */
    public boolean doTimingRecon(String dateStr) {
        try {
            int offset = 0;
            int limit = 100;
            int totalProcessed = 0;

            while (true) {
                // 查询待核账订单
                List<ReconOrderMainDO> pendingOrders = reconRepository.getPendingReconOrders(dateStr, offset, limit);
                if (pendingOrders == null || pendingOrders.isEmpty()) {
                    break;
                }

                // 处理每个待核账订单
                for (ReconOrderMainDO order : pendingOrders) {
                    processPendingOrder(order);
                    totalProcessed++;
                }

                offset += limit;
            }

            alarmService.sendAlarm("定时对账完成，共处理 " + totalProcessed + " 笔订单");
            return true;
        } catch (Exception e) {
            alarmService.sendAlarm("定时对账失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 处理待核账订单
     *
     * @param order 待核账订单
     */
    private void processPendingOrder(ReconOrderMainDO order) {
        try {
            // 这里可以添加更复杂的对账逻辑
            // 例如：与第三方支付平台对账、金额校验等

            // 更新对账状态为已对账
            reconRepository.updateReconStatus(order.getOrderNo(), 1); // 1: 已对账
        } catch (Exception e) {
            alarmService.sendAlarm("处理订单 " + order.getOrderNo() + " 失败: " + e.getMessage());
        }
    }

}
