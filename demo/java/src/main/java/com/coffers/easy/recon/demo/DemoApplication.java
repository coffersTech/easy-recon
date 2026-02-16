package com.coffers.easy.recon.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.coffers.recon.api.EasyReconApi;
import tech.coffers.recon.api.result.ReconResult;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;
import tech.coffers.recon.entity.ReconOrderRefundSplitSubDO;
import tech.coffers.recon.entity.ReconSummaryDO;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.SplitStatusEnum;
import tech.coffers.recon.api.enums.NotifyStatusEnum;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.api.result.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private EasyReconApi easyReconApi;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Easy Recon SDK 功能演示开始 ===");

        // 生成一个演示用的基础订单号
        String baseOrderNo = "ORD-JAVA-" + System.currentTimeMillis();

        // 场景 1: 同步实时对账
        demoSyncRecon(baseOrderNo);

        // 场景 2: 同步退款对账 (基于场景 1 的订单)
        demoRefundRecon(baseOrderNo);

        // 场景 3: 异步对账演示
        demoAsyncRecon();

        // 场景 4: 异常处理演示 (模拟金额不一致)
        demoExceptionHandling();

        // 场景 5: 手动触发定时核账
        demoTimingRecon();

        // 场景 6: 报表统计与分页查询
        demoReporting();

        // 场景 7: 商户通知状态演变业务场景
        demoNotificationLogic();

        // 场景 8: 专门的通知回调接口演示
        demoNotificationCallbackApi();

        // 场景 9: 多商户分账及逐个通知闭环演示
        demoMultiMerchantClosure();

        // 场景 10: 基于子订单识别的退款对账
        demoRefundBySubRecon();

        // 等待异步任务执行完成
        Thread.sleep(2000);
        System.out.println("\n=== Easy Recon SDK 功能演示结束 ===");
    }

    /**
     * 场景 1: 同步实时对账
     * 演示最基础的订单支付核账，包含多维度金额校验和多方分账对账。
     */
    private void demoSyncRecon(String orderNo) {
        System.out.println("\n--- [场景 1] 同步实时对账 (订单号: " + orderNo + ") ---");

        BigDecimal payAmount = new BigDecimal("300.00");
        BigDecimal platformIncome = BigDecimal.ZERO;
        BigDecimal payFee = BigDecimal.ZERO;

        // 模拟多方分账明细 (直接构建 DO 列表)
        List<ReconOrderSplitSubDO> splitDetails = new ArrayList<>();

        ReconOrderSplitSubDO sub1 = new ReconOrderSplitSubDO();
        sub1.setSubOrderNo(orderNo + "-1");
        sub1.setMerchantId("MCH-SUB-001");
        sub1.setSplitAmount(new BigDecimal("200.00"));
        splitDetails.add(sub1);

        ReconOrderSplitSubDO sub2 = new ReconOrderSplitSubDO();
        sub2.setSubOrderNo(orderNo + "-2");
        sub2.setMerchantId("MCH-SUB-002");
        sub2.setSplitAmount(new BigDecimal("100.00"));
        splitDetails.add(sub2);

        // 调用对账接口 (支付状态、分账状态、通知状态均为 SUCCESS)
        ReconResult result = easyReconApi.reconOrder(
                orderNo, payAmount, platformIncome, payFee, splitDetails,
                PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.SUCCESS);

        if (result.isSuccess()) {
            System.out.println("同步实时对账成功: " + result.getMessage());
        } else {
            System.out.println("同步实时对账失败: " + result.getMessage());
        }
    }

    /**
     * 场景 2: 退款对账
     * 演示订单发生部分退款时的对账处理，自动调整子商户的分账金额。
     */
    private void demoRefundRecon(String orderNo) {
        System.out.println("\n--- [场景 2] 退款对账 ---");

        BigDecimal refundAmount = new BigDecimal("50.00");
        LocalDateTime refundTime = LocalDateTime.now();
        RefundStatusEnum refundStatus = RefundStatusEnum.SUCCESS; // 假设处理成功

        // 模拟退款扣回分账明细 (使用 DO 列表以支持子订单号)
        List<ReconOrderRefundSplitSubDO> refundSplits = new ArrayList<>();
        ReconOrderRefundSplitSubDO sub = new ReconOrderRefundSplitSubDO();
        sub.setSubOrderNo(orderNo + "-1");
        sub.setMerchantId("MCH-SUB-001");
        sub.setRefundSplitAmount(new BigDecimal("50.00"));
        refundSplits.add(sub);

        ReconResult result = easyReconApi.reconRefund(orderNo, refundAmount, refundTime, refundStatus, refundSplits);

        if (result.isSuccess()) {
            System.out.println("退款对账成功，当前订单状态: " + easyReconApi.getReconStatus(orderNo));
        } else {
            System.out.println("退款对账失败: " + result.getMessage());
        }
    }

    /**
     * 场景 3: 异步对账
     * 演示非阻塞式对账，适用于对性能要求较高的实时对账场景。
     */
    private void demoAsyncRecon() {
        System.out.println("\n--- [场景 3] 异步对账演示 ---");
        String asyncOrderNo = "ORD-ASYNC-" + System.currentTimeMillis();

        List<ReconOrderSplitSubDO> splits = new ArrayList<>();
        ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
        sub.setSubOrderNo(asyncOrderNo + "-S1");
        sub.setMerchantId("MCH-001");
        sub.setSplitAmount(new BigDecimal("100.00"));
        splits.add(sub);

        // 先创建一笔成功的订单，再进行异步退款对账演示
        easyReconApi.reconOrder(asyncOrderNo, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                splits, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.SUCCESS);

        List<ReconOrderRefundSplitSubDO> refundSplits = new ArrayList<>();
        ReconOrderRefundSplitSubDO refundSub = new ReconOrderRefundSplitSubDO();
        refundSub.setSubOrderNo(asyncOrderNo + "-S1");
        refundSub.setMerchantId("MCH-001");
        refundSub.setRefundSplitAmount(new BigDecimal("20.00"));
        refundSplits.add(refundSub);

        easyReconApi.reconRefundAsync(asyncOrderNo, new BigDecimal("20.00"), LocalDateTime.now(),
                RefundStatusEnum.SUCCESS, refundSplits)
                .thenAccept(success -> {
                    System.out.println(">>> [异步结果] 订单 " + asyncOrderNo + " 退款对账完成: " + success);
                });
    }

    /**
     * 场景 4: 异常处理演示
     * 故意制造金额不一致的情况，演示 SDK 记录异常明细的能力。
     */
    private void demoExceptionHandling() {
        System.out.println("\n--- [场景 4] 异常处理演示 (模拟金额不匹配) ---");
        String errorOrderNo = "ORD-ERR-" + System.currentTimeMillis();

        BigDecimal payAmount = new BigDecimal("100.00");
        List<ReconOrderSplitSubDO> mismatchSplits = new ArrayList<>();

        ReconOrderSplitSubDO s1 = new ReconOrderSplitSubDO();
        s1.setSubOrderNo(errorOrderNo + "-1");
        s1.setMerchantId("MCH-001");
        s1.setSplitAmount(new BigDecimal("60.00"));
        mismatchSplits.add(s1);

        ReconOrderSplitSubDO s2 = new ReconOrderSplitSubDO();
        s2.setSubOrderNo(errorOrderNo + "-2");
        s2.setMerchantId("MCH-002");
        s2.setSplitAmount(new BigDecimal("30.00")); // 合计为 90，与 100 不平
        mismatchSplits.add(s2);

        ReconResult result = easyReconApi.reconOrder(errorOrderNo, payAmount, BigDecimal.ZERO, BigDecimal.ZERO,
                mismatchSplits, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.SUCCESS);

        System.out.println("对账结果 (预期失败): " + result.getMessage());

        if (!result.isSuccess()) {
            System.out.println("查询该订单的异常明细:");
            easyReconApi.getReconExceptions(errorOrderNo).forEach(ex -> {
                System.out.println(" - 异常步骤: [" + ex.getExceptionStep() + "], 原因: " + ex.getExceptionMsg());
            });
        }
    }

    /**
     * 场景 5: 定时核账触发
     * 演示如何手动触发指定日期的全量离线核账。
     */
    private void demoTimingRecon() {
        System.out.println("\n--- [场景 5] 手动触发定时核账 ---");
        String today = LocalDate.now().toString();

        boolean result = easyReconApi.doTimingRecon(today);
        System.out.println("触发 " + today + " 定时核账任务: " + (result ? "成功" : "失败"));
    }

    /**
     * 场景 6: 报表统计与分页查询
     * 演示 SDK 提供的管理端 API，包括对账汇总快照和明细的分页检索。
     */
    private void demoReporting() {
        System.out.println("\n--- [场景 6] 报表统计与查询演示 ---");
        String today = LocalDate.now().toString();

        // 1. 获取对账汇总快照
        ReconSummaryDO summary = easyReconApi.getReconSummary(today);
        if (summary != null) {
            System.out.println("今日 [" + today + "] 对账汇总:");
            System.out.println(" - 总单数: " + summary.getTotalOrders());
            System.out.println(" - 成功单数: " + summary.getSuccessCount());
            System.out.println(" - 异常单数: " + summary.getFailCount());
            System.out.println(" - 总金额: " + summary.getTotalAmount());
        }

        // 2. 分页查询对账订单明细
        System.out.println("\n分页查询 (第 1 页，每页 10 条):");
        PageResult<ReconOrderMainDO> page = easyReconApi.listOrdersByDate(today, null, 1, 10);

        if (page != null && page.getList() != null) {
            long totalPages = (page.getTotal() + page.getSize() - 1) / page.getSize();
            System.out.println("总记录数: " + page.getTotal() + ", 总页数: " + totalPages);
            page.getList().forEach(order -> {
                System.out.println(" - 订单号: " + order.getOrderNo() + ", 对账状态: " + order.getReconStatus());
            });
        }
    }

    /**
     * 场景 7: 商户通知状态处理
     * 演示当商户通知处于“处理中”时，对账状态会自动标记为 PENDING，直到通知完成后变为最终态。
     */
    private void demoNotificationLogic() {
        System.out.println("\n--- [场景 7] 商户通知状态演变演示 ---");
        String notifyOrderNo = "ORD-NOTIFY-" + System.currentTimeMillis();

        BigDecimal payAmount = new BigDecimal("100.00");
        // 1. 模拟通知处理中 (payStatus=SUCCESS, splitStatus=SUCCESS, notifyStatus=PROCESSING)
        System.out.println("步骤1: 支付成功，但通知回调处理中 (notifyStatus = PROCESSING)...");
        List<ReconOrderSplitSubDO> splits = new ArrayList<>();
        ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
        sub.setSubOrderNo(notifyOrderNo + "-S1");
        sub.setMerchantId("MCH-001");
        sub.setSplitAmount(new BigDecimal("100.00"));
        splits.add(sub);

        easyReconApi.reconOrder(notifyOrderNo, payAmount, BigDecimal.ZERO, BigDecimal.ZERO,
                splits, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.PROCESSING);

        tech.coffers.recon.api.enums.ReconStatusEnum status = easyReconApi.getReconStatus(notifyOrderNo);
        System.out.println("当前对账状态 (预期为 PENDING): " + status);

        // 2. 模拟通知最终成功 (notifyStatus=SUCCESS)
        System.out.println("步骤2: 收到通知成功回调，更新状态...");
        easyReconApi.reconOrder(notifyOrderNo, payAmount, BigDecimal.ZERO, BigDecimal.ZERO,
                splits, PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.SUCCESS);

        status = easyReconApi.getReconStatus(notifyOrderNo);
        System.out.println("最终对账状态 (预期为 SUCCESS): " + status);
    }

    /**
     * 场景 8: 专门的通知回调接口演示
     * 展示如何使用专有的 reconNotify API 更新通知状态并触发对账平滑过渡。
     */
    private void demoNotificationCallbackApi() {
        System.out.println("\n--- [场景 8] 专门的通知回调接口演示 ---");
        String orderNo = "ORD-CB-" + System.currentTimeMillis();

        BigDecimal payAmount = new BigDecimal("200.00");
        List<ReconOrderSplitSubDO> splits = new ArrayList<>();
        ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
        sub.setSubOrderNo(orderNo + "-S1");
        sub.setMerchantId("MCH-001");
        sub.setSplitAmount(new BigDecimal("200.00"));
        splits.add(sub);

        // 1. 提交初始订单，通知状态为 PROCESSING
        System.out.println("1. 提交订单，当前通知状态为 PROCESSING...");
        easyReconApi.reconOrder(orderNo, payAmount, BigDecimal.ZERO, BigDecimal.ZERO, splits,
                PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.PROCESSING);
        System.out.println("初始对账状态: " + easyReconApi.getReconStatus(orderNo));

        // 2. 使用专有接口更新通知结果
        System.out.println("2. 调用 reconNotify 接口更新异步通知结果...");
        ReconResult result = easyReconApi.reconNotify(
                orderNo, "MCH-001", "https://callback.io/api", NotifyStatusEnum.SUCCESS, "SUCCESS");

        System.out.println("通知处理结果: " + (result.isSuccess() ? "成功" : "失败"));
        System.out.println("最终对账状态: " + easyReconApi.getReconStatus(orderNo));
    }

    /**
     * [场景 9] 多商户分账及逐个通知闭环演示
     */
    private void demoMultiMerchantClosure() {
        System.out.println("\n--- [场景 9] 多商户分账及逐个通知闭环演示 ---");
        String orderNo = "ORD-MULTI-" + System.currentTimeMillis();

        BigDecimal payAmount = new BigDecimal("300.00");
        List<ReconOrderSplitSubDO> splits = new ArrayList<>();

        ReconOrderSplitSubDO subA = new ReconOrderSplitSubDO();
        subA.setSubOrderNo(orderNo + "-A");
        subA.setMerchantId("MCH-A");
        subA.setSplitAmount(new BigDecimal("100.00"));
        splits.add(subA);

        ReconOrderSplitSubDO subB = new ReconOrderSplitSubDO();
        subB.setSubOrderNo(orderNo + "-B");
        subB.setMerchantId("MCH-B");
        subB.setSplitAmount(new BigDecimal("200.00"));
        splits.add(subB);

        // 1. 提交初始订单，初始化为 PROCESSING
        // 1. 提交订单 (商户A: 100, 商户B: 200)，当前通知状态为 PROCESSING...
        easyReconApi.reconOrder(orderNo, payAmount, BigDecimal.ZERO, BigDecimal.ZERO, splits,
                PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.PROCESSING);
        System.out.println("初始对账状态 (预期为 PENDING): " + easyReconApi.getReconStatus(orderNo));

        // 2. 更新商户A的通知结果 (使用简化接口，仅凭子订单号识别)
        System.out.println("2. 更新商户A通知成功 (使用 reconNotifyBySub)...");
        easyReconApi.reconNotifyBySub("MCH-A", orderNo + "-A", "https://callback.io/api/A", NotifyStatusEnum.SUCCESS,
                "OK");
        System.out.println("商户A更新后状态 (预期为 PENDING): " + easyReconApi.getReconStatus(orderNo));

        // 3. 更新商户B的通知结果 (使用简化接口，仅凭子订单号识别)
        System.out.println("3. 更新商户B通知成功 (使用 reconNotifyBySub)...");
        easyReconApi.reconNotifyBySub("MCH-B", orderNo + "-B", "https://callback.io/api/B", NotifyStatusEnum.SUCCESS,
                "OK");
        System.out.println("最终对账状态 (预期为 SUCCESS): " + easyReconApi.getReconStatus(orderNo));
    }

    /**
     * [场景 10] 基于子订单识别的退款对账
     * 演示在只知道子订单号和商户号的情况下进行退款对账。
     */
    private void demoRefundBySubRecon() {
        System.out.println("\n--- [场景 10] 基于子订单识别的退款对账 ---");
        String orderNo = "ORD-SUB-REFUND-" + System.currentTimeMillis();
        String subOrderNo = orderNo + "-S1";
        String merchantId = "MCH-REFUND-001";

        // 1. 创建原始订单
        List<ReconOrderSplitSubDO> splits = new ArrayList<>();
        ReconOrderSplitSubDO sub = new ReconOrderSplitSubDO();
        sub.setSubOrderNo(subOrderNo);
        sub.setMerchantId(merchantId);
        sub.setSplitAmount(new BigDecimal("100.00"));
        splits.add(sub);

        easyReconApi.reconOrder(orderNo, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, splits,
                PayStatusEnum.SUCCESS, SplitStatusEnum.SUCCESS, NotifyStatusEnum.SUCCESS);

        // 2. 模拟退款对账 (仅凭 subOrderNo 和 merchantId，不再需要手动构造详情列表)
        System.out.println("执行基于子订单的简化退款对账 (子订单: " + subOrderNo + ", 商户: " + merchantId + ")...");
        ReconResult result = easyReconApi.reconRefundBySub(merchantId, subOrderNo, new BigDecimal("30.00"),
                LocalDateTime.now(), RefundStatusEnum.SUCCESS);

        if (result.isSuccess()) {
            System.out.println("基于子订单的退款对账成功!");
            System.out.println("最终对账状态: " + easyReconApi.getReconStatus(orderNo));
        } else {
            System.out.println("基于子订单的退款对账失败: " + result.getMessage());
        }
    }

}
