package tech.coffers.recon.core.service;

import lombok.extern.slf4j.Slf4j;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.SettlementTypeEnum;
import tech.coffers.recon.repository.ReconRepository;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.entity.ReconOrderSplitDetailDO;
import java.math.BigDecimal;
import java.util.List;

/**
 * 定时对账核心服务
 * <p>
 * 负责离线/定时对账任务的处理。主要用于：
 * 1. 补偿因各种原因未能及时完成实时对账的订单。
 * 2. 处理业务状态更新缓慢，需要周期性拉取结果并重新判定的场景。
 * 3. 产生最终产生对账汇总告警或日志。
 */
@Slf4j
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
     * 手动触发指定日期的定时对账
     * <p>
     * 逻辑说明：分批拉取数据库中 [未对账成功] 的订单，逐笔执行状态判定。
     *
     * @param dateStr 对账日期（yyyy-MM-dd）
     * @return 是否触发成功
     */
    public boolean doTimingRecon(String dateStr) {
        try {
            int offset = 0;
            int limit = properties.getBatchSize();
            int totalProcessed = 0;

            while (true) {
                // 1. 分页查询 [待核账] 订单
                List<ReconOrderMainDO> pendingOrders = reconRepository.getPendingReconOrders(dateStr, offset, limit);
                if (pendingOrders == null || pendingOrders.isEmpty()) {
                    break;
                }

                // 2. 依次处理每笔订单
                for (ReconOrderMainDO order : pendingOrders) {
                    processPendingOrder(order);
                    totalProcessed++;
                }

                offset += limit;
                if (pendingOrders.size() < limit) {
                    break;
                }
            }

            alarmService.sendAlarm("定时对账核账完成 [" + dateStr + "]，共处理 " + totalProcessed + " 笔记录");
            return true;
        } catch (Exception e) {
            alarmService.sendAlarm("定时对账运行异常 [" + dateStr + "]: " + e.getMessage());
            return false;
        }
    }

    /**
     * 对单笔待处理订单执行“核账判定”
     * <p>
     * 判定逻辑与 RealtimeReconService 核心规则一致：
     * 如果此时业务各侧（支付、分账、通知）都已成功，则更新状态为 SUCCESS。
     */
    private void processPendingOrder(ReconOrderMainDO order) {
        try {
            // 1. 获取事实明细
            List<ReconOrderSplitDetailDO> splitDetailDOs = reconRepository
                    .getOrderSplitDetailByOrderNo(order.getOrderNo());

            // 2. 检查业务状态：如果是处理中，则跳过本次定时处理
            if (PayStatusEnum.fromCode(order.getPayStatus()) == PayStatusEnum.PROCESSING ||
                    SplitStatusEnum.fromCode(order.getSplitStatus()) == SplitStatusEnum.PROCESSING ||
                    NotifyStatusEnum.fromCode(order.getNotifyStatus()) == NotifyStatusEnum.PROCESSING) {
                return;
            }

            // 3. 计算分账总金额 (事实)
            BigDecimal splitTotal = BigDecimal.ZERO;
            if (splitDetailDOs != null) {
                for (ReconOrderSplitDetailDO sub : splitDetailDOs) {
                    splitTotal = splitTotal.add(sub.getSplitAmount());
                }
            }

            // 4. 校验金额 (重新推断到账方式)
            SettlementTypeEnum settlementEnum = inferSettlementTypeFromFacts(order, splitDetailDOs);
            if (!validateAmountBySettlementType(order, splitDetailDOs, settlementEnum)) {
                // 金额校验失败，标记为 FAILURE 状态，等待人工介入
                recordException(order.getOrderNo(), "SELF", "定时对账失败：金额校验不平", 4);
                reconRepository.updateReconStatus(order.getOrderNo(), ReconStatusEnum.FAILURE);
                return;
            }

            // 5. 更新对账状态为已对账 (SUCCESS)
            reconRepository.updateReconStatus(order.getOrderNo(), ReconStatusEnum.SUCCESS);

        } catch (Exception e) {
            log.error("定时核账单笔处理异常, orderNo: {}", order.getOrderNo(), e);
            recordException(order.getOrderNo(), "SELF", "定时对账异常: " + e.getMessage(), 5);
        }
    }

    /**
     * 手动触发指定日期的定时退账核账补偿
     *
     * @param dateStr 业务日期
     * @return 是否成功
     */
    public boolean doTimingRefundRecon(String dateStr) {
        log.info("触发定时退账补偿逻辑, 日期: {}", dateStr);
        // FIXME: 实现退款场景的定时补偿逻辑
        return true;
    }

    private void recordException(String orderNo, String merchantId, String msg, int step) {
        exceptionRecordService.recordReconException(orderNo, merchantId, msg, step);
        alarmService.sendReconAlarm(orderNo, merchantId, msg);
    }

    private boolean validateAmountBySettlementType(ReconOrderMainDO order, List<ReconOrderSplitDetailDO> splitDetails,
            SettlementTypeEnum settlementType) {
        BigDecimal totalSplitFact = BigDecimal.ZERO;
        BigDecimal totalArrival = BigDecimal.ZERO;
        BigDecimal totalSubFee = BigDecimal.ZERO;

        if (splitDetails != null) {
            for (ReconOrderSplitDetailDO sub : splitDetails) {
                if (sub.getSplitAmount() != null) {
                    totalSplitFact = totalSplitFact.add(sub.getSplitAmount());
                }
                if (sub.getArrivalAmount() != null) {
                    totalArrival = totalArrival.add(sub.getArrivalAmount());
                }
                if (sub.getSplitFee() != null) {
                    totalSubFee = totalSubFee.add(sub.getSplitFee());
                }
            }
        }

        BigDecimal tolerance = properties.getAmountTolerance();

        switch (settlementType) {
            case PLATFORM_COLLECTION:
                BigDecimal calc1 = totalSplitFact.add(order.getPlatformIncome()).add(order.getPayFee());
                return order.getPayAmount().subtract(calc1).abs().compareTo(tolerance) <= 0;
            case DIRECT_TO_MERCHANT:
                BigDecimal calc2 = totalArrival.add(totalSubFee).add(order.getPayFee());
                return order.getPayAmount().subtract(calc2).abs().compareTo(tolerance) <= 0;
            case REALTIME_SPLIT:
                BigDecimal calc3 = totalArrival.add(totalSubFee).add(order.getPlatformIncome()).add(order.getPayFee());
                return order.getPayAmount().subtract(calc3).abs().compareTo(tolerance) <= 0;
            default:
                return false;
        }
    }

    private SettlementTypeEnum inferSettlementTypeFromFacts(ReconOrderMainDO main,
            List<ReconOrderSplitDetailDO> facts) {
        if (facts == null || facts.isEmpty()) {
            return SettlementTypeEnum.PLATFORM_COLLECTION;
        }

        java.util.Set<String> factMerchants = new java.util.HashSet<>();
        long totalArrivalFen = 0L;
        for (ReconOrderSplitDetailDO fact : facts) {
            if (fact.getMerchantId() != null) {
                factMerchants.add(fact.getMerchantId());
            }
            if (fact.getArrivalAmountFen() != null) {
                totalArrivalFen += fact.getArrivalAmountFen();
            }
        }

        if (factMerchants.size() > 1) {
            return SettlementTypeEnum.REALTIME_SPLIT;
        }

        if (totalArrivalFen > 0) {
            if (main.getPlatformIncomeFen() != null && main.getPlatformIncomeFen() > 0) {
                return SettlementTypeEnum.REALTIME_SPLIT;
            }
            return SettlementTypeEnum.DIRECT_TO_MERCHANT;
        }

        return SettlementTypeEnum.PLATFORM_COLLECTION;
    }
}
