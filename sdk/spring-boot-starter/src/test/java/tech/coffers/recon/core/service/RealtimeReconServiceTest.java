package tech.coffers.recon.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderRefundSplitSubDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 实时对账服务单元测试
 *
 * @author Ryan
 * @since 1.0.0
 */
class RealtimeReconServiceTest {

        @Mock
        private ReconRepository reconRepository;

        @Mock
        private ExceptionRecordService exceptionRecordService;

        @Mock
        private AlarmService alarmService;

        @Mock
        private ReconSdkProperties properties;

        @Mock
        private java.util.concurrent.ExecutorService executorService;

        private RealtimeReconService realtimeReconService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                when(properties.getAmountTolerance()).thenReturn(new BigDecimal("0.01"));
                realtimeReconService = new RealtimeReconService(reconRepository, exceptionRecordService, alarmService,
                                properties, executorService);
        }

        @Test
        void testReconOrder_Success() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_001";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setSubOrderNo(orderNo + "-S1");
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("94.00"));
                splitDetails.add(sub);

                // 模拟存储库方法
                when(reconRepository.saveOrderMain(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSplitSub(any())).thenReturn(true);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS,
                                NotifyStatusEnum.SUCCESS);

                // 验证结果
                assertTrue(result.isSuccess());
                assertEquals(200, result.getCode());
                assertEquals("对账成功", result.getMessage());
                assertEquals(orderNo, result.getOrderNo());

                // 验证存储库方法被调用
                verify(reconRepository, times(1)).saveOrderMain(any());
                verify(reconRepository, times(1)).batchSaveOrderSplitSub(any());
                verify(exceptionRecordService, never()).recordReconException(any(), any(), any(), anyInt());
                verify(alarmService, never()).sendReconAlarm(any(), any(), any());
        }

        @Test
        void testReconOrder_PayStatusFailure() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_002";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("94.00"));
                splitDetails.add(sub);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.FAILURE, SplitStatusEnum.SUCCESS,
                                NotifyStatusEnum.SUCCESS);

                // 验证结果
                assertFalse(result.isSuccess());
                assertEquals(500, result.getCode());
                assertEquals("支付状态失败，对账失败", result.getMessage());
                assertEquals(orderNo, result.getOrderNo());

                // 验证异常记录和告警被调用
                verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq("SELF"),
                                eq("支付状态失败，对账失败"),
                                eq(1));
                verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq("SELF"), eq("支付状态失败，对账失败"));
        }

        @Test
        void testReconOrder_SplitStatusFailure() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_002_SPLIT";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("94.00"));
                splitDetails.add(sub);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.SUCCESS, SplitStatusEnum.FAILURE,
                                NotifyStatusEnum.SUCCESS);

                // 验证结果
                assertFalse(result.isSuccess());
                assertEquals(500, result.getCode());
                assertEquals("分账状态失败，对账失败", result.getMessage());
                assertEquals(orderNo, result.getOrderNo());

                // 验证异常记录和告警被调用
                verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq("SELF"),
                                eq("分账状态失败，对账失败"),
                                eq(2));
                verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq("SELF"), eq("分账状态失败，对账失败"));
        }

        @Test
        void testReconOrder_NotifyStatusFailure() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_003";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("94.00"));
                splitDetails.add(sub);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS,
                                NotifyStatusEnum.FAILURE);

                // 验证结果
                assertFalse(result.isSuccess());
                assertEquals(500, result.getCode());
                assertEquals("通知状态失败，对账失败", result.getMessage());
                assertEquals(orderNo, result.getOrderNo());

                // 验证异常记录和告警被调用
                verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq("SELF"),
                                eq("通知状态失败，对账失败"),
                                eq(3));
                verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq("SELF"), eq("通知状态失败，对账失败"));
        }

        @Test
        void testReconOrder_AmountCheckFailure() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_004";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("90.00")); // 分账金额错误，应该是 94.00
                splitDetails.add(sub);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS,
                                NotifyStatusEnum.SUCCESS);

                // 验证结果
                assertFalse(result.isSuccess());
                assertEquals(500, result.getCode());
                assertEquals("金额校验失败，实付金额与计算金额不一致", result.getMessage());
                assertEquals(orderNo, result.getOrderNo());

                // 验证异常记录和告警被调用
                verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq("SELF"),
                                eq("金额校验失败，实付金额与计算金额不一致"), eq(4));
                verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq("SELF"), eq("金额校验失败，实付金额与计算金额不一致"));
        }

        @Test
        void testReconOrder_Exception() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_005";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("94.00"));
                splitDetails.add(sub);

                // 模拟存储库方法抛出异常
                when(reconRepository.saveOrderMain(any())).thenThrow(new RuntimeException("数据库操作失败"));

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS,
                                NotifyStatusEnum.SUCCESS);

                // 验证结果
                assertFalse(result.isSuccess());
                assertEquals(500, result.getCode());
                assertTrue(result.getMessage().contains("对账处理异常"));
                assertEquals(orderNo, result.getOrderNo());

                // 验证异常记录和告警被调用
                verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq("SELF"), anyString(),
                                eq(5));
                verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq("SELF"), anyString());
        }

        @Test
        void testReconRefund_Success() {
                // 准备测试数据
                String orderNo = "TEST_REFUND_001";
                String merchantId = "MERCHANT_001";
                BigDecimal refundAmount = new BigDecimal("50.00");
                LocalDateTime refundTime = LocalDateTime.now();
                List<ReconOrderRefundSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderRefundSplitSubDO sub = new ReconOrderRefundSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setRefundSplitAmount(new BigDecimal("50.00"));
                splitDetails.add(sub);

                // 模拟原订单存在
                ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
                orderMainDO.setOrderNo(orderNo);
                orderMainDO.setPayAmount(new BigDecimal("100.00")); // 实付 100
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(orderMainDO);

                when(reconRepository.updateReconRefundStatus(anyString(), anyInt(), any(), any())).thenReturn(true);
                when(reconRepository.batchSaveOrderRefundSplitSub(any())).thenReturn(true);

                // 执行测试
                ReconResult result = realtimeReconService.reconRefund(orderNo, refundAmount, refundTime,
                                RefundStatusEnum.SUCCESS,
                                splitDetails);

                // 验证结果
                assertTrue(result.isSuccess());
                assertEquals(200, result.getCode());
                assertEquals("对账成功", result.getMessage());
                assertEquals(orderNo, result.getOrderNo());

                // 验证调用
                verify(reconRepository, times(1)).getOrderMainByOrderNo(orderNo);
                verify(reconRepository, times(1)).updateReconRefundStatus(eq(orderNo),
                                eq(RefundStatusEnum.SUCCESS.getCode()),
                                eq(refundAmount),
                                eq(refundTime));
                verify(reconRepository, times(1)).batchSaveOrderRefundSplitSub(any());
        }

        @Test
        void testReconRefund_RefundAmountTooLarge() {
                // 准备测试数据
                String orderNo = "TEST_REFUND_002";

                BigDecimal refundAmount = new BigDecimal("150.00"); // 退款 > 实付
                LocalDateTime refundTime = LocalDateTime.now();
                List<ReconOrderRefundSplitSubDO> splitDetails = new ArrayList<>();

                // 模拟原订单
                ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
                orderMainDO.setOrderNo(orderNo);
                orderMainDO.setPayAmount(new BigDecimal("100.00"));
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(orderMainDO);

                // 执行测试
                ReconResult result = realtimeReconService.reconRefund(orderNo, refundAmount, refundTime,
                                RefundStatusEnum.SUCCESS,
                                splitDetails);

                // 验证结果
                assertFalse(result.isSuccess());
                assertEquals("退款金额大于实付金额", result.getMessage());

                // 验证异常记录
                verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq("SELF"),
                                eq("退款金额大于实付金额"), eq(4));
        }

        @Test
        void testReconOrder_ProcessingStatus() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_006";
                String merchantId = "MERCHANT_001";
                BigDecimal payAmount = new BigDecimal("100.00");
                BigDecimal platformIncome = new BigDecimal("5.00");
                BigDecimal payFee = new BigDecimal("1.00");

                List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setMerchantId(merchantId);
                sub.setSplitAmount(new BigDecimal("94.00"));
                splitDetails.add(sub);

                // 模拟存储库方法
                when(reconRepository.saveOrderMain(any())).thenReturn(true);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(orderNo, payAmount, platformIncome,
                                payFee, splitDetails, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS,
                                NotifyStatusEnum.PROCESSING);

                // 验证结果
                assertTrue(result.isSuccess());

                // 验证主记录状态为 PENDING
                verify(reconRepository).saveOrderMain(argThat(order -> order.getOrderNo().equals(orderNo) &&
                                order.getReconStatus().equals(ReconStatusEnum.PENDING.getCode())));

                // 验证没有异常记录产生
                verify(exceptionRecordService, never()).recordReconException(any(), any(), any(), anyInt());
        }

        @Test
        void testReconNotifyByMerchantOrder_Success() {
                String orderNo = "TEST_ORDER_007";
                String merchantId = "MCH_888";
                String merchantOrderNo = "MCH_ORDER_999";

                // 模拟反查订单号
                when(reconRepository.findOrderNoByMerchantOrder(merchantId, merchantOrderNo)).thenReturn(orderNo);
                when(reconRepository.isAllSplitSubNotified(orderNo)).thenReturn(true);
                when(reconRepository.updateReconStatus(anyString(), any())).thenReturn(true);

                // 模拟主订单 (retryRecon 内部需要)
                ReconOrderMainDO mainDO = new ReconOrderMainDO();
                mainDO.setOrderNo(orderNo);
                mainDO.setPayAmount(new BigDecimal("100.00"));
                mainDO.setPlatformIncome(BigDecimal.ZERO);
                mainDO.setPayFee(BigDecimal.ZERO);
                mainDO.setReconStatus(ReconStatusEnum.PENDING.getCode());
                mainDO.setPayStatus(PayStatusEnum.SUCCESS.getCode());
                mainDO.setSplitStatus(SplitStatusEnum.SUCCESS.getCode());
                mainDO.setNotifyStatus(NotifyStatusEnum.SUCCESS.getCode());
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(mainDO);

                List<ReconOrderSplitSubDO> subs = new ArrayList<>();
                ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
                sub.setSplitAmount(new BigDecimal("100.00"));
                subs.add(sub);
                when(reconRepository.getOrderSplitSubByOrderNo(orderNo)).thenReturn(subs);

                // 执行测试
                ReconResult result = realtimeReconService.reconNotifyByMerchantOrder(merchantId, merchantOrderNo,
                                "http://test.com", NotifyStatusEnum.SUCCESS, "OK");

                // 验证结果
                assertTrue(result.isSuccess());
                assertEquals(orderNo, result.getOrderNo());

                // 验证存储库逻辑
                verify(reconRepository).findOrderNoByMerchantOrder(merchantId, merchantOrderNo);
                verify(reconRepository).saveNotifyLog(any());
        }

        @Test
        void testReconRefundByMerchantOrder_Success() {
                String orderNo = "TEST_ORDER_008";
                String merchantId = "MCH_888";
                String merchantOrderNo = "MCH_ORDER_999_REF";
                BigDecimal refundAmount = new BigDecimal("50.00");

                // 模拟反查订单号
                when(reconRepository.findOrderNoByMerchantOrder(merchantId, merchantOrderNo)).thenReturn(orderNo);

                // 模拟原订单存在
                ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
                orderMainDO.setOrderNo(orderNo);
                orderMainDO.setPayAmount(new BigDecimal("100.00"));
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(orderMainDO);

                when(reconRepository.updateReconRefundStatus(anyString(), anyInt(), any(), any())).thenReturn(true);

                // 执行测试
                ReconResult result = realtimeReconService.reconRefundByMerchantOrder(merchantId, merchantOrderNo,
                                refundAmount, LocalDateTime.now(), RefundStatusEnum.SUCCESS);

                // 验证结果
                assertTrue(result.isSuccess());
                assertEquals(orderNo, result.getOrderNo());

                // 验证存储库逻辑
                verify(reconRepository).findOrderNoByMerchantOrder(merchantId, merchantOrderNo);
                verify(reconRepository).batchSaveOrderRefundSplitSub(
                                argThat(list -> list.get(0).getMerchantOrderNo().equals(merchantOrderNo)));
        }

}
