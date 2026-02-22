package tech.coffers.recon.dialect;

/**
 * PostgreSQL 数据库方言实现
 * <p>
 * 为 PostgreSQL 数据库生成 SQL 语句
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public class PgReconDialect implements ReconDatabaseDialect {

    /**
     * 生成主订单插入或更新的 SQL 语句 (PostgreSQL)
     * 使用 ON CONFLICT (order_no) 实现幂等录入
     */
    @Override
    public String getInsertOrderMainSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, pay_amount, pay_amount_fen, platform_income, platform_income_fen, pay_fee, pay_fee_fen, split_total_amount, split_total_amount_fen, pay_status, split_status, notify_status, notify_result, recon_status, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (order_no) DO UPDATE SET "
                + "pay_amount = EXCLUDED.pay_amount, pay_amount_fen = EXCLUDED.pay_amount_fen, "
                + "platform_income = EXCLUDED.platform_income, platform_income_fen = EXCLUDED.platform_income_fen, "
                + "pay_fee = EXCLUDED.pay_fee, pay_fee_fen = EXCLUDED.pay_fee_fen, "
                + "split_total_amount = EXCLUDED.split_total_amount, split_total_amount_fen = EXCLUDED.split_total_amount_fen, "
                + "pay_status = EXCLUDED.pay_status, split_status = EXCLUDED.split_status, "
                + "notify_status = EXCLUDED.notify_status, "
                + "notify_result = EXCLUDED.notify_result, recon_status = EXCLUDED.recon_status, update_time = EXCLUDED.update_time";
    }

    /**
     * 生成分账项插入或更新的 SQL 语句 (PostgreSQL)
     */
    @Override
    public String getInsertOrderSplitSubSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, sub_order_no, merchant_id, merchant_order_no, split_amount, split_amount_fen, arrival_amount, arrival_amount_fen, split_fee, split_fee_fen, notify_status, notify_result, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (order_no, merchant_id, sub_order_no) DO UPDATE SET "
                + "merchant_order_no = EXCLUDED.merchant_order_no, "
                + "split_amount = EXCLUDED.split_amount, split_amount_fen = EXCLUDED.split_amount_fen, "
                + "arrival_amount = EXCLUDED.arrival_amount, arrival_amount_fen = EXCLUDED.arrival_amount_fen, "
                + "split_fee = EXCLUDED.split_fee, split_fee_fen = EXCLUDED.split_fee_fen, "
                + "notify_status = EXCLUDED.notify_status, notify_result = EXCLUDED.notify_result, "
                + "update_time = EXCLUDED.update_time";
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
     * 生成通知日志插入的 SQL 语句 (PostgreSQL)
     */
    @Override
    public String getInsertNotifyLogSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, sub_order_no, merchant_id, notify_url, notify_status, notify_result, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (order_no, merchant_id, sub_order_no) DO UPDATE SET "
                + "notify_url = EXCLUDED.notify_url, notify_status = EXCLUDED.notify_status, "
                + "notify_result = EXCLUDED.notify_result, update_time = EXCLUDED.update_time";
    }

    /**
     * 生成退款分项记录插入的 SQL 语句
     */
    @Override
    public String getInsertOrderRefundSplitSubSql(String tableName) {
        return "INSERT INTO " + tableName
                + " (order_no, sub_order_no, merchant_id, merchant_order_no, refund_split_amount, refund_split_amount_fen, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * 生成查询待核账订单的分页 SQL 语句 (PostgreSQL)
     */
    @Override
    public String getPendingReconOrdersSql(String tableName, int offset, int limit) {
        return "SELECT * FROM " + tableName + " WHERE recon_status = 0 ORDER BY create_time ASC LIMIT " + limit
                + " OFFSET " + offset;
    }

}
