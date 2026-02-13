package tech.coffers.recon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ReconOrderMainDOTest {

    @Test
    public void testPayAmountSync() {
        ReconOrderMainDO orderMain = new ReconOrderMainDO();
        orderMain.setPayAmount(new BigDecimal("100.00"));
        assertEquals(10000L, orderMain.getPayAmountFen());

        orderMain.setPayAmount(null);
        assertNull(orderMain.getPayAmountFen());
    }

    @Test
    public void testPlatformIncomeSync() {
        ReconOrderMainDO orderMain = new ReconOrderMainDO();
        orderMain.setPlatformIncome(new BigDecimal("1.50"));
        assertEquals(150L, orderMain.getPlatformIncomeFen());
    }

    @Test
    public void testRefundAmountSync() {
        ReconOrderMainDO orderMain = new ReconOrderMainDO();
        orderMain.setRefundAmount(new BigDecimal("50.25"));
        assertEquals(5025L, orderMain.getRefundAmountFen());
    }
}
