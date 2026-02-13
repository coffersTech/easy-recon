package tech.coffers.recon.dialect;

/**
 * 数据库方言接口
 * <p>
 * 定义不同数据库的 SQL 语句生成方法，支持 MySQL 和 PostgreSQL
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public interface ReconDatabaseDialect {

    /**
     * 获取插入订单主记录的 SQL
     *
     * @param tableName 表名
     * @return SQL 语句
     */
    String getInsertOrderMainSql(String tableName);

    /**
     * 获取插入分账子记录的 SQL
     *
     * @param tableName 表名
     * @return SQL 语句
     */
    String getInsertOrderSplitSubSql(String tableName);

    /**
     * 获取插入异常记录的 SQL
     *
     * @param tableName 表名
     * @return SQL 语句
     */
    String getInsertExceptionSql(String tableName);

    /**
     * 获取插入通知日志的 SQL
     *
     * @param tableName 表名
     * @return SQL 语句
     */
    String getInsertNotifyLogSql(String tableName);

    /**
     * 获取插入退款分账子记录的 SQL
     *
     * @param tableName 表名
     * @return SQL 语句
     */
    String getInsertOrderRefundSplitSubSql(String tableName);

    /**
     * 获取查询待核账订单的 SQL
     *
     * @param tableName 表名
     * @param offset    偏移量
     * @param limit     限制条数
     * @return SQL 语句
     */
    String getPendingReconOrdersSql(String tableName, int offset, int limit);

}
