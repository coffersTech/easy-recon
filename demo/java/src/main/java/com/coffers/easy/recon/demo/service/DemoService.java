package com.coffers.easy.recon.demo.service;

import org.springframework.stereotype.Service;
import tech.coffers.recon.api.EasyReconApi;
import tech.coffers.recon.api.enums.PayStatusEnum;
import tech.coffers.recon.api.enums.RefundStatusEnum;
import tech.coffers.recon.api.model.ReconOrderFenRequest;
import tech.coffers.recon.api.model.ReconOrderRequest;
import tech.coffers.recon.api.model.ReconRefundRequest;
import tech.coffers.recon.api.model.ReconOrderSplitRequest;
import tech.coffers.recon.api.model.ReconRefundSplitRequest;
import tech.coffers.recon.api.model.ReconSubOrderRequest;
import tech.coffers.recon.api.model.ReconSplitRuleRequest;
import com.coffers.easy.recon.demo.model.DemoGenRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 演示数据生成服务
 */
@Service
public class DemoService {

        private final EasyReconApi easyReconApi;

        public DemoService(EasyReconApi easyReconApi) {
                this.easyReconApi = easyReconApi;
        }

        /**
         * 运行指定演示场景 (带参数)
         */
        public void runScenario(DemoGenRequest request) {
                String type = request.getType();
                try {
                        switch (type) {
                                case "inference":
                                        // [场景 1] 基础结算推断
                                        generateSettlementTypeInference(request);
                                        break;
                                case "platform":
                                        // [场景 2] 平台代收
                                        generatePlatformCollection(request);
                                        break;
                                case "air_split":
                                        // [场景 3] 空中分账 (多商户实时分账)
                                        generateAirSplit(request);
                                        break;
                                case "refund":
                                        // [场景 4] 精准退款链
                                        generatePreciseRefundFlow(request);
                                        break;
                                case "async":
                                        // [场景 5] 异步流程
                                        generateAsyncFlow(request);
                                        break;
                                case "exception":
                                        // [场景 6] 异常处理
                                        generateExceptionHandling(request);
                                        break;
                                case "direct":
                                        // [场景 7] 全额到账商户
                                        generateDirectToMerchant(request);
                                        break;
                                case "all":
                                default:
                                        runAllScenarios();
                                        break;
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        /**
         * 运行所有演示场景，生成种子数据
         */
        public void runAllScenarios() {
                try {
                        DemoGenRequest req = new DemoGenRequest();
                        // [场景 1] 基础结算推断 (直连 vs 分账)
                        generateSettlementTypeInference("ORD-DIRECT", 10000L, 10000L, "MCH-A");
                        generateSettlementTypeInference("ORD-SPLIT", 10000L, 9000L, "MCH-B");

                        // [场景 2] 平台代收逻辑演示 (多商户意图 -> 事实归集到平台)
                        generatePlatformCollection(req);

                        // [场景 3] 复杂多商户空中分账对账
                        generateAirSplit(req);

                        // [场景 4] 精准退款链验证 (基于子订单)
                        generatePreciseRefundFlow(req);

                        // [场景 5] 异步流程演示
                        generateAsyncFlow(req);

                        // [场景 6] 异常处理（金额与推断冲突）
                        generateExceptionHandling(req);

                        // [场景 7] 全额到账商户
                        generateDirectToMerchant(req);

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private void generateSettlementTypeInference(DemoGenRequest req) {
                // 默认值
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-GEN";
                Long payAmtFen = req.getPayAmount() != null
                                ? req.getPayAmount().multiply(new BigDecimal(100)).longValue()
                                : 10000L;

                Integer splitRatio = req.getSplitRatio() != null ? req.getSplitRatio() : 10000;
                // 到账金额(分账基数) = 支付金额 * 比例
                Long subOrderAmtFen = payAmtFen * splitRatio / 10000;
                String mchId = req.getMerchantIds() != null && !req.getMerchantIds().isEmpty() ? req.getMerchantIds()
                                : "MCH-GEN";

                doGenerateSettlementTypeInference(prefix, payAmtFen, subOrderAmtFen, mchId,
                                splitRatio);
        }

        private void generateSettlementTypeInference(String prefix, Long payAmtFen, Long subOrderAmtFen,
                        String mchId) {
                doGenerateSettlementTypeInference(prefix, payAmtFen, subOrderAmtFen, mchId, null);
        }

        private void doGenerateSettlementTypeInference(String prefix, Long payAmtFen, Long subOrderAmtFen,
                        String mchId, Integer splitRatio) {
                String orderNo = prefix + "-" + System.currentTimeMillis();

                // 支付手续费 0.6%
                long payFeeFen = (long) (payAmtFen * 0.006);
                // 实际到账 = 分账金额 - 手续费
                long arrivalAmtFen = subOrderAmtFen - payFeeFen;

                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                subOrders.add(ReconSubOrderRequest.builder().merchantId(mchId)
                                .subOrderNo(orderNo + "-S1")
                                .merchantOrderNo("M-DEMO-" + System.currentTimeMillis() + "-1")
                                .orderAmountFen(payAmtFen)
                                .feeFen(payFeeFen).build());

                List<ReconOrderSplitRequest> facts = new ArrayList<>();
                facts.add(ReconOrderSplitRequest.builder().merchantId(mchId).splitAmountFen(subOrderAmtFen)
                                .arrivalAmountFen(arrivalAmtFen).splitFeeFen(payFeeFen).build());

                List<ReconSplitRuleRequest> splitRules = new ArrayList<>();
                if (splitRatio != null) {
                        splitRules.add(ReconSplitRuleRequest.builder()
                                        .merchantId(mchId)
                                        .ratio(splitRatio)
                                        .build());
                }

                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(payAmtFen)
                                .payFeeFen(payFeeFen)
                                .subOrders(subOrders)
                                .splitDetails(facts)
                                .splitRules(splitRules)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .build();

                easyReconApi.reconOrder(request);
        }

        /**
         * 平台代收
         * 
         * @param req
         */
        private void generatePlatformCollection(DemoGenRequest req) {
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-PLATFORM";
                Long payAmtFen = req.getPayAmount() != null
                                ? req.getPayAmount().multiply(new BigDecimal(100)).longValue()
                                : 10000L;

                // 解析商户号（支持动态数量）
                String merchantStr = req.getMerchantIds() != null && !req.getMerchantIds().isEmpty()
                                ? req.getMerchantIds()
                                : "MCH-DEMO";
                String[] mchIds = merchantStr.split(",");
                int subCountPerMch = req.getSplitCount() != null && req.getSplitCount() > 0 ? req.getSplitCount() : 1;
                int totalSubOrders = mchIds.length * subCountPerMch;

                long perSubAmt = payAmtFen / totalSubOrders;
                String orderNo = prefix + "-" + System.currentTimeMillis();

                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                List<ReconSplitRuleRequest> splitRules = new ArrayList<>();

                Integer ratio = req.getSplitRatio() != null ? req.getSplitRatio() : 7500;
                int index = 0;
                for (String mchIdRaw : mchIds) {
                        String currentMchId = mchIdRaw.trim();
                        splitRules.add(ReconSplitRuleRequest.builder()
                                        .merchantId(currentMchId)
                                        .ratio(ratio)
                                        .build());

                        for (int i = 0; i < subCountPerMch; i++) {
                                long amt = (index == totalSubOrders - 1) ? (payAmtFen - perSubAmt * index) : perSubAmt;
                                subOrders.add(ReconSubOrderRequest.builder()
                                                .merchantId(currentMchId)
                                                .subOrderNo(orderNo + "-S" + (index + 1))
                                                .merchantOrderNo("M-ORD-" + System.currentTimeMillis() + "-"
                                                                + (index + 1))
                                                .orderAmountFen(amt)
                                                .build());
                                index++;
                        }
                }

                List<ReconOrderSplitRequest> facts = new ArrayList<>();
                long totalFeeFen = payAmtFen * 6 / 1000;
                facts.add(ReconOrderSplitRequest.builder()
                                .merchantId("MCH-PLATFORM-RECEIVER") // Different merchant ID for platform
                                                                     // collection fact
                                .splitAmountFen(payAmtFen)
                                .arrivalAmountFen(payAmtFen - totalFeeFen)
                                .splitFeeFen(totalFeeFen)
                                .build());

                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(payAmtFen)
                                .subOrders(subOrders)
                                .splitDetails(facts)
                                .splitRules(splitRules)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .build();

                try {
                        System.out.println("========== [PLATFORM COLLECTION] ==========");
                        System.out.println("Input DemoGenRequest -> payAmount: " + req.getPayAmount()
                                        + ", splitRatio: " + req.getSplitRatio()
                                        + ", merchantIds: " + req.getMerchantIds());
                        System.out.println("Generated API Request -> \n"
                                        + new com.fasterxml.jackson.databind.ObjectMapper()
                                                        .writerWithDefaultPrettyPrinter().writeValueAsString(request));
                        System.out.println("===========================================");
                } catch (Exception e) {
                        e.printStackTrace();
                }

                easyReconApi.reconOrder(request);
        }

        /**
         * 全额到账商户
         * 
         * @param req
         */
        private void generateDirectToMerchant(DemoGenRequest req) {
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-DIRECT-MCH";
                String orderNo = prefix + "-" + System.currentTimeMillis();

                Long payAmtFen = req.getPayAmount() != null
                                ? req.getPayAmount().multiply(new BigDecimal(100)).longValue()
                                : 10000L;

                String merchantStr = req.getMerchantIds() != null && !req.getMerchantIds().isEmpty()
                                ? req.getMerchantIds()
                                : "MCH-DIRECT-DEMO";
                String[] mchIds = merchantStr.split(",");
                int subCountPerMch = req.getSplitCount() != null && req.getSplitCount() > 0 ? req.getSplitCount() : 1;
                int totalSubOrders = mchIds.length * subCountPerMch;

                // 计算渠道支付层手续费 0.6%
                long payFeeFen = payAmtFen * 6 / 1000;

                Integer splitRatio = req.getSplitRatio() != null ? req.getSplitRatio() : 10000;
                long totalIntentSplitAmtFen = payAmtFen * splitRatio / 10000;
                long totalIntentFeeFen = payFeeFen * splitRatio / 10000;

                long perSubOrderAmtFen = payAmtFen / totalSubOrders;
                long perIntentSplitAmtFen = totalIntentSplitAmtFen / totalSubOrders;
                long perIntentFeeFen = totalIntentFeeFen / totalSubOrders;

                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                List<ReconSplitRuleRequest> splitRules = new ArrayList<>();
                List<ReconOrderSplitRequest> facts = new ArrayList<>();

                int index = 0;
                for (String mchIdRaw : mchIds) {
                        String currentMchId = mchIdRaw.trim();
                        splitRules.add(ReconSplitRuleRequest.builder()
                                        .merchantId(currentMchId)
                                        .ratio(splitRatio)
                                        .build());

                        long mchTotalAmt = 0;
                        long mchTotalFee = 0;
                        for (int i = 0; i < subCountPerMch; i++) {
                                // Deal with exact remainder on the last iteration if needed
                                long currentOrderAmt = (index == totalSubOrders - 1)
                                                ? (payAmtFen - perSubOrderAmtFen * index)
                                                : perSubOrderAmtFen;
                                long currentIntentSplitAmt = (index == totalSubOrders - 1)
                                                ? (totalIntentSplitAmtFen - perIntentSplitAmtFen * index)
                                                : perIntentSplitAmtFen;
                                long currentIntentFee = (index == totalSubOrders - 1)
                                                ? (totalIntentFeeFen - perIntentFeeFen * index)
                                                : perIntentFeeFen;

                                subOrders.add(ReconSubOrderRequest.builder()
                                                .merchantId(currentMchId)
                                                .subOrderNo(orderNo + "-S" + (index + 1))
                                                .merchantOrderNo("M-DIR-" + System.currentTimeMillis() + "-" + index)
                                                .orderAmountFen(currentOrderAmt)
                                                .splitAmountFen(currentIntentSplitAmt)
                                                .feeFen(currentIntentFee)
                                                .build());
                                mchTotalAmt += currentOrderAmt;
                                mchTotalFee += currentIntentFee;
                                index++;
                        }

                        facts.add(ReconOrderSplitRequest.builder()
                                        .merchantId(currentMchId)
                                        .splitAmountFen(mchTotalAmt)
                                        .arrivalAmountFen(mchTotalAmt - mchTotalFee)
                                        .splitFeeFen(mchTotalFee)
                                        .build());
                }

                // 全额直连商户，意图与事实一致，且没有平台截留
                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(payAmtFen)
                                .subOrders(subOrders)
                                .splitDetails(facts)
                                .splitRules(splitRules)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .build();

                try {
                        System.out.println("========== [DIRECT TO MERCHANT] ==========");
                        System.out.println("Generated API Request -> \n"
                                        + new com.fasterxml.jackson.databind.ObjectMapper()
                                                        .writerWithDefaultPrettyPrinter().writeValueAsString(request));
                        System.out.println("===========================================");
                } catch (Exception e) {
                        e.printStackTrace();
                }
                easyReconApi.reconOrder(request);
        }

        /**
         * 空中分账 (实时分账)
         * 
         * @param req
         */
        private void generateAirSplit(DemoGenRequest req) {
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-AIR";
                String orderNo = prefix + "-" + System.currentTimeMillis();

                Long payAmtFen = req.getPayAmount() != null
                                ? req.getPayAmount().multiply(new BigDecimal(100)).longValue()
                                : 10000L;

                int subCountPerMch = req.getSplitCount() != null && req.getSplitCount() > 0 ? req.getSplitCount() : 3;

                String merchantStr = req.getMerchantIds() != null && !req.getMerchantIds().isEmpty()
                                ? req.getMerchantIds()
                                : "MCH-AIR";
                String[] mchIds = merchantStr.split(",");
                int totalSubOrders = mchIds.length * subCountPerMch;

                long perMchAmt = payAmtFen / totalSubOrders;
                // Use input splitRatio if provided, default to 10000 (100%)
                Integer ratio = req.getSplitRatio() != null ? req.getSplitRatio() : 10000;
                long totalIntentFeeFen = payAmtFen * 6 / 1000;
                long perFeeFen = totalIntentFeeFen / totalSubOrders;

                List<ReconSubOrderRequest> subOrders = new ArrayList<>();
                List<ReconSplitRuleRequest> splitRules = new ArrayList<>();
                List<ReconOrderSplitRequest> facts = new ArrayList<>();

                int index = 0;
                for (String mchIdRaw : mchIds) {
                        String currentMchId = mchIdRaw.trim();
                        splitRules.add(ReconSplitRuleRequest.builder().merchantId(currentMchId).ratio(ratio).build());

                        long mchTotalSplitAmt = 0;
                        long mchTotalFee = 0;

                        for (int i = 0; i < subCountPerMch; i++) {
                                long amt = (index == totalSubOrders - 1) ? (payAmtFen - perMchAmt * index) : perMchAmt;

                                // The merchant receives the full intended sub-order amount natively
                                long fee = (index == totalSubOrders - 1) ? (totalIntentFeeFen - perFeeFen * index)
                                                : perFeeFen;

                                long intentionalSplitAmt = amt * ratio / 10000;
                                long intentionalFee = fee * ratio / 10000;

                                subOrders.add(ReconSubOrderRequest.builder()
                                                .merchantId(currentMchId)
                                                .subOrderNo(orderNo + "-S" + (index + 1))
                                                .merchantOrderNo("M-AIR-" + System.currentTimeMillis() + "-"
                                                                + (index + 1))
                                                .orderAmountFen(amt)
                                                // 意图核算层：按业务规则比例计算应分金额与手续费
                                                .splitAmountFen(intentionalSplitAmt)
                                                .feeFen(intentionalFee)
                                                .build());

                                // If ratio is less than 100% (e.g. 70%), the 'inferred intent' will be less
                                // than the 'fact amount' (100%), leading to a reconciliation Exception.
                                // This demonstrates a case where the merchant receives more than expected.
                                mchTotalSplitAmt += amt;
                                mchTotalFee += fee;
                                index++;
                        }

                        // Facts represent the actual settlement data arriving at the merchant (100%
                        // arrival)
                        facts.add(ReconOrderSplitRequest.builder().merchantId(currentMchId)
                                        .splitAmountFen(mchTotalSplitAmt)
                                        .splitFeeFen(mchTotalFee)
                                        .arrivalAmountFen(mchTotalSplitAmt - mchTotalFee).build());
                }

                ReconOrderFenRequest request = ReconOrderFenRequest.builder()
                                .orderNo(orderNo)
                                .payAmountFen(payAmtFen)
                                .subOrders(subOrders)
                                .splitDetails(facts)
                                .splitRules(splitRules)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .build();

                try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        System.out.println("========== [AIR SPLIT / REALTIME SPLIT] ==========");
                        System.out.println("Input Parameters -> \n"
                                        + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req));
                        System.out.println("Generated API Request -> \n"
                                        + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
                        System.out.println("==================================================");
                } catch (Exception e) {
                        e.printStackTrace();
                }

                easyReconApi.reconOrder(request);
        }

        private void generatePreciseRefundFlow(DemoGenRequest req) {
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-REFUND-BASE";
                String orderNo = prefix + "-" + System.currentTimeMillis();
                String subOrderNo = orderNo + "-S1";

                Long payAmtFen = req.getPayAmount() != null
                                ? req.getPayAmount().multiply(new BigDecimal(100)).longValue()
                                : 10000L;

                List<ReconSubOrderRequest> subs = new ArrayList<>();
                List<ReconSplitRuleRequest> splitRules = new ArrayList<>();
                subs.add(ReconSubOrderRequest.builder().subOrderNo(subOrderNo).merchantId("MCH-A")
                                .merchantOrderNo("M-REF-" + System.currentTimeMillis() + "-1")
                                .orderAmountFen(payAmtFen)
                                .build());

                Integer ratio = req.getSplitRatio() != null ? req.getSplitRatio() : 10000;
                splitRules.add(ReconSplitRuleRequest.builder()
                                .merchantId("MCH-A").ratio(ratio).build());

                easyReconApi.reconOrder(ReconOrderFenRequest.builder()
                                .orderNo(orderNo).payAmountFen(payAmtFen).subOrders(subs).splitRules(splitRules)
                                .payStatus(PayStatusEnum.SUCCESS)
                                .build());

                List<ReconRefundSplitRequest> refundFacts = new ArrayList<>();
                refundFacts.add(ReconRefundSplitRequest.builder().merchantId("MCH-A").refundSplitAmountFen(4000L)
                                .build());

                ReconRefundRequest refundReq = ReconRefundRequest.builder()
                                .orderNo(orderNo)
                                .subOrderNo(subOrderNo)
                                .refundAmount(new BigDecimal("40.00"))
                                .splitDetails(refundFacts)
                                .refundStatus(RefundStatusEnum.SUCCESS)
                                .build();

                easyReconApi.reconRefund(refundReq);
        }

        private void generateAsyncFlow(DemoGenRequest req) {
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-ASYNC";
                String orderNo = prefix + "-" + System.currentTimeMillis();
                Long payAmtFen = req.getPayAmount() != null
                                ? req.getPayAmount().multiply(new BigDecimal(100)).longValue()
                                : 10000L;
                easyReconApi.reconOrderAsync(ReconOrderFenRequest.builder()
                                .orderNo(orderNo).payAmountFen(payAmtFen).payStatus(PayStatusEnum.SUCCESS).build());
        }

        private void generateExceptionHandling(DemoGenRequest req) {
                String prefix = req.getOrderPrefix() != null && !req.getOrderPrefix().isEmpty() ? req.getOrderPrefix()
                                : "ORD-ERR";
                String errorOrderNo = prefix + "-" + System.currentTimeMillis();

                BigDecimal payAmt = req.getPayAmount() != null ? req.getPayAmount() : new BigDecimal("100.00");

                ReconOrderRequest request = ReconOrderRequest.builder()
                                .orderNo(errorOrderNo).payAmount(payAmt)
                                .payStatus(PayStatusEnum.SUCCESS).build();

                easyReconApi.reconOrder(request);
        }
}
