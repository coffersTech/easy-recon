package tech.coffers.recon.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import tech.coffers.recon.autoconfigure.ReconSdkProperties;
import tech.coffers.recon.dialect.MySqlReconDialect;
import tech.coffers.recon.dialect.ReconDialectFactory;
import tech.coffers.recon.entity.ReconOrderMainDO;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JdbcReconRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ReconDialectFactory dialectFactory;

    @Mock
    private ReconSdkProperties properties;

    @Mock
    private PreparedStatement preparedStatement;

    private JdbcReconRepository repository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getTablePrefix()).thenReturn("easy_recon_");
        when(dialectFactory.getDialect()).thenReturn(new MySqlReconDialect());
        repository = new JdbcReconRepository(jdbcTemplate, dialectFactory, properties);
    }

    @Test
    public void testSaveOrderMain_SetsFenFields() throws SQLException {
        ReconOrderMainDO orderMain = new ReconOrderMainDO();
        orderMain.setOrderNo("ORD001");
        orderMain.setMerchantId("MCH001");
        orderMain.setPayAmount(new BigDecimal("100.00")); // 10000 Fen

        when(jdbcTemplate.update(anyString(), any(PreparedStatementSetter.class))).thenAnswer(invocation -> {
            PreparedStatementSetter setter = invocation.getArgument(1);
            setter.setValues(preparedStatement);
            return 1;
        });

        repository.saveOrderMain(orderMain);

        verify(preparedStatement).setObject(4, 10000L); // payAmountFen at index 4
    }
}
