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
        assertEquals(new BigDecimal("5"), mainDO.getPlatformIncome()); // 500/100 = 5

        // Test Null
        mainDO.setPayFee(null);
        assertNull(mainDO.getPayFeeFen());

        mainDO.setSplitTotalAmountFen(null);
        assertNull(mainDO.getSplitTotalAmount());
    }

    @Test
    public void testReconOrderSplitSubDO() {
        ReconOrderSplitSubDO subDO = new ReconOrderSplitSubDO();

        // Yuan -> Fen
        subDO.setSplitAmount(new BigDecimal("12.34"));
        assertEquals(1234L, subDO.getSplitAmountFen());

        // Fen -> Yuan
        subDO.setSplitAmountFen(5678L);
        assertEquals(new BigDecimal("56.78"), subDO.getSplitAmount());
    }
}
