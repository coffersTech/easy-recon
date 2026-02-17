/**
 * Easy Recon SDK 配置类
 */
const Decimal = require('decimal.js');

class ReconConfig {
    constructor(options = {}) {
        this.enabled = options.enabled !== undefined ? options.enabled : true;
        this.amountTolerance = new Decimal(options.amountTolerance || '0.01');
        this.batchSize = options.batchSize || 1000;
        this.timingCron = options.timingCron || '0 0 2 * * *'; // Default to 2 AM daily

        this.timing = {
            enabled: options.timing?.enabled !== undefined ? options.timing.enabled : false
        };

        // Node.js is single-threaded, threadPool config is kept for structure parity/future use if worker threads are added
        this.threadPool = {
            corePoolSize: options.threadPool?.corePoolSize || 10,
            maxPoolSize: options.threadPool?.maxPoolSize || 20,
            queueCapacity: options.threadPool?.queueCapacity || 1000
        };

        this.alarm = {
            type: options.alarm?.type || 'log',
            dingtalk: {
                webhookUrl: options.alarm?.dingtalk?.webhookUrl || process.env.DINGTALK_WEBHOOK_URL
            }
        };

        // Load database config if provided, otherwise expect connection passed to Repository
        this.database = options.database || {};
    }
}

module.exports = ReconConfig;
