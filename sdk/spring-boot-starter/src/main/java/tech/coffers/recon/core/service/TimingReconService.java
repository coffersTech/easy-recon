package tech.coffers.recon.core.service;

import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.repository.ReconRepository;

import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import java.math.BigDecimal;
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
    private final ExceptionRecordService exceptionRecordService;
    private final ReconSdkProperties properties;

    public TimingReconService(ReconRepository reconRepository, ExceptionRecordService exceptionRecordService,
            AlarmService alarmService, ReconSdkProperties properties) {
        this.reconRepository = reconRepository;
        this.exceptionRecordService = exceptionRecordService;
        this.alarmService = alarmService;
        this.properties = properties;
    }

    /**
     * 执行定时对账
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "${easy-recon.timing-cron}")
    public void scheduledTimingRecon() {
        if (!properties.getTiming().isEnabled()) {
            return;
        }
        doTimingRecon(java.time.LocalDate.now().minusDays(1).toString());
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
            int limit = properties.getBatchSize();
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
                if (pendingOrders.size() < limit) {
                    break;
                }
            }

            alarmService.sendAlarm("定时对账完成 [" + dateStr + "]，共处理 " + totalProcessed + " 笔订单");
            return true;
        } catch (Exception e) {
            alarmService.sendAlarm("定时对账失败 [" + dateStr + "]: " + e.getMessage());
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
            // 1. 获取分账子记录
            List<ReconOrderSplitSubDO> splitSubDOs = reconRepository.getOrderSplitSubByOrderNo(order.getOrderNo());

            // 2. 检查业务状态：如果是处理中，则跳过本次定时处理
            if (PayStatusEnum.fromCode(order.getPayStatus()) == PayStatusEnum.PROCESSING ||
                    SplitStatusEnum.fromCode(order.getSplitStatus()) == SplitStatusEnum.PROCESSING ||
                    NotifyStatusEnum.fromCode(order.getNotifyStatus()) == NotifyStatusEnum.PROCESSING) {
                return;
            }

            // 3. 计算分账总额
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitSubDOs != null) {
                for (ReconOrderSplitSubDO sub : splitSubDOs) {
                    splitTotal = splitTotal.add(sub.getSplitAmount());
                }
            }

            // 4. 校验金额 (实付金额 = 平台收入 + 支付手续费 + 分账总额)
            BigDecimal calcAmount = splitTotal.add(order.getPlatformIncome()).add(order.getPayFee());

            if (order.getPayAmount().subtract(calcAmount).abs().compareTo(properties.getAmountTolerance()) > 0) {
                // 金额校验失败
                recordException(order.getOrderNo(), "SELF", "定时对账失败：金额校验不一致", 4);
                reconRepository.updateReconStatus(order.getOrderNo(), ReconStatusEnum.FAILURE);
                return;
            }

            // 5. 更新对账状态为已对账
            reconRepository.updateReconStatus(order.getOrderNo(), ReconStatusEnum.SUCCESS);

        } catch (Exception e) {
            recordException(order.getOrderNo(), "SELF", "定时对账异常: " + e.getMessage(), 5);
        }
    }

    private void recordException(String orderNo, String merchantId, String msg, int step) {
        exceptionRecordService.recordReconException(orderNo, merchantId, msg, step);
        alarmService.sendReconAlarm(orderNo, merchantId, msg);
    }

    /**
     * 执行定时退款对账
     *
     * @param dateStr 对账日期（yyyy-MM-dd）
     * @return 对账结果
     */
    public boolean doTimingRefundRecon(String dateStr) {
        // TODO: 实现定时退款对账逻辑
        // 1. 查询当日发生的退款订单
        // 2. 对于每笔退款，校验金额和分账
        // 3. 更新退款对账状态
        // 目前简化处理，仅作为占位符，后续根据实际业务需求完善
        return true;
    }

}
