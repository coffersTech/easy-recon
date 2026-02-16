package tech.coffers.recon.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.math.BigDecimal;

/**
 * Easy Recon SDK 核心配置属性类
 * <p>
 * 该类映射以 "easy-recon" 为前缀的配置项。主要包含：
 * <ul>
 * <li>SDK 开关与核账精度控制 (amount-tolerance)</li>
 * <li>数据库访问配置 (table-prefix)</li>
 * <li>定时对账任务调度频率 (timing-cron, batch-size)</li>
 * <li>核心异步处理线程池配置 (thread-pool)</li>
 * <li>多种业务异常告警策略配置 (alarm)</li>
 * </ul>
 *
 * @author coffersTech
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "easy-recon")
public class ReconSdkProperties {

    /**
     * 是否启用 SDK
     */
    private boolean enabled = true;

    /**
     * 金额容差（默认 0.01）
     */
    private BigDecimal amountTolerance = new BigDecimal("0.01");

    /**
     * 定时核账批次大小（默认 1000）
     */
    private int batchSize = 1000;

    /**
     * 定时核账 cron 表达式（默认每天凌晨 2 点）
     */
    private String timingCron = "0 0 2 * * ?";

    /**
     * 数据库表前缀（默认 easy_recon_）
     */
    private String tablePrefix = "easy_recon_";

    /**
     * 定时对账配置
     */
    private Timing timing = new Timing();

    @Data
    public static class Timing {
        /**
         * 是否启用定时核账调度
         */
        private boolean enabled = true;
    }

    /**
     * 配置解密密钥（用于解密 encrypted: 前缀的配置）
     */
    private String configSecretKey;

    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * 告警配置
     */
    private Alarm alarm = new Alarm();

    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPool {
        /**
         * 线程池核心线程数（默认 10）
         */
        private int corePoolSize = 10;

        /**
         * 线程池最大线程数（默认 20）
         */
        private int maxPoolSize = 20;

        /**
         * 线程池队列容量（默认 1000）
         */
        private int queueCapacity = 1000;
    }

    /**
     * 告警配置
     */
    @Data
    public static class Alarm {
        /**
         * 告警类型（log 或 dingtalk）
         */
        private String type = "log";

        /**
         * 钉钉告警配置
         */
        private DingTalk dingtalk = new DingTalk();

        /**
         * 钉钉告警配置
         */
        @Data
        public static class DingTalk {
            /**
             * 钉钉告警机器人 Webhook
             */
            private String webhookUrl;
        }
    }

}
