package tech.coffers.recon.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import tech.coffers.recon.core.EasyReconTemplate;
import tech.coffers.recon.core.service.AlarmService;
import tech.coffers.recon.core.service.ExceptionRecordService;
import tech.coffers.recon.core.service.RealtimeReconService;
import tech.coffers.recon.core.service.TimingReconService;
import tech.coffers.recon.dialect.ReconDatabaseDialect;
import tech.coffers.recon.dialect.ReconDialectFactory;
import tech.coffers.recon.repository.jdbc.JdbcReconRepository;
import tech.coffers.recon.repository.ReconRepository;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 对账SDK自动配置类
 * <p>
 * 自动配置SDK所需的所有组件，包括服务、存储库等
 * </p>
 *
 * @author Ryan
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ReconSdkProperties.class)
public class ReconSdkAutoConfiguration {

    private final ReconSdkProperties properties;

    public ReconSdkAutoConfiguration(ReconSdkProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建线程池
     *
     * @return 线程池
     */
    @Bean
    @ConditionalOnMissingBean(ExecutorService.class)
    public ExecutorService executorService() {
        int corePoolSize = properties.getThreadPool().getCorePoolSize();
        int maxPoolSize = properties.getThreadPool().getMaxPoolSize();
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 创建数据库方言
     *
     * @param dataSource 数据源
     * @return 数据库方言
     */

    /**
     * 创建数据库方言工厂
     *
     * @param dataSource 数据源
     * @return 数据库方言工厂
     */
    @Bean
    @ConditionalOnMissingBean(ReconDialectFactory.class)
    public ReconDialectFactory reconDialectFactory(DataSource dataSource) {
        return new ReconDialectFactory(dataSource);
    }

    /**
     * 创建数据库方言
     *
     * @param dialectFactory 数据库方言工厂
     * @return 数据库方言
     */
    @Bean
    @ConditionalOnMissingBean(ReconDatabaseDialect.class)
    public ReconDatabaseDialect reconDatabaseDialect(ReconDialectFactory dialectFactory) {
        return dialectFactory.getDialect();
    }

    /**
     * 创建对账存储库
     *
     * @param dataSource     数据源
     * @param dialectFactory 数据库方言工厂
     * @return 对账存储库
     */
    @Bean
    @ConditionalOnMissingBean(ReconRepository.class)
    public ReconRepository reconRepository(DataSource dataSource, ReconDialectFactory dialectFactory) {
        return new JdbcReconRepository(new JdbcTemplate(dataSource), dialectFactory, properties);
    }

    /**
     * 创建告警服务
     *
     * @return 告警服务
     */
    @Bean
    @ConditionalOnMissingBean(AlarmService.class)
    public AlarmService alarmService() {
        String alarmType = properties.getAlarm().getType();
        if ("dingtalk".equals(alarmType)) {
            String webhookUrl = properties.getAlarm().getDingtalk().getWebhookUrl();
            return new AlarmService(new AlarmService.DingTalkAlarmStrategy(webhookUrl));
        } else {
            return new AlarmService(new AlarmService.LogAlarmStrategy());
        }
    }

    /**
     * 创建定时对账服务
     *
     * @param reconRepository 对账存储库
     * @param executorService 线程池
     * @param alarmService    告警服务
     * @return 定时对账服务
     */
    @Bean
    @ConditionalOnMissingBean(TimingReconService.class)
    public TimingReconService timingReconService(ReconRepository reconRepository, ExecutorService executorService,
            AlarmService alarmService) {
        return new TimingReconService(reconRepository, executorService, alarmService);
    }

    /**
     * 创建异常记录服务
     *
     * @param reconRepository 对账存储库
     * @return 异常记录服务
     */
    @Bean
    @ConditionalOnMissingBean(ExceptionRecordService.class)
    public ExceptionRecordService exceptionRecordService(ReconRepository reconRepository) {
        return new ExceptionRecordService(reconRepository);
    }

    /**
     * 创建实时对账服务
     *
     * @param reconRepository        对账存储库
     * @param exceptionRecordService 异常记录服务
     * @param alarmService           告警服务
     * @param properties             配置属性
     * @param executorService        线程池
     * @return 实时对账服务
     */
    @Bean
    @ConditionalOnMissingBean(RealtimeReconService.class)
    public RealtimeReconService realtimeReconService(ReconRepository reconRepository,
            ExceptionRecordService exceptionRecordService, AlarmService alarmService, ReconSdkProperties properties,
            ExecutorService executorService) {
        return new RealtimeReconService(reconRepository, exceptionRecordService, alarmService, properties,
                executorService);
    }

    /**
     * 创建对账模板
     *
     * @param realtimeReconService 实时对账服务
     * @param timingReconService   定时对账服务
     * @return 对账模板
     */
    @Bean
    @ConditionalOnMissingBean(EasyReconTemplate.class)
    public EasyReconTemplate easyReconTemplate(RealtimeReconService realtimeReconService,
            TimingReconService timingReconService) {
        return new EasyReconTemplate(realtimeReconService, timingReconService);
    }

}
