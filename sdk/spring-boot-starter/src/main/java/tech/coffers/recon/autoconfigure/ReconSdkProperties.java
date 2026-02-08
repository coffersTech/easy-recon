package tech.coffers.recon.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Easy Recon SDK 配置属性
 * <p>
 * 用于读取和管理 SDK 的配置信息，支持在 application.yml 中配置
 * </p>
 *
 * @author Ryan
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
    private double amountTolerance = 0.01;

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
