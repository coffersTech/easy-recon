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

    @Override
    public String getInsertOrderMainSql(String tableName) {
        return "INSERT INTO " + tableName + " (order_no, merchant_id, pay_amount, platform_income, pay_fee, split_total_amount, recon_status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public String getInsertOrderSplitSubSql(String tableName) {
        return "INSERT INTO " + tableName + " (order_no, merchant_id, split_amount, create_time, update_time) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public String getInsertExceptionSql(String tableName) {
        return "INSERT INTO " + tableName + " (order_no, merchant_id, exception_msg, exception_step, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public String getInsertNotifyLogSql(String tableName) {
        return "INSERT INTO " + tableName + " (order_no, merchant_id, notify_url, notify_status, notify_result, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public String getPendingReconOrdersSql(String tableName, int offset, int limit) {
        return "SELECT * FROM " + tableName + " WHERE recon_status = 0 ORDER BY create_time ASC LIMIT " + limit + " OFFSET " + offset;
    }

}
