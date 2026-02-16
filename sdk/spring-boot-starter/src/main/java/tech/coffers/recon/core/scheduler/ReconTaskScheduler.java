package tech.coffers.recon.core.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import tech.coffers.recon.core.service.TimingReconService;
import java.time.LocalDate;

/**
 * 定时对账任务调度器
 * <p>
 * 专门负责触发定时对账任务，与核心业务逻辑分离。
 * 可通过配置 easy-recon.timing.enabled=false 关闭调度，但保留 API 能力。
 *
 * @author coffersTech
 * @since 1.2.0
 */
public class ReconTaskScheduler {

    private final TimingReconService timingReconService;

    public ReconTaskScheduler(TimingReconService timingReconService) {
        this.timingReconService = timingReconService;
    }

    @Scheduled(cron = "${easy-recon.timing-cron}")
    public void scheduledTimingRecon() {
        // 默认核对昨天的账单
        timingReconService.doTimingRecon(LocalDate.now().minusDays(1).toString());
    }
}
