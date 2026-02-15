package com.coffers.easy.recon.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.coffers.recon.api.EasyReconApi;
import tech.coffers.recon.api.result.ReconResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private EasyReconApi easyReconApi;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Starting Java Easy Recon SDK Demo ---");

        String orderNo = "ORD-JAVA-" + System.currentTimeMillis();

        // 1. Execute Real-time Recon with all statuses
        System.out.println("--- Starting Real-time Recon for " + orderNo + " ---");

        BigDecimal payAmount = new BigDecimal("300.00");
        BigDecimal platformIncome = BigDecimal.ZERO;
        BigDecimal payFee = BigDecimal.ZERO;

        Map<String, BigDecimal> splitDetails = new HashMap<>();
        splitDetails.put("MCH-SUB-001", new BigDecimal("200.00"));
        splitDetails.put("MCH-SUB-002", new BigDecimal("100.00"));

        // status: 1 (SUCCESS)
        ReconResult result = easyReconApi.reconOrder(
                orderNo,
                payAmount,
                platformIncome,
                payFee,
                splitDetails,
                1, 1, 1);

        if (result.isSuccess()) {
            System.out.println("--- Real-time Recon Successful ---");

            // 2. Simulate Refund Recon
            System.out.println("--- Starting Refund Recon ---");
            BigDecimal refundAmount = new BigDecimal("50.00");
            java.time.LocalDateTime refundTime = java.time.LocalDateTime.now();
            int refundStatus = 1; // Partial Refund

            Map<String, BigDecimal> refundSplitDetails = new HashMap<>();
            refundSplitDetails.put("MCH-SUB-001", new BigDecimal("50.00"));

            boolean refundSuccess = easyReconApi.reconRefund(orderNo, refundAmount, refundTime, refundStatus,
                    refundSplitDetails);

            if (refundSuccess) {
                System.out.println("--- Refund Recon Successful ---");

                // 3. Query Status
                System.out.println("Final Recon Status: " + easyReconApi.getReconStatus(orderNo));
            } else {
                System.out.println("--- Refund Recon Failed ---");
            }

        } else {
            System.out.println("--- Real-time Recon Failed: " + result.getMessage() + " ---");
        }

        System.out.println("--- Java Demo Completion ---");
    }
}
