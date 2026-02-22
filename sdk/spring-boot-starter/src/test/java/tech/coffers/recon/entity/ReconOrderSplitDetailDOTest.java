package tech.coffers.recon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 分账事实明细实体测试
 */
public class ReconOrderSplitDetailDOTest {

    @Test
    public void testSplitAmountSync() {
        ReconOrderSplitDetailDO splitDetail = new ReconOrderSplitDetailDO();
        splitDetail.setSplitAmount(new BigDecimal("20.00"));
        assertEquals(2000L, splitDetail.getSplitAmountFen());

        splitDetail.setSplitAmount(null);
        assertNull(splitDetail.getSplitAmountFen());
    }

    @Test
    public void testArrivalAmountSync() {
        ReconOrderSplitDetailDO splitDetail = new ReconOrderSplitDetailDO();
        splitDetail.setArrivalAmount(new BigDecimal("19.50"));
        assertEquals(1950L, splitDetail.getArrivalAmountFen());

        splitDetail.setArrivalAmountFen(1500L);
        assertEquals(new BigDecimal("15.00"), splitDetail.getArrivalAmount());
    }
}
