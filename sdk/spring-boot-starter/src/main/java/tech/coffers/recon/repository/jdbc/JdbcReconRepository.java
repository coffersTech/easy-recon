package tech.coffers.recon.repository.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.dialect.ReconDialectFactory;
import tech.coffers.recon.entity.*;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC 对账存储库实现
 * <p>
 * 使用 JdbcTemplate 实现对账相关的数据库操作，支持 MySQL 和 PostgreSQL
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Slf4j
public class JdbcReconRepository implements ReconRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ReconDialectFactory dialectFactory;
    private final ReconSdkProperties properties;

    /**
     * 构造函数
     */
    public JdbcReconRepository(JdbcTemplate jdbcTemplate, ReconDialectFactory dialectFactory,
            ReconSdkProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialectFactory = dialectFactory;
        this.properties = properties;
    }

    // ==================== 订单主记录操作 ====================

    @Override
    public boolean saveOrderMain(ReconOrderMainDO orderMainDO) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            String sql = dialectFactory.getDialect().getInsertOrderMainSql(tableName);
            int rows = jdbcTemplate.update(sql, ps -> {
                ps.setString(1, orderMainDO.getOrderNo());
                ps.setString(2, orderMainDO.getMerchantId());
                ps.setBigDecimal(3, orderMainDO.getPayAmount());
                ps.setObject(4, orderMainDO.getPayAmountFen());
                ps.setBigDecimal(5, orderMainDO.getPlatformIncome());
                ps.setObject(6, orderMainDO.getPlatformIncomeFen());
                ps.setBigDecimal(7, orderMainDO.getPayFee());
                ps.setObject(8, orderMainDO.getPayFeeFen());
                ps.setBigDecimal(9, orderMainDO.getSplitTotalAmount());
                ps.setObject(10, orderMainDO.getSplitTotalAmountFen());
                ps.setInt(11, orderMainDO.getReconStatus());
                ps.setObject(12, orderMainDO.getCreateTime());
                ps.setObject(13, orderMainDO.getUpdateTime());
            });
            return rows > 0;
        } catch (Exception e) {
            log.error("保存订单主记录失败", e);
            return false;
        }
    }

    @Override
    public ReconOrderMainDO getOrderMainByOrderNo(String orderNo) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            String sql = "SELECT * FROM " + tableName + " WHERE order_no = ?";
            return jdbcTemplate.queryForObject(sql, new OrderMainRowMapper(), orderNo);
        } catch (Exception e) {
            log.error("查询订单主记录失败，订单号: {}", orderNo, e);
            return null;
        }
    }

    // ==================== 分账子记录操作 ====================

    @Override
    public boolean batchSaveOrderSplitSub(List<ReconOrderSplitSubDO> splitSubDOs) {
        if (splitSubDOs == null || splitSubDOs.isEmpty()) {
            return true;
        }

        try {
            String tableName = properties.getTablePrefix() + "order_split_sub";
            String sql = dialectFactory.getDialect().getInsertOrderSplitSubSql(tableName);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ReconOrderSplitSubDO subDO = splitSubDOs.get(i);
                    ps.setString(1, subDO.getOrderNo());
                    ps.setString(2, subDO.getMerchantId());
                    ps.setBigDecimal(3, subDO.getSplitAmount());
                    ps.setObject(4, subDO.getSplitAmountFen());
                    ps.setObject(5, subDO.getCreateTime());
                    ps.setObject(6, subDO.getUpdateTime());
                }

                @Override
                public int getBatchSize() {
                    return splitSubDOs.size();
                }
            });
            return true;
        } catch (Exception e) {
            log.error("批量保存分账子记录失败", e);
            return false;
        }
    }

    @Override
    public List<ReconOrderSplitSubDO> getOrderSplitSubByOrderNo(String orderNo) {
        try {
            String tableName = properties.getTablePrefix() + "order_split_sub";
            String sql = "SELECT * FROM " + tableName + " WHERE order_no = ?";
            return jdbcTemplate.query(sql, new OrderSplitSubRowMapper(), orderNo);
        } catch (Exception e) {
            log.error("查询分账子记录失败，订单号: {}", orderNo, e);
            return null;
        }
    }

    // ==================== 异常记录操作 ====================

    @Override
    public boolean saveException(ReconExceptionDO exceptionDO) {
        try {
            String tableName = properties.getTablePrefix() + "exception";
            String sql = dialectFactory.getDialect().getInsertExceptionSql(tableName);
            int rows = jdbcTemplate.update(sql, ps -> {
                ps.setString(1, exceptionDO.getOrderNo());
                ps.setString(2, exceptionDO.getMerchantId());
                ps.setString(3, exceptionDO.getExceptionMsg());
                ps.setInt(4, exceptionDO.getExceptionStep());
                ps.setObject(5, exceptionDO.getCreateTime());
                ps.setObject(6, exceptionDO.getUpdateTime());
            });
            return rows > 0;
        } catch (Exception e) {
            log.error("保存异常记录失败", e);
            return false;
        }
    }

    @Override
    public boolean batchSaveException(List<ReconExceptionDO> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return true;
        }

        try {
            String tableName = properties.getTablePrefix() + "exception";
            String sql = dialectFactory.getDialect().getInsertExceptionSql(tableName);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ReconExceptionDO exceptionDO = exceptions.get(i);
                    ps.setString(1, exceptionDO.getOrderNo());
                    ps.setString(2, exceptionDO.getMerchantId());
                    ps.setString(3, exceptionDO.getExceptionMsg());
                    ps.setInt(4, exceptionDO.getExceptionStep());
                    ps.setObject(5, exceptionDO.getCreateTime());
                    ps.setObject(6, exceptionDO.getUpdateTime());
                }

                @Override
                public int getBatchSize() {
                    return exceptions.size();
                }
            });
            return true;
        } catch (Exception e) {
            log.error("批量保存异常记录失败", e);
            return false;
        }
    }

    // ==================== 通知日志操作 ====================

    @Override
    public boolean saveNotifyLog(ReconNotifyLogDO notifyLogDO) {
        try {
            String tableName = properties.getTablePrefix() + "notify_log";
            String sql = dialectFactory.getDialect().getInsertNotifyLogSql(tableName);
            int rows = jdbcTemplate.update(sql, ps -> {
                ps.setString(1, notifyLogDO.getOrderNo());
                ps.setString(2, notifyLogDO.getMerchantId());
                ps.setString(3, notifyLogDO.getNotifyUrl());
                ps.setInt(4, notifyLogDO.getNotifyStatus());
                ps.setString(5, notifyLogDO.getNotifyResult());
                ps.setObject(6, notifyLogDO.getCreateTime());
                ps.setObject(7, notifyLogDO.getUpdateTime());
            });
            return rows > 0;
        } catch (Exception e) {
            log.error("保存通知日志失败", e);
            return false;
        }
    }

    // ==================== 其他操作 ====================

    @Override
    public List<ReconOrderMainDO> getPendingReconOrders(String dateStr, int offset, int limit) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            String sql = dialectFactory.getDialect().getPendingReconOrdersSql(tableName, offset, limit);
            return jdbcTemplate.query(sql, new OrderMainRowMapper());
        } catch (Exception e) {
            log.error("查询待核账订单失败，日期: {}", dateStr, e);
            return null;
        }
    }

    @Override
    public boolean updateReconStatus(String orderNo, int reconStatus) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            String sql = "UPDATE " + tableName + " SET recon_status = ?, update_time = ? WHERE order_no = ?";
            int rows = jdbcTemplate.update(sql, reconStatus, LocalDateTime.now(), orderNo);
            return rows > 0;
        } catch (Exception e) {
            log.error("更新对账状态失败，订单号: {}", orderNo, e);
            return false;
        }
    }

    @Override
    public Integer getReconStatus(String orderNo) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            String sql = "SELECT recon_status FROM " + tableName + " WHERE order_no = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, orderNo);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            log.error("查询对账状态失败，订单号: {}", orderNo, e);
            return null;
        }
    }

    // ==================== 新增查询方法 ====================

    @Override
    public List<ReconOrderMainDO> getOrderMainByMerchantId(String merchantId, String startDate, String endDate,
            Integer reconStatus, int offset, int limit) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE merchant_id = ?");

            // 添加日期条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append(" AND DATE(create_time) >= ?");
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append(" AND DATE(create_time) <= ?");
            }

            // 添加对账状态条件
            if (reconStatus != null) {
                sql.append(" AND recon_status = ?");
            }

            // 添加排序和分页
            sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

            // 构建参数
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(merchantId);
            if (startDate != null && !startDate.isEmpty()) {
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                params.add(endDate);
            }
            if (reconStatus != null) {
                params.add(reconStatus);
            }
            params.add(limit);
            params.add(offset);

            return jdbcTemplate.query(sql.toString(), new OrderMainRowMapper(), params.toArray());
        } catch (Exception e) {
            log.error("根据商户ID查询对账订单失败", e);
            return null;
        }
    }

    @Override
    public List<ReconOrderMainDO> getOrderMainByDate(String dateStr, Integer reconStatus, int offset, int limit) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE DATE(create_time) = ?");

            // 添加对账状态条件
            if (reconStatus != null) {
                sql.append(" AND recon_status = ?");
            }

            // 添加排序和分页
            sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

            // 构建参数
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(dateStr);
            if (reconStatus != null) {
                params.add(reconStatus);
            }
            params.add(limit);
            params.add(offset);

            return jdbcTemplate.query(sql.toString(), new OrderMainRowMapper(), params.toArray());
        } catch (Exception e) {
            log.error("根据日期查询对账订单失败，日期: {}", dateStr, e);
            return null;
        }
    }

    @Override
    public List<ReconExceptionDO> getExceptionRecords(String merchantId, String startDate, String endDate,
            Integer exceptionStep, int offset, int limit) {
        try {
            String tableName = properties.getTablePrefix() + "exception";
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);

            // 添加条件
            boolean hasWhere = false;
            if (merchantId != null && !merchantId.isEmpty()) {
                sql.append(" WHERE merchant_id = ?");
                hasWhere = true;
            }

            if (startDate != null && !startDate.isEmpty()) {
                if (hasWhere) {
                    sql.append(" AND");
                } else {
                    sql.append(" WHERE");
                    hasWhere = true;
                }
                sql.append(" DATE(create_time) >= ?");
            }

            if (endDate != null && !endDate.isEmpty()) {
                if (hasWhere) {
                    sql.append(" AND");
                } else {
                    sql.append(" WHERE");
                    hasWhere = true;
                }
                sql.append(" DATE(create_time) <= ?");
            }

            if (exceptionStep != null) {
                if (hasWhere) {
                    sql.append(" AND");
                } else {
                    sql.append(" WHERE");
                }
                sql.append(" exception_step = ?");
            }

            // 添加排序和分页
            sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");

            // 构建参数
            java.util.List<Object> params = new java.util.ArrayList<>();
            if (merchantId != null && !merchantId.isEmpty()) {
                params.add(merchantId);
            }
            if (startDate != null && !startDate.isEmpty()) {
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                params.add(endDate);
            }
            if (exceptionStep != null) {
                params.add(exceptionStep);
            }
            params.add(limit);
            params.add(offset);

            return jdbcTemplate.query(sql.toString(), new ExceptionRowMapper(), params.toArray());
        } catch (Exception e) {
            log.error("查询对账异常记录失败", e);
            return null;
        }
    }

    @Override
    public List<ReconExceptionDO> getExceptionsByOrderNo(String orderNo) {
        try {
            String tableName = properties.getTablePrefix() + "exception";
            String sql = "SELECT * FROM " + tableName + " WHERE order_no = ? ORDER BY create_time DESC";
            return jdbcTemplate.query(sql, new ExceptionRowMapper(), orderNo);
        } catch (Exception e) {
            log.error("根据订单号查询对账异常记录列表失败，订单号: {}", orderNo, e);
            return null;
        }
    }

    // ==================== 统计报表 ====================

    @Override
    public ReconSummaryDO getReconSummary(String dateStr) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            // 聚合查询
            String sql = "SELECT " +
                    "COUNT(*) as total_orders, " +
                    "SUM(CASE WHEN recon_status = 1 THEN 1 ELSE 0 END) as success_count, " +
                    "SUM(CASE WHEN recon_status = 2 THEN 1 ELSE 0 END) as fail_count, " +
                    "SUM(CASE WHEN recon_status = 0 THEN 1 ELSE 0 END) as init_count, " +
                    "SUM(pay_amount) as total_amount " +
                    "FROM " + tableName + " " +
                    "WHERE DATE(create_time) = ?";

            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                ReconSummaryDO summary = new ReconSummaryDO();
                summary.setSummaryDate(java.time.LocalDate.parse(dateStr));
                summary.setTotalOrders(rs.getInt("total_orders"));
                summary.setSuccessCount(rs.getInt("success_count"));
                summary.setFailCount(rs.getInt("fail_count"));
                summary.setInitCount(rs.getInt("init_count"));
                summary.setTotalAmount(rs.getBigDecimal("total_amount"));
                if (summary.getTotalAmount() == null) {
                    summary.setTotalAmount(BigDecimal.ZERO);
                }
                return summary;
            }, dateStr);
        } catch (Exception e) {
            log.error("查询对账统计数据失败，日期: {}", dateStr, e);
            return null;
        }
    }

    // ==================== 退款操作 ====================

    @Override
    public boolean batchSaveOrderRefundSplitSub(List<ReconOrderRefundSplitSubDO> refundSplitSubDOs) {
        if (refundSplitSubDOs == null || refundSplitSubDOs.isEmpty()) {
            return true;
        }

        try {
            String tableName = properties.getTablePrefix() + "order_refund_split_sub";
            String sql = dialectFactory.getDialect().getInsertOrderRefundSplitSubSql(tableName);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ReconOrderRefundSplitSubDO subDO = refundSplitSubDOs.get(i);
                    ps.setString(1, subDO.getOrderNo());
                    ps.setString(2, subDO.getMerchantId());
                    ps.setBigDecimal(3, subDO.getRefundSplitAmount());
                    ps.setObject(4, subDO.getRefundSplitAmountFen());
                    ps.setObject(5, subDO.getCreateTime());
                    ps.setObject(6, subDO.getUpdateTime());
                }

                @Override
                public int getBatchSize() {
                    return refundSplitSubDOs.size();
                }
            });
            return true;
        } catch (Exception e) {
            log.error("批量保存退款分账子记录失败", e);
            return false;
        }
    }

    @Override
    public boolean updateReconRefundStatus(String orderNo, int refundStatus, BigDecimal refundAmount,
            LocalDateTime refundTime) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            String sql = "UPDATE " + tableName
                    + " SET refund_status = ?, refund_amount = ?, refund_amount_fen = ?, refund_time = ?, update_time = ? WHERE order_no = ?";
            Long refundAmountFen = refundAmount != null ? refundAmount.multiply(new BigDecimal("100")).longValue()
                    : null;
            int rows = jdbcTemplate.update(sql, refundStatus, refundAmount, refundAmountFen, refundTime,
                    LocalDateTime.now(), orderNo);
            return rows > 0;
        } catch (Exception e) {
            log.error("更新退款对账状态失败，订单号: {}", orderNo, e);
            return false;
        }
    }

    @Override
    public List<ReconOrderRefundSplitSubDO> getOrderRefundSplitSubByOrderNo(String orderNo) {
        try {
            String tableName = properties.getTablePrefix() + "order_refund_split_sub";
            String sql = "SELECT * FROM " + tableName + " WHERE order_no = ?";
            return jdbcTemplate.query(sql, new OrderRefundSplitSubRowMapper(), orderNo);
        } catch (Exception e) {
            log.error("查询退款分账子记录失败，订单号: {}", orderNo, e);
            return null;
        }
    }

    // ==================== 对账规则操作 ====================

    @Override
    public boolean saveReconRule(ReconRuleDO reconRuleDO) {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "INSERT INTO " + tableName
                    + " (rule_name, rule_type, rule_expression, rule_desc, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int rows = jdbcTemplate.update(sql, ps -> {
                ps.setString(1, reconRuleDO.getRuleName());
                ps.setInt(2, reconRuleDO.getRuleType());
                ps.setString(3, reconRuleDO.getRuleExpression());
                ps.setString(4, reconRuleDO.getRuleDesc());
                ps.setInt(5, reconRuleDO.getStatus());
                ps.setObject(6, LocalDateTime.now());
                ps.setObject(7, LocalDateTime.now());
            });
            return rows > 0;
        } catch (Exception e) {
            log.error("保存对账规则失败", e);
            return false;
        }
    }

    @Override
    public ReconRuleDO getReconRuleById(Long id) {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new ReconRuleRowMapper(), id);
        } catch (Exception e) {
            log.error("根据 ID 查询对账规则失败，ID: {}", id, e);
            return null;
        }
    }

    @Override
    public ReconRuleDO getReconRuleByName(String ruleName) {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "SELECT * FROM " + tableName + " WHERE rule_name = ?";
            return jdbcTemplate.queryForObject(sql, new ReconRuleRowMapper(), ruleName);
        } catch (Exception e) {
            log.error("根据规则名称查询对账规则失败，规则名称: {}", ruleName, e);
            return null;
        }
    }

    @Override
    public List<ReconRuleDO> getEnabledReconRules() {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "SELECT * FROM " + tableName + " WHERE status = 1";
            return jdbcTemplate.query(sql, new ReconRuleRowMapper());
        } catch (Exception e) {
            log.error("查询启用的对账规则失败", e);
            return null;
        }
    }

    @Override
    public List<ReconRuleDO> getReconRules(int offset, int limit) {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "SELECT * FROM " + tableName + " ORDER BY create_time DESC LIMIT ? OFFSET ?";
            return jdbcTemplate.query(sql, new ReconRuleRowMapper(), limit, offset);
        } catch (Exception e) {
            log.error("查询对账规则列表失败", e);
            return null;
        }
    }

    @Override
    public boolean updateReconRule(ReconRuleDO reconRuleDO) {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "UPDATE " + tableName
                    + " SET rule_name = ?, rule_type = ?, rule_expression = ?, rule_desc = ?, status = ?, update_time = ? WHERE id = ?";
            int rows = jdbcTemplate.update(sql, ps -> {
                ps.setString(1, reconRuleDO.getRuleName());
                ps.setInt(2, reconRuleDO.getRuleType());
                ps.setString(3, reconRuleDO.getRuleExpression());
                ps.setString(4, reconRuleDO.getRuleDesc());
                ps.setInt(5, reconRuleDO.getStatus());
                ps.setObject(6, LocalDateTime.now());
                ps.setLong(7, reconRuleDO.getId());
            });
            return rows > 0;
        } catch (Exception e) {
            log.error("更新对账规则失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteReconRule(Long id) {
        try {
            String tableName = properties.getTablePrefix() + "rule";
            String sql = "DELETE FROM " + tableName + " WHERE id = ?";
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            log.error("删除对账规则失败，ID: {}", id, e);
            return false;
        }
    }

    // ==================== 行映射器 ====================

    @Override
    public long countOrderMainByMerchantId(String merchantId, String startDate, String endDate, Integer reconStatus) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + tableName + " WHERE merchant_id = ?");

            // 添加日期条件
            if (startDate != null && !startDate.isEmpty()) {
                sql.append(" AND DATE(create_time) >= ?");
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append(" AND DATE(create_time) <= ?");
            }

            // 添加对账状态条件
            if (reconStatus != null) {
                sql.append(" AND recon_status = ?");
            }

            // 构建参数
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(merchantId);
            if (startDate != null && !startDate.isEmpty()) {
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                params.add(endDate);
            }
            if (reconStatus != null) {
                params.add(reconStatus);
            }

            Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计商户对账订单数量失败", e);
            return 0;
        }
    }

    @Override
    public long countOrderMainByDate(String dateStr, Integer reconStatus) {
        try {
            String tableName = properties.getTablePrefix() + "order_main";
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + tableName + " WHERE DATE(create_time) = ?");

            // 添加对账状态条件
            if (reconStatus != null) {
                sql.append(" AND recon_status = ?");
            }

            // 构建参数
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(dateStr);
            if (reconStatus != null) {
                params.add(reconStatus);
            }

            Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计日期对账订单数量失败", e);
            return 0;
        }
    }

    @Override
    public long countExceptionRecords(String merchantId, String startDate, String endDate, Integer exceptionStep) {
        try {
            String tableName = properties.getTablePrefix() + "exception";
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + tableName);

            // 添加条件
            boolean hasWhere = false;
            if (merchantId != null && !merchantId.isEmpty()) {
                sql.append(" WHERE merchant_id = ?");
                hasWhere = true;
            }

            if (startDate != null && !startDate.isEmpty()) {
                if (hasWhere) {
                    sql.append(" AND");
                } else {
                    sql.append(" WHERE");
                    hasWhere = true;
                }
                sql.append(" DATE(create_time) >= ?");
            }

            if (endDate != null && !endDate.isEmpty()) {
                if (hasWhere) {
                    sql.append(" AND");
                } else {
                    sql.append(" WHERE");
                    hasWhere = true;
                }
                sql.append(" DATE(create_time) <= ?");
            }

            if (exceptionStep != null) {
                if (hasWhere) {
                    sql.append(" AND");
                } else {
                    sql.append(" WHERE");
                }
                sql.append(" exception_step = ?");
            }

            // 构建参数
            java.util.List<Object> params = new java.util.ArrayList<>();
            if (merchantId != null && !merchantId.isEmpty()) {
                params.add(merchantId);
            }
            if (startDate != null && !startDate.isEmpty()) {
                params.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                params.add(endDate);
            }
            if (exceptionStep != null) {
                params.add(exceptionStep);
            }

            Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计异常记录数量失败", e);
            return 0;
        }
    }

    /**
     * 订单主记录行映射器
     */
    private static class OrderMainRowMapper implements RowMapper<ReconOrderMainDO> {
        @Override
        public ReconOrderMainDO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReconOrderMainDO mainDO = new ReconOrderMainDO();
            mainDO.setId(rs.getLong("id"));
            mainDO.setOrderNo(rs.getString("order_no"));
            mainDO.setMerchantId(rs.getString("merchant_id"));
            mainDO.setPayAmount(rs.getBigDecimal("pay_amount"));
            mainDO.setPayAmountFen(rs.getObject("pay_amount_fen", Long.class));
            mainDO.setPlatformIncome(rs.getBigDecimal("platform_income"));
            mainDO.setPlatformIncomeFen(rs.getObject("platform_income_fen", Long.class));
            mainDO.setPayFee(rs.getBigDecimal("pay_fee"));
            mainDO.setPayFeeFen(rs.getObject("pay_fee_fen", Long.class));
            mainDO.setSplitTotalAmount(rs.getBigDecimal("split_total_amount"));
            mainDO.setSplitTotalAmountFen(rs.getObject("split_total_amount_fen", Long.class));
            mainDO.setReconStatus(rs.getInt("recon_status"));
            mainDO.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
            mainDO.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
            mainDO.setRefundAmount(rs.getBigDecimal("refund_amount"));
            mainDO.setRefundAmountFen(rs.getObject("refund_amount_fen", Long.class));
            mainDO.setRefundStatus(rs.getInt("refund_status"));
            mainDO.setRefundTime(rs.getObject("refund_time", LocalDateTime.class));
            return mainDO;
        }
    }

    /**
     * 分账子记录行映射器
     */
    private static class OrderSplitSubRowMapper implements RowMapper<ReconOrderSplitSubDO> {
        @Override
        public ReconOrderSplitSubDO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReconOrderSplitSubDO subDO = new ReconOrderSplitSubDO();
            subDO.setId(rs.getLong("id"));
            subDO.setOrderNo(rs.getString("order_no"));
            subDO.setMerchantId(rs.getString("merchant_id"));
            subDO.setSplitAmount(rs.getBigDecimal("split_amount"));
            subDO.setSplitAmountFen(rs.getObject("split_amount_fen", Long.class));
            subDO.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
            subDO.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
            return subDO;
        }
    }

    /**
     * 退款分账子记录行映射器
     */
    private static class OrderRefundSplitSubRowMapper implements RowMapper<ReconOrderRefundSplitSubDO> {
        @Override
        public ReconOrderRefundSplitSubDO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReconOrderRefundSplitSubDO subDO = new ReconOrderRefundSplitSubDO();
            subDO.setId(rs.getLong("id"));
            subDO.setOrderNo(rs.getString("order_no"));
            subDO.setMerchantId(rs.getString("merchant_id"));
            subDO.setRefundSplitAmount(rs.getBigDecimal("refund_split_amount"));
            subDO.setRefundSplitAmountFen(rs.getObject("refund_split_amount_fen", Long.class));
            subDO.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
            subDO.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
            return subDO;
        }
    }

    /**
     * 异常记录行映射器
     */
    private static class ExceptionRowMapper implements RowMapper<ReconExceptionDO> {
        @Override
        public ReconExceptionDO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReconExceptionDO exceptionDO = new ReconExceptionDO();
            exceptionDO.setId(rs.getLong("id"));
            exceptionDO.setOrderNo(rs.getString("order_no"));
            exceptionDO.setMerchantId(rs.getString("merchant_id"));
            exceptionDO.setExceptionMsg(rs.getString("exception_msg"));
            exceptionDO.setExceptionStep(rs.getInt("exception_step"));
            exceptionDO.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
            exceptionDO.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
            return exceptionDO;
        }
    }

    /**
     * 对账规则行映射器
     */
    private static class ReconRuleRowMapper implements RowMapper<ReconRuleDO> {
        @Override
        public ReconRuleDO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReconRuleDO ruleDO = new ReconRuleDO();
            ruleDO.setId(rs.getLong("id"));
            ruleDO.setRuleName(rs.getString("rule_name"));
            ruleDO.setRuleType(rs.getInt("rule_type"));
            ruleDO.setRuleExpression(rs.getString("rule_expression"));
            ruleDO.setRuleDesc(rs.getString("rule_desc"));
            ruleDO.setStatus(rs.getInt("status"));
            ruleDO.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
            ruleDO.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
            return ruleDO;
        }
    }

}
