# Easy Recon SDK used for Spring Boot

Easy Recon SDK 是一个专为 Spring Boot 应用设计的轻量级、高效对账框架。它通过提供内置的订单存储、状态追踪和自动化对账规则支持，简化了财务数据的对账流程。

## 特性

*   **双单位金额存储**：支持同时以 元（BigDecimal）和 分（Long）存储金额，确保金额的精度和一致性。
*   **自动化对账**：支持实时对账和定时（基于 Cron 表达式）批量对账。
*   **灵活的数据存储**：兼容 MySQL 和 PostgreSQL 数据库。
*   **自定义规则**：支持通过易读的表达式定义自定义对账规则。
*   **告警集成**：内置钉钉告警和日志告警支持。
*   **可扩展**：支持扩展方言和存储库实现。

## 安装

在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>tech.coffers</groupId>
    <artifactId>easy-recon-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 配置

在 `application.yml` 或 `application.properties` 中配置 SDK：

```yaml
easy-recon:
  enabled: true # 启用或禁用 SDK
  amount-tolerance: 0.01 # 金额比较容差
  batch-size: 1000 # 定时核账批次大小
  timing-cron: "0 0 2 * * ?" # 定时核账 Cron 表达式（默认每天凌晨 2 点）
  table-prefix: "easy_recon_" # SDK 表前缀
  
  thread-pool:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 1000

  alarm:
    type: dingtalk # 或 "log"
    dingtalk:
      webhook-url: "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN"
```

## 数据库 Schema

SDK 需要特定的数据库表才能正常工作。迁移脚本位于 `src/main/resources/db/migration`：

*   MySQL: `V1__easy_recon_tables_mysql.sql`
*   PostgreSQL: `V1__easy_recon_tables_pg.sql`

请确保使用适合您数据库类型的脚本初始化数据库。

### 核心表
*   `easy_recon_order_main`: 对账订单主记录表。
*   `easy_recon_order_split_sub`: 分账金额子记录表。
*   `easy_recon_order_refund_split_sub`: 退款分账金额子记录表。
*   `easy_recon_exception`: 对账异常记录表。
*   `easy_recon_notify_log`: 通知日志表。
*   `easy_recon_rule`: 对账、状态和自定义规则表。

## 使用方法

注入 `EasyReconTemplate` 以使用对账服务：

```java
@Autowired
private EasyReconTemplate easyReconTemplate;

public void processRecon() {
    // 执行实时对账
    easyReconTemplate.getRealtimeReconService().recon(orderNo);
}
```

## 许可证

Apache License, Version 2.0
