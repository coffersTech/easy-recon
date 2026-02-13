package tech.coffers.recon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ReconOrderSplitSubDOTest {

    @Test
    public void testSplitAmountSync() {
        ReconOrderSplitSubDO splitSub = new ReconOrderSplitSubDO();
        splitSub.setSplitAmount(new BigDecimal("20.00"));
        assertEquals(2000L, splitSub.getSplitAmountFen());

        splitSub.setSplitAmount(null);
        assertNull(splitSub.getSplitAmountFen());
    }
}
