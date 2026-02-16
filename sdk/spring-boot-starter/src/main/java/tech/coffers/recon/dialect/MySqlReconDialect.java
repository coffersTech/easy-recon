package tech.coffers.recon.dialect;

/**
 * MySQL 数据库方言实现
 * <p>
 * 为 MySQL 数据库生成 SQL 语句
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public class MySqlReconDialect implements ReconDatabaseDialect {

    /**
     * 生成主订单插入或更新的 SQL 语句 (MySQL)
     * 使用 ON DUPLICATE KEY UPDATE 实现幂等录入
     */
    @Override
    public String getInsertOrderMainSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, pay_amount, pay_amount_fen, platform_income, platform_income_fen, pay_fee, pay_fee_fen, split_total_amount, split_total_amount_fen, pay_status, split_status, notify_status, notify_result, recon_status, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "pay_amount = VALUES(pay_amount), pay_amount_fen = VALUES(pay_amount_fen), "
                + "platform_income = VALUES(platform_income), platform_income_fen = VALUES(platform_income_fen), "
                + "pay_fee = VALUES(pay_fee), pay_fee_fen = VALUES(pay_fee_fen), "
                + "split_total_amount = VALUES(split_total_amount), split_total_amount_fen = VALUES(split_total_amount_fen), "
                + "pay_status = VALUES(pay_status), split_status = VALUES(split_status), "
                + "notify_status = VALUES(notify_status), notify_result = VALUES(notify_result), "
                + "recon_status = VALUES(recon_status), update_time = VALUES(update_time)";
    }

    /**
     * 生成分账项插入或更新的 SQL 语句 (MySQL)
     */
    @Override
    public String getInsertOrderSplitSubSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, sub_order_no, merchant_id, split_amount, split_amount_fen, notify_status, notify_result, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "sub_order_no = VALUES(sub_order_no), "
                + "split_amount = VALUES(split_amount), split_amount_fen = VALUES(split_amount_fen), "
                + "notify_status = VALUES(notify_status), notify_result = VALUES(notify_result), "
                + "update_time = VALUES(update_time)";
    }

    /**
     * 生成异常信息记录的 SQL 语句
     */
    @Override
    public String getInsertExceptionSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, merchant_id, exception_msg, exception_step, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)";
    }

    /**
     * 生成通知日志插入的 SQL 语句 (MySQL)
     */
    @Override
    public String getInsertNotifyLogSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, sub_order_no, merchant_id, notify_url, notify_status, notify_result, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "notify_url = VALUES(notify_url), notify_status = VALUES(notify_status), "
                + "notify_result = VALUES(notify_result), update_time = VALUES(update_time)";
    }

    /**
     * 生成退款分项记录插入的 SQL 语句
     */
    @Override
    public String getInsertOrderRefundSplitSubSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, sub_order_no, merchant_id, refund_split_amount, refund_split_amount_fen, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * 生成查询待核账订单的分页 SQL 语句 (MySQL)
     */
    @Override
    public String getPendingReconOrdersSql(String tableName, int offset, int limit) {
        return "SELECT * FROM " + tableName + " WHERE recon_status = 0 ORDER BY create_time ASC LIMIT " + limit
                + " OFFSET " + offset;
    }

}
