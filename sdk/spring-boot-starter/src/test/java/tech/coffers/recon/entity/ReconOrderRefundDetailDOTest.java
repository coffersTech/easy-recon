package tech.coffers.recon.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 退款事实明细实体测试
 */
public class ReconOrderRefundDetailDOTest {

    @Test
    public void testRefundSplitAmountSync() {
        ReconOrderRefundDetailDO refundDetail = new ReconOrderRefundDetailDO();
        refundDetail.setRefundSplitAmount(new BigDecimal("10.50"));
        assertEquals(1050L, refundDetail.getRefundSplitAmountFen());

        refundDetail.setRefundSplitAmount(null);
        assertNull(refundDetail.getRefundSplitAmountFen());
    }
}
