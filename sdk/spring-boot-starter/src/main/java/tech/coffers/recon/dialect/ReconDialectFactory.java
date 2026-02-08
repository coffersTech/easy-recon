package tech.coffers.recon.dialect;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * 数据库方言工厂
 * <p>
 * 根据数据源自动检测数据库类型并创建对应的方言实例
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Slf4j
public class ReconDialectFactory {

    private final ReconDatabaseDialect dialect;

    /**
     * 构造函数
     *
     * @param dataSource 数据源
     */
    public ReconDialectFactory(DataSource dataSource) {
        this.dialect = detectDialect(dataSource);
    }

    /**
     * 获取数据库方言
     */
    public ReconDatabaseDialect getDialect() {
        return dialect;
    }

    /**
     * 检测数据库类型并创建对应的方言
     */
    private ReconDatabaseDialect detectDialect(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            log.info("检测到数据库类型: {}", databaseProductName);

            if (databaseProductName.toLowerCase().contains("mysql")) {
                return new MySqlReconDialect();
            } else if (databaseProductName.toLowerCase().contains("postgresql")) {
                return new PgReconDialect();
            } else {
                log.warn("未检测到支持的数据库类型，使用默认方言（MySQL）");
                return new MySqlReconDialect();
            }
        } catch (Exception e) {
            log.error("检测数据库类型失败，使用默认方言（MySQL）", e);
            return new MySqlReconDialect();
        }
    }

}
