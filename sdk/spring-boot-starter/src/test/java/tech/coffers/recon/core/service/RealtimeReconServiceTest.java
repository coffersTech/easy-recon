package tech.coffers.recon.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.repository.ReconRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
        when(properties.getAmountTolerance()).thenReturn(0.01);
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
        Map<String, BigDecimal> splitDetails = new HashMap<>();
        splitDetails.put(merchantId, new BigDecimal("94.00"));
        boolean payStatus = true;
        boolean notifyStatus = true;

        // 模拟存储库方法
        when(reconRepository.saveOrderMain(any())).thenReturn(true);
        when(reconRepository.batchSaveOrderSplitSub(any())).thenReturn(true);

        // 执行测试
        ReconResult result = realtimeReconService.reconOrder(orderNo, merchantId, payAmount, platformIncome, payFee,
                splitDetails, payStatus, notifyStatus);

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
        Map<String, BigDecimal> splitDetails = new HashMap<>();
        splitDetails.put(merchantId, new BigDecimal("94.00"));
        boolean payStatus = false; // 支付状态失败
        boolean notifyStatus = true;

        // 执行测试
        ReconResult result = realtimeReconService.reconOrder(orderNo, merchantId, payAmount, platformIncome, payFee,
                splitDetails, payStatus, notifyStatus);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertEquals("支付状态失败，对账失败", result.getMessage());
        assertEquals(orderNo, result.getOrderNo());

        // 验证异常记录和告警被调用
        verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq(merchantId), eq("支付状态失败，对账失败"),
                eq(1));
        verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq(merchantId), eq("支付状态失败，对账失败"));
    }

    @Test
    void testReconOrder_NotifyStatusFailure() {
        // 准备测试数据
        String orderNo = "TEST_ORDER_003";
        String merchantId = "MERCHANT_001";
        BigDecimal payAmount = new BigDecimal("100.00");
        BigDecimal platformIncome = new BigDecimal("5.00");
        BigDecimal payFee = new BigDecimal("1.00");
        Map<String, BigDecimal> splitDetails = new HashMap<>();
        splitDetails.put(merchantId, new BigDecimal("94.00"));
        boolean payStatus = true;
        boolean notifyStatus = false; // 通知状态失败

        // 执行测试
        ReconResult result = realtimeReconService.reconOrder(orderNo, merchantId, payAmount, platformIncome, payFee,
                splitDetails, payStatus, notifyStatus);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertEquals("通知状态失败，对账失败", result.getMessage());
        assertEquals(orderNo, result.getOrderNo());

        // 验证异常记录和告警被调用
        verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq(merchantId), eq("通知状态失败，对账失败"),
                eq(3));
        verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq(merchantId), eq("通知状态失败，对账失败"));
    }

    @Test
    void testReconOrder_AmountCheckFailure() {
        // 准备测试数据
        String orderNo = "TEST_ORDER_004";
        String merchantId = "MERCHANT_001";
        BigDecimal payAmount = new BigDecimal("100.00");
        BigDecimal platformIncome = new BigDecimal("5.00");
        BigDecimal payFee = new BigDecimal("1.00");
        Map<String, BigDecimal> splitDetails = new HashMap<>();
        splitDetails.put(merchantId, new BigDecimal("90.00")); // 分账金额错误，应该是 94.00
        boolean payStatus = true;
        boolean notifyStatus = true;

        // 执行测试
        ReconResult result = realtimeReconService.reconOrder(orderNo, merchantId, payAmount, platformIncome, payFee,
                splitDetails, payStatus, notifyStatus);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertEquals("金额校验失败，实付金额与计算金额不一致", result.getMessage());
        assertEquals(orderNo, result.getOrderNo());

        // 验证异常记录和告警被调用
        verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq(merchantId),
                eq("金额校验失败，实付金额与计算金额不一致"), eq(4));
        verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq(merchantId), eq("金额校验失败，实付金额与计算金额不一致"));
    }

    @Test
    void testReconOrder_Exception() {
        // 准备测试数据
        String orderNo = "TEST_ORDER_005";
        String merchantId = "MERCHANT_001";
        BigDecimal payAmount = new BigDecimal("100.00");
        BigDecimal platformIncome = new BigDecimal("5.00");
        BigDecimal payFee = new BigDecimal("1.00");
        Map<String, BigDecimal> splitDetails = new HashMap<>();
        splitDetails.put(merchantId, new BigDecimal("94.00"));
        boolean payStatus = true;
        boolean notifyStatus = true;

        // 模拟存储库方法抛出异常
        when(reconRepository.saveOrderMain(any())).thenThrow(new RuntimeException("数据库操作失败"));

        // 执行测试
        ReconResult result = realtimeReconService.reconOrder(orderNo, merchantId, payAmount, platformIncome, payFee,
                splitDetails, payStatus, notifyStatus);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("对账处理异常"));
        assertEquals(orderNo, result.getOrderNo());

        // 验证异常记录和告警被调用
        verify(exceptionRecordService, times(1)).recordReconException(eq(orderNo), eq(merchantId), anyString(), eq(5));
        verify(alarmService, times(1)).sendReconAlarm(eq(orderNo), eq(merchantId), anyString());
    }

}
