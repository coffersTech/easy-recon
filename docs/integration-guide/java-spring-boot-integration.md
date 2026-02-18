# Java Spring Boot 集成指南

## 环境要求

- JDK 11 或更高版本
- Spring Boot 2.7.x
- Maven 3.6 或更高版本
- MySQL 5.7+ 或 PostgreSQL 10+

## 集成步骤

### 1. 添加依赖

在 `pom.xml` 文件中添加 Easy Recon SDK 依赖：

```xml
<dependency>
    <groupId>tech.coffers</groupId>
    <artifactId>easy-recon-sdk</artifactId>
    <version>1.0.1</version>
</dependency>

<!-- 数据库驱动依赖 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.30</version>
</dependency>

<!-- 或 PostgreSQL 驱动 -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.5.0</version>
</dependency>

<!-- Flyway 数据库迁移 -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>8.5.13</version>
</dependency>
```

### 2. 配置数据源

在 `application.yml` 文件中配置数据库连接和 SDK 相关配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easy_recon
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  flyway:
    enabled: true
    baseline-on-migrate: true

easy-recon:
  enabled: true
  amount-tolerance: 0.01
  batch-size: 1000
  timing-cron: 0 0 2 * * ?
  alarm:
    type: log
    dingtalk:
      webhook-url: https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
  thread-pool:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 1000
  config-secret-key: your-encryption-key
```

### 3. 启用 SDK

在 Spring Boot 启动类上添加 `@EnableEasyRecon` 注解：

```java
@SpringBootApplication
@EnableEasyRecon
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. 使用 SDK

#### 4.1 注入 EasyReconApi

```java
@Autowired
private EasyReconApi easyReconApi;
```

#### 4.2 执行实时对账

```java
public boolean doRealtimeRecon() {
    // 创建订单主记录
    ReconOrderMainDO orderMain = new ReconOrderMainDO();
    orderMain.setOrderNo("ORDER_" + System.currentTimeMillis());
    orderMain.setMerchantName("测试商户");
    orderMain.setOrderAmount(new BigDecimal(100.00));
    orderMain.setActualAmount(new BigDecimal(100.00));
    orderMain.setReconStatus(0); // 待对账
    orderMain.setOrderTime(new Date());
    orderMain.setPayTime(new Date());
    orderMain.setCreateTime(new Date());
    orderMain.setUpdateTime(new Date());

    // 创建分账子记录
    List<ReconOrderSplitSubDO> splitSubs = new ArrayList<>();
    ReconOrderSplitSubDO sub1 = new ReconOrderSplitSubDO();
    sub1.setOrderNo(orderMain.getOrderNo());
    sub1.setSubOrderNo("SUB_" + System.currentTimeMillis());
    sub1.setMerchantId("MERCHANT_001");
    sub1.setSplitAmount(new BigDecimal(80.00));
    sub1.setStatus(0); // 待处理
    sub1.setCreateTime(new Date());
    sub1.setUpdateTime(new Date());
    splitSubs.add(sub1);

    // 执行对账
    return easyReconApi.doRealtimeRecon(orderMain, splitSubs);
}
```

#### 4.3 执行定时对账

```java
public boolean doTimingRecon() {
    String dateStr = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return easyReconApi.doTimingRecon(dateStr);
}
```

#### 4.4 异步对账

```java
public CompletableFuture<Boolean> doReconAsync() {
    // 创建订单主记录和分账子记录（代码省略）
    
    // 异步执行对账
    return easyReconApi.doRealtimeReconAsync(orderMain, splitSubs);
}

// 处理异步结果
public void handleAsyncResult() {
    doReconAsync().thenAccept(success -> {
        if (success) {
            System.out.println("对账成功");
        } else {
            System.out.println("对账失败");
        }
    }).exceptionally(ex -> {
        System.err.println("对账异常: " + ex.getMessage());
        return false;
    });
}
```

#### 4.5 退款对账 (New)

```java
public void processRefund(String orderNo, BigDecimal refundAmount) {
    // 1. 构建退款请求
    LocalDateTime refundTime = LocalDateTime.now();
    RefundStatusEnum status = RefundStatusEnum.SUCCESS;
    
    // 2. 构建退款分账明细 (可选，如果涉及分账退回)
    List<ReconOrderRefundSplitSubDO> splitDetails = new ArrayList<>();
    // ... 添加明细
    
    // 3. 执行退款对账
    ReconResult result = easyReconApi.reconRefund(orderNo, refundAmount, refundTime, status, splitDetails);
    
    if (result.isSuccess()) {
        log.info("退款对账成功");
    } else {
        log.error("退款对账失败: {}", result.getMessage());
    }
}
```

