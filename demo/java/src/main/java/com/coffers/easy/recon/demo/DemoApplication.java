package com.coffers.easy.recon.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.coffers.recon.core.EasyReconTemplate;
import tech.coffers.recon.entity.ReconOrderMainDO;
import tech.coffers.recon.entity.ReconOrderSplitSubDO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private EasyReconTemplate easyReconTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Starting Java Easy Recon SDK Demo ---");

        // Create Mock Data
        ReconOrderMainDO orderMain = new ReconOrderMainDO();
        orderMain.setOrderNo("ORD-JAVA-123456");
        orderMain.setMerchantId("MCH-JAVA-001");
        orderMain.setPayAmount(new BigDecimal("300.00"));
        orderMain.setPlatformIncome(BigDecimal.ZERO);
        orderMain.setPayFee(BigDecimal.ZERO);
        orderMain.setSplitTotalAmount(new BigDecimal("300.00"));
        orderMain.setReconStatus(0);
        orderMain.setCreateTime(java.time.LocalDateTime.now());
        orderMain.setUpdateTime(java.time.LocalDateTime.now());

        ReconOrderSplitSubDO sub1 = new ReconOrderSplitSubDO();
        sub1.setOrderNo("ORD-JAVA-123456"); // Explicitly set if needed
        sub1.setMerchantId("MCH-SUB-001");
        sub1.setSplitAmount(new BigDecimal("200.00"));
        sub1.setCreateTime(java.time.LocalDateTime.now());
        sub1.setUpdateTime(java.time.LocalDateTime.now());

        ReconOrderSplitSubDO sub2 = new ReconOrderSplitSubDO();
        sub2.setOrderNo("ORD-JAVA-123456");
        sub2.setMerchantId("MCH-SUB-002");
        sub2.setSplitAmount(new BigDecimal("100.00"));
        sub2.setCreateTime(java.time.LocalDateTime.now());
        sub2.setUpdateTime(java.time.LocalDateTime.now());

        List<ReconOrderSplitSubDO> splitSubs = new ArrayList<>();
        splitSubs.add(sub1);
        splitSubs.add(sub2);

        // Execute Recon
        try {
            boolean success = easyReconTemplate.doRealtimeRecon(orderMain, splitSubs);
            if (success) {
                System.out.println("--- Recon Successful ---");
            } else {
                System.out.println("--- Recon Failed ---");
            }
        } catch (Exception e) {
            System.err.println("Error executing demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
