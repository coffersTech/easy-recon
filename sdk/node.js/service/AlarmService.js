/**
 * 告警服务
 */
class AlarmService {
  /**
   * 构造函数
   * @param {Object} strategy - 告警策略
   */
  constructor(strategy) {
    this.strategy = strategy;
  }

  /**
   * 发送告警
   * @param {string} message - 告警消息
   */
  sendAlarm(message) {
    this.strategy.sendAlarm(message);
  }
}

/**
 * 告警策略接口
 */
class AlarmStrategy {
  /**
   * 发送告警
   * @param {string} message - 告警消息
   */
  sendAlarm(message) {
    throw new Error('子类必须实现sendAlarm方法');
  }
}

/**
 * 日志告警策略
 */
class LogAlarmStrategy extends AlarmStrategy {
  /**
   * 发送告警
   * @param {string} message - 告警消息
   */
  sendAlarm(message) {
    // 实现日志告警逻辑
    console.log(`[Log] 告警: ${message}`);
  }
}

/**
 * 钉钉告警策略
 */
class DingTalkAlarmStrategy extends AlarmStrategy {
  /**
   * 构造函数
   * @param {string} webhookUrl - 钉钉告警机器人Webhook
   */
  constructor(webhookUrl) {
    super();
    this.webhookUrl = webhookUrl;
  }

  /**
   * 发送告警
   * @param {string} message - 告警消息
   */
  sendAlarm(message) {
    // 实现钉钉告警逻辑
    console.log(`[DingTalk] 发送告警: ${message}`);
    // 实际实现中需要调用钉钉机器人API
  }
}

module.exports = {
  AlarmService,
  AlarmStrategy,
  LogAlarmStrategy,
  DingTalkAlarmStrategy
};