#### 4.6 通知回调处理 (New)

```java
@PostMapping("/pay/notify")
public String handlePayNotify(@RequestBody PayNotifyDTO notify) {
    // 1. 解析通知内容
    String orderNo = notify.getOrderNo();
    NotifyStatusEnum status = notify.isSuccess() ? NotifyStatusEnum.SUCCESS : NotifyStatusEnum.FAILURE;
    
    // 2. 调用 SDK 处理通知
    // merchantId传入 SELF 表示主订单通知，传入具体商户ID表示子订单通知
    ReconResult result = easyReconApi.reconNotify(orderNo, "SELF", "http://notify.url", status, JSON.toJSONString(notify));
    
    return "success";
}
```

#### 4.7 数据查询 (New)

```java
public void queryReconData(String orderNo) {
    // 查询对账状态
    ReconStatusEnum status = easyReconApi.getReconStatus(orderNo);
    
    // 查询主订单详情
    ReconOrderMainDO orderMain = easyReconApi.getOrderMain(orderNo);
    
    // 查询异常记录
    List<ReconExceptionDO> exceptions = easyReconApi.getReconExceptions(orderNo);
}
```

### 5. 配置告警

#### 5.1 使用日志告警

```yaml
eas-recon:
  alarm:
    type: log
```

#### 5.2 使用钉钉告警

```yaml
eas-recon:
  alarm:
    type: dingtalk
    dingtalk:
      webhook-url: https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

### 6. 自定义告警策略

```java
@Component
public class CustomAlarmStrategy implements AlarmStrategy {
    @Override
    public void alarm(ReconExceptionDO exception) {
        // 自定义告警逻辑
        System.out.println("自定义告警: " + exception.getExceptionMsg());
        // 可以集成其他告警方式，如邮件、短信等
    }
}

// 在配置类中使用自定义告警策略
@Configuration
public class AlarmConfig {
    @Bean
    public AlarmService alarmService(CustomAlarmStrategy customAlarmStrategy) {
        return new AlarmService(customAlarmStrategy);
    }
}
```

## 高级配置

### 1. 线程池配置

```yaml
eas-recon:
  thread-pool:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 1000
    keep-alive-seconds: 60
```

### 2. 定时任务配置

```yaml
eas-recon:
  timing-cron: 0 0 2 * * ?  # 每天凌晨 2 点执行
```

### 3. 配置加密

```yaml
eas-recon:
  config-secret-key: your-encryption-key
```

### 4. 数据库方言配置

SDK 会根据数据库连接自动检测方言，无需手动配置。

## 监控与维护

### 1. 健康检查

Spring Boot Actuator 可以监控应用健康状态：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. 日志配置

在 `application.yml` 中配置日志：

```yaml
logging:
  level:
    tech.coffers.easyrecon: info
  file:
    name: logs/easy-recon.log
```

### 3. 性能监控

集成 Prometheus 和 Grafana 监控系统性能：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## 部署

### 1. 打包部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/your-application.jar
```

### 2. Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/your-application.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-jar", "app.jar"]
```

构建并运行 Docker 容器：

```bash
# 构建镜像
docker build -t easy-recon-app .

# 运行容器
docker run -d --name easy-recon \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/easy_recon \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  easy-recon-app
```

## 常见问题与解决方案

### 1. 启动时自动建表失败

**原因**：数据库连接失败或权限不足

**解决方案**：
- 检查数据库服务是否启动
- 验证数据库连接信息是否正确
- 确保数据库用户有创建表的权限

### 2. 对账结果不准确

**原因**：数据格式错误或金额不匹配

**解决方案**：
- 检查订单数据格式是否正确
- 验证金额计算是否准确
- 调整金额容差配置

### 3. 性能问题

**原因**：线程池配置不合理或数据库性能不足

**解决方案**：
- 调整线程池配置
- 优化数据库索引
- 增加数据库连接池大小

### 4. 告警不触发

**原因**：告警配置错误或网络问题

**解决方案**：
- 检查告警配置是否正确
- 验证网络连接是否正常
- 测试告警通道是否可用

## 最佳实践

1. **使用连接池**：配置合理的数据库连接池大小
2. **异步处理**：对于非关键路径，使用异步对账提高性能
3. **监控告警**：配置合适的告警方式，及时发现异常
4. **定期维护**：定期清理过期数据，优化数据库性能
5. **性能测试**：在生产环境部署前进行性能测试
6. **备份数据**：定期备份数据库，防止数据丢失

## 示例项目

完整的示例项目代码可参考：
- GitHub: https://github.com/example/easy-recon-sdk-examples
