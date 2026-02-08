package tech.coffers.recon.core.service;

/**
 * 告警服务
 * <p>
 * 提供异常告警功能，支持多种告警方式
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public class AlarmService {

    private final AlarmStrategy alarmStrategy;

    public AlarmService(AlarmStrategy alarmStrategy) {
        this.alarmStrategy = alarmStrategy;
    }

    /**
     * 发送告警
     *
     * @param message 告警消息
     */
    public void sendAlarm(String message) {
        alarmStrategy.sendAlarm(message);
    }

    /**
     * 告警策略接口
     */
    public interface AlarmStrategy {
        /**
         * 发送告警
         *
         * @param message 告警消息
         */
        void sendAlarm(String message);
    }

    /**
     * 钉钉告警策略
     */
    public static class DingTalkAlarmStrategy implements AlarmStrategy {
        private final String webhookUrl;

        public DingTalkAlarmStrategy(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        @Override
        public void sendAlarm(String message) {
            // 实现钉钉告警逻辑
            System.out.println("[DingTalk] 发送告警: " + message);
            // 实际实现中需要调用钉钉机器人 API
        }
    }

    /**
     * 日志告警策略
     */
    public static class LogAlarmStrategy implements AlarmStrategy {
        @Override
        public void sendAlarm(String message) {
            // 实现日志告警逻辑
            System.out.println("[Log] 告警: " + message);
        }
    }

}
