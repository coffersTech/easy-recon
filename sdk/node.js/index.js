/**
 * Easy Recon SDK for Node.js
 */
const ReconConfig = require('./config/ReconConfig');
const SQLReconRepository = require('./repository/SQLReconRepository');
const { createDialect } = require('./dialect/ReconDatabaseDialect');
const RealtimeReconService = require('./service/RealtimeReconService');
const TimingReconService = require('./service/TimingReconService');
const { AlarmService, LogAlarmStrategy, DingTalkAlarmStrategy } = require('./service/AlarmService');

// Entities
const ReconOrderMain = require('./entity/ReconOrderMain');
const ReconOrderSplitSub = require('./entity/ReconOrderSplitSub');
const ReconOrderRefundSplitSub = require('./entity/ReconOrderRefundSplitSub');
const ReconNotifyLog = require('./entity/ReconNotifyLog');
const ReconSummary = require('./entity/ReconSummary');
const ReconException = require('./entity/ReconException');

class EasyReconApi {
    /**
     * 初始化 SDK
     * @param {Object|ReconConfig} configOrOption - 配置对象
     * @param {Object} connection - 数据库连接 (mysql2 connection/pool or pg client)
     */
    constructor(configOrOption, connection) {
        this.config = configOrOption instanceof ReconConfig ? configOrOption : new ReconConfig(configOrOption);

        if (!connection && !this.config.database) {
            throw new Error('Database connection is required');
        }
        this.connection = connection; // User provided connection

        // Initialize Dialect
        this.dialect = createDialect(this.connection);

        // Initialize Repository
        this.repository = new SQLReconRepository(this.connection, this.dialect);

        // Initialize Alarm
        let alarmStrategy = new LogAlarmStrategy();
        if (this.config.alarm && this.config.alarm.type === 'dingtalk' && this.config.alarm.dingtalk.webhookUrl) {
            alarmStrategy = new DingTalkAlarmStrategy(this.config.alarm.dingtalk.webhookUrl);
        }
        this.alarmService = new AlarmService(alarmStrategy);

        // Initialize Services
        this.realtimeReconService = new RealtimeReconService(this.repository, this.alarmService, this.config);
        this.timingReconService = new TimingReconService(this.repository, this.alarmService, this.config);

        // Helper for direct repository access if needed
        this.repo = this.repository;
    }

    /**
     * 实时对账
     */
    async reconOrder(orderMain, splitSubs) {
        if (!this.config.enabled) return true;
        return this.realtimeReconService.reconOrder(orderMain, splitSubs);
    }

    /**
     * 退款对账
     */
    async reconRefund(orderNo, refundAmount, refundApps) {
        if (!this.config.enabled) return true;
        return this.realtimeReconService.reconRefund(orderNo, refundAmount, refundApps);
    }

    /**
     * 通知处理
     */
    async reconNotify(notifyLog) {
        if (!this.config.enabled) return true;
        return this.realtimeReconService.reconNotify(notifyLog);
    }

    /**
     * 定时对账 (手动触发)
     */
    async runTimingRecon(dateStr) {
        if (!this.config.enabled) return true;
        return this.timingReconService.doTimingRecon(dateStr);
    }

    /**
     * 获取建表 SQL (Utility)
     */
    getCreateTableSQL() {
        return this.dialect.getCreateTableSQL();
    }
}

module.exports = {
    EasyReconApi,
    ReconConfig,
    ReconOrderMain,
    ReconOrderSplitSub,
    ReconOrderRefundSplitSub,
    ReconNotifyLog,
    ReconSummary,
    ReconException
};
