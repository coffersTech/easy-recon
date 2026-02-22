package tech.coffers.recon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DualAmountTest {

    @Test
    public void testReconOrderMainDO() {
        ReconOrderMainDO mainDO = new ReconOrderMainDO();

        // Test Yuan -> Fen
        mainDO.setPayAmount(new BigDecimal("100.00"));
        assertEquals(10000L, mainDO.getPayAmountFen());

        // Test Fen -> Yuan
        mainDO.setPlatformIncomeFen(500L);
        assertEquals(new BigDecimal("5.00"), mainDO.getPlatformIncome()); // 500/100 = 5.00

        // Test Null
        mainDO.setPayFee(null);
        assertNull(mainDO.getPayFeeFen());

        mainDO.setSplitTotalAmountFen(null);
        assertNull(mainDO.getSplitTotalAmount());
    }

    @Test
    public void testReconOrderSplitDetailDO() {
        ReconOrderSplitDetailDO detailDO = new ReconOrderSplitDetailDO();

        // Yuan -> Fen
        detailDO.setSplitAmount(new BigDecimal("12.34"));
        assertEquals(1234L, detailDO.getSplitAmountFen());

        // Fen -> Yuan
        detailDO.setSplitAmountFen(5678L);
        assertEquals(new BigDecimal("56.78"), detailDO.getSplitAmount());
    }

    @Test
    public void testReconOrderSubDO() {
        ReconOrderSubDO subDO = new ReconOrderSubDO();

        // orderAmount Yuan -> Fen
        subDO.setOrderAmount(new BigDecimal("100.00"));
        assertEquals(10000L, subDO.getOrderAmountFen());

        // splitAmount Fen -> Yuan
        subDO.setSplitAmountFen(9000L);
        assertEquals(new BigDecimal("90.00"), subDO.getSplitAmount());

        // fee Yuan -> Fen
        subDO.setFee(new BigDecimal("1.50"));
        assertEquals(150L, subDO.getFeeFen());
    }
}
