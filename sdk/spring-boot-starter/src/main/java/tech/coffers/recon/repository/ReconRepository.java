package tech.coffers.recon.repository;

import tech.coffers.recon.entity.*;
import tech.coffers.recon.api.enums.ReconStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对账存储库接口
 * <p>
 * 定义对账相关的数据库操作方法，包括订单主记录、分账子记录、异常记录、通知日志等
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
public interface ReconRepository {

        /**
         * 保存对账订单主记录
         *
         * @param orderMainDO 对账订单主记录
         * @return 保存结果
         */
        boolean saveOrderMain(ReconOrderMainDO orderMainDO);

        /**
         * 批量保存分账子记录
         *
         * @param splitSubDOs 分账子记录列表
         * @return 保存结果
         */
        boolean batchSaveOrderSplitSub(List<ReconOrderSplitSubDO> splitSubDOs);

        /**
         * 保存异常记录
         *
         * @param exceptionDO 异常记录
         * @return 保存结果
         */
        boolean saveException(ReconExceptionDO exceptionDO);

        /**
         * 批量保存异常记录
         *
         * @param exceptions 异常记录列表
         * @return 保存结果
         */
        boolean batchSaveException(List<ReconExceptionDO> exceptions);

        /**
         * 保存通知日志
         *
         * @param notifyLogDO 通知日志
         * @return 保存结果
         */
        boolean saveNotifyLog(ReconNotifyLogDO notifyLogDO);

        /**
         * 根据订单号查询对账订单主记录
         *
         * @param orderNo 订单号
         * @return 对账订单主记录
         */
        ReconOrderMainDO getOrderMainByOrderNo(String orderNo);

        /**
         * 根据订单号查询分账子记录
         *
         * @param orderNo 订单号
         * @return 分账子记录列表
         */
        List<ReconOrderSplitSubDO> getOrderSplitSubByOrderNo(String orderNo);

        /**
         * 查询指定日期的待核账订单（分页）
         *
         * @param dateStr 日期字符串，格式：yyyy-MM-dd
         * @param offset  偏移量
         * @param limit   限制数量
         * @return 待核账订单列表
         */
        List<ReconOrderMainDO> getPendingReconOrders(String dateStr, int offset, int limit);

        /**
         * 更新对账状态
         *
         * @param orderNo     订单号
         * @param reconStatus 对账状态
         * @return 更新结果
         */
        boolean updateReconStatus(String orderNo, ReconStatusEnum reconStatus);

        /**
         * 根据订单号查询对账状态
         *
         * @param orderNo 订单号
         * @return 对账状态（可能为 null）
         */
        Integer getReconStatus(String orderNo);

        // ==================== 查询方法 ====================

        /**
         * 根据日期查询对账订单主记录（分页）
         *
         * @param dateStr     日期（yyyy-MM-dd）
         * @param reconStatus 对账状态（null 表示全部）
         * @param offset      偏移量
         * @param limit       限制数量
         * @return 对账订单主记录列表
         */
        List<ReconOrderMainDO> getOrderMainByDate(String dateStr, ReconStatusEnum reconStatus, int offset, int limit);

        /**
         * 查询对账异常记录（分页）
         *
         * @param merchantId    商户ID（null 表示全部）
         * @param startDate     开始日期（yyyy-MM-dd）
         * @param endDate       结束日期（yyyy-MM-dd）
         * @param exceptionStep 异常步骤（null 表示全部）
         * @param offset        偏移量
         * @param limit         限制数量
         * @return 对账异常记录列表
         */
        List<ReconExceptionDO> getExceptionRecords(String merchantId, String startDate, String endDate,
                        Integer exceptionStep, int offset, int limit);

        // ==================== 退款操作 ====================

        /**
         * 批量保存退款分账子记录
         *
         * @param refundSplitSubDOs 退款分账子记录列表
         * @return 保存结果
         */
        boolean batchSaveOrderRefundSplitSub(List<ReconOrderRefundSplitSubDO> refundSplitSubDOs);

        /**
         * 更新退款对账状态
         *
         * @param orderNo      订单号
         * @param refundStatus 退款对账状态
         * @param refundAmount 退款金额
         * @param refundTime   退款时间
         * @return 更新结果
         */
        boolean updateReconRefundStatus(String orderNo, int refundStatus, BigDecimal refundAmount,
                        LocalDateTime refundTime);

        /**
         * 根据订单号查询退款分账子记录
         *
         * @param orderNo 订单号
         * @return 退款分账子记录列表
         */
        List<ReconOrderRefundSplitSubDO> getOrderRefundSplitSubByOrderNo(String orderNo);

        // ==================== 对账规则操作 ====================

        /**
         * 保存对账规则
         *
         * @param reconRuleDO 对账规则
         * @return 保存结果
         */
        boolean saveReconRule(ReconRuleDO reconRuleDO);

        /**
         * 根据 ID 查询对账规则
         *
         * @param id 规则 ID
         * @return 对账规则
         */
        ReconRuleDO getReconRuleById(Long id);

        /**
         * 根据规则名称查询对账规则
         *
         * @param ruleName 规则名称
         * @return 对账规则
         */
        ReconRuleDO getReconRuleByName(String ruleName);

        /**
         * 查询所有启用的对账规则
         *
         * @return 对账规则列表
         */
        List<ReconRuleDO> getEnabledReconRules();

        /**
         * 查询对账规则列表（分页）
         *
         * @param offset 偏移量
         * @param limit  限制数量
         * @return 对账规则列表
         */
        List<ReconRuleDO> getReconRules(int offset, int limit);

        /**
         * 更新对账规则
         *
         * @param reconRuleDO 对账规则
         * @return 更新结果
         */
        boolean updateReconRule(ReconRuleDO reconRuleDO);

        /**
         * 删除对账规则
         *
         * @param id 规则 ID
         * @return 删除结果
         */
        boolean deleteReconRule(Long id);

        /**
         * 根据订单号查询对账异常记录列表
         *
         * @param orderNo 订单号
         * @return 对账异常记录列表
         */
        List<ReconExceptionDO> getExceptionsByOrderNo(String orderNo);

        // ==================== 统计报表 ====================

        /**
         * 获取指定日期的对账统计数据
         *
         * @param dateStr 日期字符串 (yyyy-MM-dd)
         * @return 对账统计数据
         */
        ReconSummaryDO getReconSummary(String dateStr);

        /**
         * 根据日期统计对账订单数量
         *
         * @param dateStr     日期
         * @param reconStatus 对账状态
         * @return 订单数量
         */
        long countOrderMainByDate(String dateStr, ReconStatusEnum reconStatus);

        /**
         * 统计异常记录数量
         *
         * @param merchantId    商户ID
         * @param startDate     开始日期
         * @param endDate       结束日期
         * @param exceptionStep 异常步骤
         * @return 异常记录数量
         */
        long countExceptionRecords(String merchantId, String startDate, String endDate, Integer exceptionStep);

}
