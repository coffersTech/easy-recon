package tech.coffers.recon.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderMerchantSettlementDO;
import tech.coffers.recon.entity.ReconOrderSplitDetailDO;
import org.mockito.ArgumentCaptor;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.enums.SettlementTypeEnum;
import tech.coffers.recon.api.model.ReconNotifyRequest;
import tech.coffers.recon.api.model.ReconOrderFenRequest;
import tech.coffers.recon.api.model.ReconOrderRequest;
import tech.coffers.recon.api.model.ReconRefundRequest;
import tech.coffers.recon.api.model.ReconOrderSplitRequest;
import tech.coffers.recon.api.model.ReconSubOrderRequest;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 实时对账服务单元测试 (DTO 模式)
 *
 * @author Ryan
 * @since 1.1.0
 */
class RealtimeReconServiceTest {

        @Mock
        private ReconRepository reconRepository;

        @Mock
        private ExceptionRecordService exceptionRecordService;

        @Mock
        private AlarmService alarmService;

        @Mock
        private java.util.concurrent.ExecutorService executorService;

        private RealtimeReconService realtimeReconService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                realtimeReconService = new RealtimeReconService(reconRepository, exceptionRecordService, alarmService,
                                executorService);
        }

        @Test
        void testReconOrder_Success() {
                // 准备测试数据
                String orderNo = "TEST_ORDER_001";
                // 业务子订单 (意图)
                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                subOrders.add(ReconSubOrderRequest.builder()
                                .subOrderNo(orderNo + "-S1")
                                .merchantId("MERCHANT_001")
                                .splitAmount(new BigDecimal("94.00"))
                                .build());

                // 支付分账明细 (事实)
                List<ReconOrderSplitRequest> splitDetails = new ArrayList<>();
                splitDetails.add(ReconOrderSplitRequest.builder()
                                .merchantId("MERCHANT_001")
                                .splitAmount(new BigDecimal("94.00"))
                                .build());

                ReconOrderRequest request = ReconOrderRequest.builder()
                                .orderNo(orderNo)
                                .payAmount(new BigDecimal("100.00"))
                                .platformIncome(new BigDecimal("5.00"))
                                .payFee(new BigDecimal("1.00"))
                                .subOrders(subOrders)
                                .splitDetails(splitDetails)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                // 模拟存储库方法
                when(reconRepository.saveOrderMain(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSub(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSplitDetail(any())).thenReturn(true);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(request);

                // 验证结果
                assertTrue(result.isSuccess());
                assertEquals(orderNo, result.getOrderNo());
                verify(reconRepository, times(1)).saveOrderMain(any());
                verify(reconRepository, times(1)).batchSaveOrderSub(any());
                verify(reconRepository, times(1)).batchSaveOrderSplitDetail(any());
        }

        @Test
        void testReconOrder_FenSubUnits_Success() {
                // 准备测试数据 (全部使用分单位)
                String orderNo = "ORDER_FEN_SUB_001";
                // 业务子订单 (分)
                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                subOrders.add(ReconSubOrderRequest.builder()
                                .subOrderNo(orderNo + "-S1")
                                .merchantId("MERCHANT_888")
                                .splitAmountFen(8900L) // 89.00 元
                                .build());

                // 支付分账明细 (分)
                List<ReconOrderSplitRequest> splitDetails = new ArrayList<>();
                splitDetails.add(ReconOrderSplitRequest.builder()
                                .merchantId("MERCHANT_888")
                                .splitAmountFen(8900L)
                                .arrivalAmountFen(8850L) // 88.50 元
                                .splitFeeFen(50L) // 0.50 元
                                .build());

                ReconOrderRequest request = ReconOrderRequest.builder()
                                .orderNo(orderNo)
                                .payAmount(new BigDecimal("100.00"))
                                .platformIncome(new BigDecimal("10.00"))
                                .payFee(new BigDecimal("1.00"))
                                .subOrders(subOrders)
                                .splitDetails(splitDetails)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                when(reconRepository.saveOrderMain(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSub(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSplitDetail(any())).thenReturn(true);

                // 执行测试
                ReconResult result = realtimeReconService.reconOrder(request);

                // 验证
                assertTrue(result.isSuccess());
                // 这里校验 DTO 的 Getter 是否正确将分转换回了元，以便原有的 BigDecimal 校验逻辑工作
                assertEquals(new BigDecimal("89.00"), request.getSubOrders().get(0).getSplitAmount());
        }

        @Test
        void testReconOrder_PayStatusFailure() {
                String orderNo = "TEST_ORDER_002";
                ReconOrderRequest request = ReconOrderRequest.builder()
                                .orderNo(orderNo)
                                .payAmount(new BigDecimal("100.00"))
                                .payStatus(PayStatusEnum.FAILURE)
                                .build();

                ReconResult result = realtimeReconService.reconOrder(request);

                assertFalse(result.isSuccess());
                assertEquals("支付状态失败，对账失败", result.getMessage());
                verify(exceptionRecordService).recordReconException(eq(orderNo), eq("SELF"), contains("支付状态失败"), eq(1));
        }

        @Test
        void testReconOrder_AmountCheckFailure() {
                String orderNo = "TEST_ORDER_004";
                List<ReconOrderSplitRequest> splitDetails = new ArrayList<>();
                ReconOrderSplitRequest sub = ReconOrderSplitRequest.builder()
                                .splitAmount(new BigDecimal("90.00")) // Error amount
                                .arrivalAmount(new BigDecimal("90.00"))
                                .splitFee(BigDecimal.ZERO)
                                .build();
                splitDetails.add(sub);

                ReconOrderRequest request = ReconOrderRequest.builder()
                                .orderNo(orderNo)
                                .payAmount(new BigDecimal("100.00"))
                                .platformIncome(new BigDecimal("5.00"))
                                .payFee(new BigDecimal("1.00"))
                                .splitDetails(splitDetails)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                ReconResult result = realtimeReconService.reconOrder(request);

                assertFalse(result.isSuccess());
                assertTrue(result.getMessage().contains("金额校验不符"));
                verify(exceptionRecordService).recordReconException(eq(orderNo), eq("SELF"), contains("金额校验不符"), eq(4));
        }

        @Test
        void testReconOrder_DirectToMerchant_Success() {
                String orderNo = "ORDER_DTM_001";
                List<ReconOrderSplitRequest> splitDetails = new ArrayList<>();
                ReconOrderSplitRequest sub = ReconOrderSplitRequest.builder()
                                .splitAmount(new BigDecimal("99.40"))
                                .arrivalAmount(new BigDecimal("98.00"))
                                .splitFee(new BigDecimal("1.40"))
                                .build();
                splitDetails.add(sub);

                ReconOrderRequest request = ReconOrderRequest.builder()
                                .orderNo(orderNo)
                                .payAmount(new BigDecimal("100.00"))
                                .payFee(new BigDecimal("0.60"))
                                .splitDetails(splitDetails)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                when(reconRepository.saveOrderMain(any())).thenReturn(true);

                ReconResult result = realtimeReconService.reconOrder(request);

                assertTrue(result.isSuccess());
        }

        @Test
        void testReconOrder_FenUnit_Success() {
                String orderNo = "ORDER_FEN_001";
                // 使用专门的 FenRequest 实现类
                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(10000L) // 100.00 元
                                .platformIncomeFen(10000L) // 100.00 元
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                // 核心验证: 自动同步逻辑
                assertEquals(new BigDecimal("100.00"), request.getPayAmount());
                assertEquals(new BigDecimal("100.00"), request.getPlatformIncome());

                when(reconRepository.saveOrderMain(any())).thenReturn(true);
                ReconResult result = realtimeReconService.reconOrder(request);
                assertTrue(result.isSuccess());
        }

        @Test
        void testReconRefund_Success() {
                String orderNo = "TEST_REFUND_001";
                ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
                orderMainDO.setOrderNo(orderNo);
                orderMainDO.setPayAmount(new BigDecimal("100.00"));
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(orderMainDO);
                when(reconRepository.updateReconRefundStatus(any(ReconOrderMainDO.class))).thenReturn(true);

                ReconRefundRequest request = ReconRefundRequest.builder()
                                .orderNo(orderNo)
                                .refundAmount(new BigDecimal("50.00"))
                                .refundStatus(RefundStatusEnum.SUCCESS)
                                .refundTime(LocalDateTime.now())
                                .build();

                ReconResult result = realtimeReconService.reconRefund(request);

                assertTrue(result.isSuccess());
                verify(reconRepository).updateReconRefundStatus(any(ReconOrderMainDO.class));
        }

        @Test
        void testReconNotify_Success() {
                String orderNo = "TEST_ORDER_007";
                String merchantId = "MCH_888";

                when(reconRepository.findOrderNoBySub(eq(merchantId), anyString())).thenReturn(orderNo);
                when(reconRepository.isAllSplitSubNotified(orderNo)).thenReturn(true);

                // Mock retryRecon internal calls
                ReconOrderMainDO mainDO = new ReconOrderMainDO();
                mainDO.setOrderNo(orderNo);
                mainDO.setPayAmount(new BigDecimal("100.00"));
                mainDO.setReconStatus(ReconStatusEnum.PENDING.getCode());
                mainDO.setPayStatus(PayStatusEnum.SUCCESS.getCode());
                mainDO.setSplitStatus(SplitStatusEnum.SUCCESS.getCode());
                mainDO.setNotifyStatus(NotifyStatusEnum.SUCCESS.getCode());
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(mainDO);

                List<ReconOrderSplitDetailDO> subs = new ArrayList<>();
                ReconOrderSplitDetailDO sub = new ReconOrderSplitDetailDO();
                sub.setSplitAmount(new BigDecimal("100.00"));
                subs.add(sub);
                when(reconRepository.getOrderSplitDetailByOrderNo(orderNo)).thenReturn(subs);
                when(reconRepository.updateReconStatus(anyString(), any())).thenReturn(true);

                ReconNotifyRequest request = ReconNotifyRequest.builder()
                                .merchantId(merchantId)
                                .subOrderNo("SUB_001")
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .notifyResult("OK")
                                .build();

                ReconResult result = realtimeReconService.reconNotify(request);

                assertTrue(result.isSuccess());
                verify(reconRepository).saveNotifyLog(any());
        }

        @Test
        void testReconRefund_ByMerchantOrder_Success() {
                String orderNo = "TEST_ORDER_008";
                String merchantId = "MCH_888";
                String merchantOrderNo = "MCH_ORDER_999_REF";

                when(reconRepository.findOrderNoByMerchantOrder(merchantId, merchantOrderNo)).thenReturn(orderNo);
                ReconOrderMainDO orderMainDO = new ReconOrderMainDO();
                orderMainDO.setOrderNo(orderNo);
                orderMainDO.setPayAmount(new BigDecimal("100.00"));
                when(reconRepository.getOrderMainByOrderNo(orderNo)).thenReturn(orderMainDO);
                when(reconRepository.updateReconRefundStatus(any(ReconOrderMainDO.class))).thenReturn(true);

                ReconRefundRequest request = ReconRefundRequest.builder()
                                .merchantId(merchantId)
                                .merchantOrderNo(merchantOrderNo)
                                .refundAmount(new BigDecimal("50.00"))
                                .refundStatus(RefundStatusEnum.SUCCESS)
                                .refundTime(LocalDateTime.now())
                                .build();

                ReconResult result = realtimeReconService.reconRefund(request);

                assertTrue(result.isSuccess());
                verify(reconRepository).findOrderNoByMerchantOrder(merchantId, merchantOrderNo);
        }

        @Test
        void testReconOrder_MerchantSettlementScenarios() {
                String orderNo = "ORDER_MCH_SCENARIOS_001";

                // 1. 意图层 (SubOrders)
                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                // 商户 A：全额到账意图
                subOrders.add(ReconSubOrderRequest.builder().merchantId("MCH_A").orderAmountFen(5000L)
                                .splitAmountFen(5000L).build());
                // 商户 B：空中分账意图
                subOrders.add(ReconSubOrderRequest.builder().merchantId("MCH_B").orderAmountFen(4000L)
                                .splitAmountFen(3800L).build());
                // 商户 C：平台代收意图 (在分账事实中缺失)
                subOrders.add(ReconSubOrderRequest.builder().merchantId("MCH_C").orderAmountFen(1000L)
                                .splitAmountFen(1000L).build());

                // 2. 事实层 (SplitDetails)
                List<ReconOrderSplitRequest> splitDetails = new ArrayList<>();
                // 商户 A：事实金额 = 意图金额 -> DIRECT_TO_MERCHANT
                splitDetails.add(ReconOrderSplitRequest.builder().merchantId("MCH_A").splitAmountFen(5000L)
                                .arrivalAmountFen(4950L).splitFeeFen(50L).build());
                // 商户 B：事实金额 < 意图金额 -> REALTIME_SPLIT
                splitDetails.add(ReconOrderSplitRequest.builder().merchantId("MCH_B").splitAmountFen(3800L)
                                .arrivalAmountFen(3700L).splitFeeFen(100L).build());
                // 商户 D：事实中有但意图中无 -> PLATFORM_COLLECTION (典型为平台账号)
                splitDetails.add(ReconOrderSplitRequest.builder().merchantId("MCH_PLATFORM").splitAmountFen(1200L)
                                .arrivalAmountFen(1200L).build());

                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(10200L)
                                .platformIncomeFen(200L) // 增加平台留存，使总额(10200) = 平台(200) + 分账(10000)
                                .subOrders(subOrders)
                                .splitDetails(splitDetails)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                when(reconRepository.saveOrderMain(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSub(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSplitDetail(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderMerchantSettlement(any())).thenReturn(true);

                // 执行对账
                realtimeReconService.reconOrder(request);

                // 核心捕捉：验证商户统计数据
                @SuppressWarnings("unchecked")
                ArgumentCaptor<List<ReconOrderMerchantSettlementDO>> captor = ArgumentCaptor.forClass(List.class);
                verify(reconRepository).batchSaveOrderMerchantSettlement(captor.capture());

                List<ReconOrderMerchantSettlementDO> settlements = captor.getValue();
                assertEquals(4, settlements.size()); // A, B, C, PLATFORM

                // 校验 A: DIRECT_TO_MERCHANT
                ReconOrderMerchantSettlementDO settleA = settlements.stream()
                                .filter(s -> "MCH_A".equals(s.getMerchantId())).findFirst().get();
                assertEquals(SettlementTypeEnum.DIRECT_TO_MERCHANT, settleA.getSettlementType());
                assertEquals(5000L, settleA.getArrivalAmountFen() + settleA.getSplitFeeFen());

                // 校验 B: REALTIME_SPLIT
                ReconOrderMerchantSettlementDO settleB = settlements.stream()
                                .filter(s -> "MCH_B".equals(s.getMerchantId())).findFirst().get();
                assertEquals(SettlementTypeEnum.REALTIME_SPLIT, settleB.getSettlementType());
                assertTrue(settleB.getOrderAmountFen() > settleB.getSplitAmountFen());

                // 校验 C: PLATFORM_COLLECTION (因为事实中缺失)
                ReconOrderMerchantSettlementDO settleC = settlements.stream()
                                .filter(s -> "MCH_C".equals(s.getMerchantId())).findFirst().get();
                assertEquals(SettlementTypeEnum.PLATFORM_COLLECTION, settleC.getSettlementType());

                // 校验 PLATFORM: PLATFORM_COLLECTION (因为意图中缺失)
                ReconOrderMerchantSettlementDO settleP = settlements.stream()
                                .filter(s -> "MCH_PLATFORM".equals(s.getMerchantId())).findFirst().get();
                assertEquals(SettlementTypeEnum.PLATFORM_COLLECTION, settleP.getSettlementType());
        }

        @Test
        void testReconOrder_DirectToMerchant_Rule2_Success() {
                // 模拟用户报告的场景：全额到账(Rule 2)，事实金额 = 支付金额 = 10000
                // 意向中 7000 分给商户，2958 平台留存，42 手续费
                String orderNo = "ORD-RULE2-001";
                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                subOrders.add(ReconSubOrderRequest.builder()
                                .merchantId("MCH-DEMO")
                                .orderAmountFen(5000L).splitAmountFen(3500L).feeFen(21L).build());
                subOrders.add(ReconSubOrderRequest.builder()
                                .merchantId("MCH-DEMO")
                                .orderAmountFen(5000L).splitAmountFen(3500L).feeFen(21L).build());

                List<ReconOrderSplitRequest> splitDetails = new ArrayList<>();
                splitDetails.add(ReconOrderSplitRequest.builder()
                                .merchantId("MCH-DEMO")
                                .splitAmountFen(10000L) // 事实事实收金额 = 10000 (Gross)
                                .arrivalAmountFen(9958L)
                                .splitFeeFen(42L)
                                .build());

                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(10000L)
                                .platformIncomeFen(2958L)
                                .payFeeFen(42L)
                                .subOrders(subOrders)
                                .splitDetails(splitDetails)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .splitStatus(SplitStatusEnum.SUCCESS)
                                .notifyStatus(NotifyStatusEnum.SUCCESS)
                                .build();

                when(reconRepository.saveOrderMain(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSub(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderSplitDetail(any())).thenReturn(true);
                when(reconRepository.batchSaveOrderMerchantSettlement(any())).thenReturn(true);

                ReconResult result = realtimeReconService.reconOrder(request);

                assertTrue(result.isSuccess(),
                                "Rule 2 (Direct to Merchant) with residual profit should pass macro check");
        }
}
