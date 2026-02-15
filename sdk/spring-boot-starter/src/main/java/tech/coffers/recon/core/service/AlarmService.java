package tech.coffers.recon.core.service;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

/**
 * 告警服务
 * <p>
 * 提供异常告警功能，支持多种告警方式
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Slf4j
public class AlarmService {

    private final List<AlarmStrategy> alarmStrategies;

    public AlarmService(List<AlarmStrategy> alarmStrategies) {
        this.alarmStrategies = alarmStrategies != null ? alarmStrategies : new ArrayList<>();
    }

    /**
     * 发送告警
     *
     * @param message 告警消息
     */
    public void sendAlarm(String message) {
        if (alarmStrategies.isEmpty()) {
            log.warn("未配置告警策略，消息将被忽略: {}", message);
            return;
        }
        for (AlarmStrategy strategy : alarmStrategies) {
            try {
                strategy.sendAlarm(message);
            } catch (Exception e) {
                log.error("告警发送失败, strategy: {}", strategy.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 发送对账告警
     *
     * @param orderNo    订单号
     * @param merchantId 商户ID
     * @param message    告警消息
     */
    public void sendReconAlarm(String orderNo, String merchantId, String message) {
        String fullMessage = String.format("【对账告警】订单号：%s，商户ID：%s，信息：%s", orderNo, merchantId, message);
        sendAlarm(fullMessage);
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
            // TODO: 实际生产中应使用 RestTemplate 或其他 HTTP 客户端发送钉钉 Webhook
            log.info("[DingTalk] 发送告警到 {}: {}", webhookUrl, message);
        }
    }

    /**
     * 日志告警策略
     */
    public static class LogAlarmStrategy implements AlarmStrategy {
        @Override
        public void sendAlarm(String message) {
            log.info("[Log] 告警: {}", message);
        }
    }

}
