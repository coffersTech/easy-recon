package tech.coffers.recon.repository;

import tech.coffers.recon.entity.*;
import tech.coffers.recon.api.enums.ReconStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对账仓储接口
 * <p>
 * 提供对账系统核心实体的持久化操作，包括订单主记录、分账明细、通知状态及对账异常记录等。
 *
 * @author coffersTech
 */
public interface ReconRepository {

        /**
         * 保存或更新主订单对账记录
         *
         * @param orderMainDO 对账主记录
         * @return 是否成功
         */
        boolean saveOrderMain(ReconOrderMainDO orderMainDO);

        /**
         * 保存单条分账子明细记录
         *
         * @param splitSubDO 分账子记录
         * @return 是否成功
         */
        boolean saveOrderSplitSub(ReconOrderSplitSubDO splitSubDO);

        /**
         * 批量保存分账子明细记录
         *
         * @param splitSubDOs 分账子记录列表
         * @return 是否成功
         */
        boolean batchSaveOrderSplitSub(List<ReconOrderSplitSubDO> splitSubDOs);

        /**
         * 记录核账异常信息并发送通知
         *
         * @param exceptionDO 异常记录明细
         * @return 是否成功
         */
        boolean saveException(ReconExceptionDO exceptionDO);

        /**
         * 批量记录异常信息
         *
         * @param exceptions 异常列表
         * @return 是否成功
         */
        boolean batchSaveException(List<ReconExceptionDO> exceptions);

        /**
         * 保存通知流水日志
         *
         * @param notifyLogDO 通知日志 DO
         * @return 是否成功
         */
        boolean saveNotifyLog(ReconNotifyLogDO notifyLogDO);

        /**
         * 根据主订单号查询订单主记录
         *
         * @param orderNo 业务订单号
         * @return 订单记录记录
         */
        ReconOrderMainDO getOrderMainByOrderNo(String orderNo);

        /**
         * 根据主订单号查询所有级联的分账子记录
         *
         * @param orderNo 订单号
         * @return 分账明细列表
         */
        List<ReconOrderSplitSubDO> getOrderSplitSubByOrderNo(String orderNo);

        /**
         * 分页查询特定日期的待处理（未核账成功）订单
         *
         * @param dateStr 业务日期 yyyy-MM-dd
         * @param offset  起始位置
         * @param limit   拉取数量
         * @return 待核账订单列表
         */
        List<ReconOrderMainDO> getPendingReconOrders(String dateStr, int offset, int limit);

        /**
         * 更新订单的全局核账状态（如 INIT to PENDING to SUCCESS）
         *
         * @param orderNo     订单号
         * @param reconStatus 目标状态枚举
         * @return 是否成功
         */
        boolean updateReconStatus(String orderNo, ReconStatusEnum reconStatus);

        /**
         * 提供简化的反查功能：通过商户号和子订单号定位关联的主订单号
         *
         * @param merchantId 商户ID
         * @param subOrderNo 子订单号
         * @return 对应的主订单号
         */
        String findOrderNoBySub(String merchantId, String subOrderNo);

        /**
         * 通过商户号和商户原始订单号定义关联的主订单号
         *
         * @param merchantId      商户ID
         * @param merchantOrderNo 商户原始订单号
         * @return 对应的主订单号
         */
        String findOrderNoByMerchantOrder(String merchantId, String merchantOrderNo);

        /**
         * 查询订单当前的核账状态码
         *
         * @param orderNo 订单号
         * @return 状态码 Integer
         */
        Integer getReconStatus(String orderNo);

        /**
         * 更新订单的主通知状态 (用于标记主侧通知是否闭环)
         *
         * @param orderNo      订单号
         * @param notifyStatus 通知状态码
         * @param notifyResult 通知返回结果
         * @return 是否成功
         */
        boolean updateNotifyStatus(String orderNo, int notifyStatus, String notifyResult);

        /**
         * 更新特定子商户分账项的通知状态
         *
         * @param orderNo      主订单号
         * @param merchantId   子商户ID
         * @param subOrderNo   子订单号
         * @param notifyStatus 状态码
         * @param notifyResult 原始返回结果
         * @return 更新结果
         */
        boolean updateSplitSubNotifyStatus(String orderNo, String merchantId, String subOrderNo, int notifyStatus,
                        String notifyResult);

        /**
         * 校验当前主订单下所有的分账通知状态是否均已标记为“成功”
         *
         * @param orderNo 业务单号
         * @return boolean
         */
        boolean isAllSplitSubNotified(String orderNo);

        /**
         * 按日期和状态条件分页查询主纪录
         */
        List<ReconOrderMainDO> getOrderMainByDate(String dateStr, ReconStatusEnum reconStatus, int offset, int limit);

        /**
         * 查询对账异常详情列表
         */
        List<ReconExceptionDO> getExceptionRecords(String merchantId, String startDate, String endDate,
                        Integer exceptionStep, int offset, int limit);

        /**
         * 批量持久化退款场景下的分账数据
         */
        boolean batchSaveOrderRefundSplitSub(List<ReconOrderRefundSplitSubDO> refundSplitSubDOs);

        /**
         * 更新退款对账逻辑
         */
        boolean updateReconRefundStatus(String orderNo, int refundStatus, BigDecimal refundAmount,
                        LocalDateTime refundTime);

        /**
         * 查询退款分证明细
         */
        List<ReconOrderRefundSplitSubDO> getOrderRefundSplitSubByOrderNo(String orderNo);

        /**
         * 保存对账规则配置
         */
        boolean saveReconRule(ReconRuleDO reconRuleDO);

        /**
         * 获取常用配置项
         */
        ReconRuleDO getReconRuleById(Long id);

        ReconRuleDO getReconRuleByName(String ruleName);

        List<ReconRuleDO> getEnabledReconRules();

        List<ReconRuleDO> getReconRules(int offset, int limit);

        boolean updateReconRule(ReconRuleDO reconRuleDO);

        boolean deleteReconRule(Long id);

        List<ReconExceptionDO> getExceptionsByOrderNo(String orderNo);

        ReconSummaryDO getReconSummary(String dateStr);

        long countOrderMainByDate(String dateStr, ReconStatusEnum reconStatus);

        long countExceptionRecords(String merchantId, String startDate, String endDate, Integer exceptionStep);

        /**
         * 根据主订单号查询关联的通知日志
         *
         * @param orderNo 订单号
         * @return 通知日志列表
         */
        List<ReconNotifyLogDO> getNotifyLogsByOrderNo(String orderNo);
}
