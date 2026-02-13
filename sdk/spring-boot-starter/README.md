# Easy Recon SDK for Spring Boot

Easy Recon SDK is a lightweight, efficient reconciliation framework designed for Spring Boot applications. It simplifies the process of reconciling financial data by providing built-in support for order storage, status tracking, and automated reconciliation rules.

## Features

*   **Dual-Unit Amount Storage**: Stores monetary values in both Yuan (Decimal) and Fen (Long) to ensure precision and consistency.
*   **Automated Reconciliation**: Supports both real-time and timing (cron-based) reconciliation.
*   **Flexible Data Storage**: compatible with MySQL and PostgreSQL.
*   **Customizable Rules**: Define your own reconciliation rules using easy-to-read expressions.
*   **Alarm Integration**: Built-in support for DingTalk alarms and logging.
*   **Extensible**: Easily extendable dialects and repository implementations.

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>tech.coffers</groupId>
    <artifactId>easy-recon-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration

Configure the SDK in your `application.yml` or `application.properties`:

```yaml
easy-recon:
  enabled: true # Enable or disable the SDK
  amount-tolerance: 0.01 # Tolerance for amount comparison
  batch-size: 1000 # Batch size for timing reconciliation
  timing-cron: "0 0 2 * * ?" # Cron expression for timing reconciliation
  table-prefix: "easy_recon_" # Prefix for SDK tables
  
  thread-pool:
    core-pool-size: 10
    max-pool-size: 20
    queue-capacity: 1000

  alarm:
    type: dingtalk # or "log"
    dingtalk:
      webhook-url: "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN"
```

## Database Schema

The SDK requires specific database tables to function. Migration scripts are provided in `src/main/resources/db/migration`:

*   MySQL: `V1__easy_recon_tables_mysql.sql`
*   PostgreSQL: `V1__easy_recon_tables_pg.sql`

Ensure you initialize your database with the appropriate script for your database type.

### Key Tables
*   `easy_recon_order_main`: Main order records.
*   `easy_recon_order_split_sub`: Split amount sub-records.
*   `easy_recon_order_refund_split_sub`: Refund split amount sub-records.
*   `easy_recon_exception`: Reconciliation exception records.
*   `easy_recon_notify_log`: Notification logs.
*   `easy_recon_rule`: Reconciliation, status, and custom rules.

## Usage

Inject `EasyReconTemplate` to use the reconciliation services:

```java
@Autowired
private EasyReconTemplate easyReconTemplate;

public void processRecon() {
    // Perform real-time reconciliation
    easyReconTemplate.getRealtimeReconService().recon(orderNo);
}
```

## License

Apache License, Version 2.0
