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
import tech.coffers.recon.dialect.PgReconDialect;
import tech.coffers.recon.dialect.MySqlReconDialect;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import tech.coffers.recon.repository.jdbc.JdbcReconRepository;
import tech.coffers.recon.repository.ReconRepository;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Easy Recon SDK Spring Boot 自动配置入口类
 * <p>
 * 该类负责集成并初始化 SDK 的核心组件，包括：
 * <ul>
 * <li>数据库方言 (MySql/PostgreSQL) 自动识别与注入</li>
 * <li>基于 JdbcTemplate 的持久化仓储 (ReconRepository)</li>
 * <li>实时对账核心逻辑 (RealtimeReconService)</li>
 * <li>基于 Spring @Scheduled 的定时对账补偿逻辑 (TimingReconService)</li>
 * <li>多渠道告警服务 (AlarmService)</li>
 * <li>以及基于 Flyway 的 SDK 专用表结构自动初始化</li>
 * </ul>
 *
 * @author coffersTech
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
    @ConditionalOnMissingBean(name = "reconExecutorService")
    public ExecutorService reconExecutorService() {
        int corePoolSize = properties.getThreadPool().getCorePoolSize();
        int maxPoolSize = properties.getThreadPool().getMaxPoolSize();
        int queueCapacity = properties.getThreadPool().getQueueCapacity();
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>(queueCapacity),
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
            @org.springframework.beans.factory.annotation.Qualifier("reconExecutorService") ExecutorService executorService) {
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

    /**
     * 配置 Flyway 迁移路径
     * <p>
     * 根据检测到的数据库类型，动态设置 Flyway 的迁移脚本位置
     * </p>
     */
    @Bean
    @ConditionalOnClass(Flyway.class)
    public FlywayConfigurationCustomizer reconFlywayConfigurationCustomizer(ReconDatabaseDialect dialect) {
        return configuration -> {
            String location;
            if (dialect instanceof PgReconDialect) {
                location = "classpath:easy-recon/migration/postgresql";
            } else if (dialect instanceof MySqlReconDialect) {
                location = "classpath:easy-recon/migration/mysql";
            } else {
                location = "classpath:easy-recon/migration/mysql";
            }

            // 获取现有的 locations 并追加 SDK 的路径
            Location[] currentLocations = configuration.getLocations();
            Location[] newLocations = new Location[currentLocations.length + 1];
            System.arraycopy(currentLocations, 0, newLocations, 0, currentLocations.length);
            newLocations[currentLocations.length] = new Location(location);

            configuration.locations(newLocations);
            configuration.baselineOnMigrate(true);
            configuration.baselineVersion("0");
        };
    }

}
