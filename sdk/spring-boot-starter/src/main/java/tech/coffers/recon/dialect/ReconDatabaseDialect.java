package tech.coffers.recon.dialect;

/**
 * 数据库方言接口
 * <p>
 * 定义了针对不同数据库的 SQL 语句生成方法
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public interface ReconDatabaseDialect {

    /**
     * 获取插入订单主记录的 SQL
     */
    String getInsertOrderMainSql(String tableName);

    /**
     * 获取插入分账子记录的 SQL
     */
    String getInsertOrderSplitSubSql(String tableName);

    /**
     * 获取插入异常记录的 SQL
     */
    String getInsertExceptionSql(String tableName);

    /**
     * 获取插入通知日志的 SQL
     */
    String getInsertNotifyLogSql(String tableName);

    /**
     * 获取插入退款分账子记录的 SQL
     */
    String getInsertOrderRefundSplitSubSql(String tableName);

    /**
     * 获取查询待核账订单的 SQL
     */
    String getPendingReconOrdersSql(String tableName, int offset, int limit);

    /**
     * 获取更新对账状态的 SQL
     */
    default String getUpdateReconStatusSql(String tableName) {
        return "UPDATE " + tableName + " SET recon_status = ?, update_time = ? WHERE order_no = ?";
    }

    /**
     * 获取根据日期统计订单数量的 SQL
     */
    default String getCountOrderMainByDateSql(String tableName, boolean hasStatus) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE DATE(create_time) = ?";
        if (hasStatus) {
            sql += " AND recon_status = ?";
        }
        return sql;
    }

    /**
     * 获取根据日期查询订单列表的 SQL
     */
    default String getSelectOrderMainByDateSql(String tableName, boolean hasStatus) {
        String sql = "SELECT * FROM " + tableName + " WHERE DATE(create_time) = ?";
        if (hasStatus) {
            sql += " AND recon_status = ?";
        }
        sql += " ORDER BY create_time DESC LIMIT ? OFFSET ?";
        return sql;
    }
}
