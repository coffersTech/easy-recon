package tech.coffers.recon.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.coffers.recon.api.EasyReconApi;
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
@org.springframework.scheduling.annotation.EnableScheduling
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
     * @param alarmStrategies 所有的告警策略实现
     * @return 告警服务
     */
    @Bean
    @ConditionalOnMissingBean(AlarmService.class)
    public AlarmService alarmService(java.util.List<AlarmService.AlarmStrategy> alarmStrategies) {
        return new AlarmService(alarmStrategies);
    }

    /**
     * 默认日志告警策略
     */
    @Bean
    @ConditionalOnMissingBean(AlarmService.LogAlarmStrategy.class)
    public AlarmService.LogAlarmStrategy logAlarmStrategy() {
        return new AlarmService.LogAlarmStrategy();
    }

    /**
     * 钉钉告警策略 (仅当配置了 webhook 时创建)
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "easy-recon.alarm.dingtalk", name = "webhook-url")
    public AlarmService.DingTalkAlarmStrategy dingTalkAlarmStrategy() {
        return new AlarmService.DingTalkAlarmStrategy(properties.getAlarm().getDingtalk().getWebhookUrl());
    }

    /**
     * 创建定时对账服务
     *
     * @param reconRepository        对账存储库
     * @param exceptionRecordService 异常记录服务
     * @param alarmService           告警服务
     * @param properties             配置属性
     * @return 定时对账服务
     */
    @Bean
    @ConditionalOnMissingBean(TimingReconService.class)
    public TimingReconService timingReconService(ReconRepository reconRepository,
            ExceptionRecordService exceptionRecordService, AlarmService alarmService, ReconSdkProperties properties) {
        return new TimingReconService(reconRepository, exceptionRecordService, alarmService, properties);
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
     * 创建对账 API 入口
     *
     * @param realtimeReconService 实时对账服务
     * @param timingReconService   定时对账服务
     * @param reconRepository      对账存储库
     * @return 对账 API
     */
    @Bean
    @ConditionalOnMissingBean(EasyReconApi.class)
    public EasyReconApi easyReconApi(RealtimeReconService realtimeReconService,
            TimingReconService timingReconService, ReconRepository reconRepository) {
        return new EasyReconApi(realtimeReconService, timingReconService, reconRepository);
    }

}
