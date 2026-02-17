const https = require('https');
const url = require('url');

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
  async sendAlarm(message) {
    if (this.strategy) {
      await this.strategy.sendAlarm(message);
    }
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
  async sendAlarm(message) {
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
  async sendAlarm(message) {
    console.log(`[Recon Alarm] ${message}`);
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
  async sendAlarm(message) {
    if (!this.webhookUrl) return;

    const payload = JSON.stringify({
      msgtype: 'text',
      text: {
        content: `[EasyRecon Alarm] ${message}`
      }
    });

    const parsedUrl = url.parse(this.webhookUrl);
    const options = {
      hostname: parsedUrl.hostname,
      path: parsedUrl.path,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(payload)
      }
    };

    return new Promise((resolve, reject) => {
      const req = https.request(options, (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve();
        } else {
          // consume response data to free up memory
          res.resume();
          console.error(`DingTalk Alarm Failed: Status Code ${res.statusCode}`);
          resolve(); // Don't crash on alarm fail
        }
      });

      req.on('error', (e) => {
        console.error(`DingTalk Alarm Error: ${e.message}`);
        resolve(); // Don't crash
      });

      req.write(payload);
      req.end();
    });
  }
}

module.exports = {
  AlarmService,
  AlarmStrategy,
  LogAlarmStrategy,
  DingTalkAlarmStrategy
};
