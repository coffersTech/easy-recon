package tech.coffers.recon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ReconOrderRefundSplitSubDOTest {

    @Test
    public void testRefundSplitAmountSync() {
        ReconOrderRefundSplitSubDO refundSplitSub = new ReconOrderRefundSplitSubDO();
        refundSplitSub.setRefundSplitAmount(new BigDecimal("10.50"));
        assertEquals(1050L, refundSplitSub.getRefundSplitAmountFen());

        refundSplitSub.setRefundSplitAmount(null);
        assertNull(refundSplitSub.getRefundSplitAmountFen());
    }
}
