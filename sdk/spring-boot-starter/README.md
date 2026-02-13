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

## API 参考

### EasyReconTemplate

`EasyReconTemplate` 是 SDK 的核心入口类，提供了以下主要方法：

#### 1. 实时对账

用于在业务流程中同步触发对账逻辑。

```java
/**
 * 执行实时对账
 *
 * @param orderMainDO 订单主记录（包含支付金额、手续费、平台收入等）
 * @param splitSubDOs 分账子记录列表（可选，用于分账对账）
 * @return 对账结果 (true: 成功, false: 失败)
 */
boolean doRealtimeRecon(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs);
```

**示例：**

```java
ReconOrderMainDO mainDO = new ReconOrderMainDO();
mainDO.setOrderNo("ORD20230101001");
mainDO.setPayAmount(new BigDecimal("100.00"));
// ... 设置其他必要字段

List<ReconOrderSplitSubDO> splitList = new ArrayList<>();
// ... 添加分账记录

boolean success = easyReconTemplate.doRealtimeRecon(mainDO, splitList);
```

#### 2. 异步实时对账

用于不阻塞主业务流程的对账操作。

```java
CompletableFuture<Boolean> doRealtimeReconAsync(ReconOrderMainDO orderMainDO, List<ReconOrderSplitSubDO> splitSubDOs);
```

#### 3. 退款对账

用于处理退款业务的对账。

```java
/**
 * 执行退款对账
 *
 * @param orderNo      原订单号
 * @param refundAmount 退款金额
 * @param refundTime   退款时间
 * @param refundStatus 退款状态 (0=未退款, 1=部分退款, 2=全额退款)
 * @param splitDetails 退款分账详情 (可选)
 * @return 对账结果 (true: 成功, false: 失败)
 */
boolean reconRefund(String orderNo, BigDecimal refundAmount, LocalDateTime refundTime, int refundStatus, Map<String, BigDecimal> splitDetails);
```

#### 4. 异步退款对账

用于不阻塞主业务流程的退款对账操作。

```java
CompletableFuture<Boolean> reconRefundAsync(String orderNo, BigDecimal refundAmount, LocalDateTime refundTime, int refundStatus, Map<String, BigDecimal> splitDetails);
```

#### 5. 定时对账触发

手动触发指定日期的定时对账任务（通常由定时任务自动调用）。

```java
// 触发交易对账
boolean doTimingRecon(String dateStr); // dateStr 格式: yyyy-MM-dd

// 触发退款对账
boolean doTimingRefundRecon(String dateStr);
```

## 使用方法

### 1. 注入 SDK 模板

在您的 Service 或 Component 中注入 `EasyReconTemplate`：

```java
@Autowired
private EasyReconTemplate easyReconTemplate;
```

### 2. 实时对账（同步）

适用于需要立即获取对账结果的场景，例如在支付回调处理完成后。

```java
public void handlePaymentCallback(PaymentNotify notify) {
    // 1. 构建主订单对象
    ReconOrderMainDO mainDO = new ReconOrderMainDO();
    mainDO.setOrderNo(notify.getOrderNo());
    mainDO.setPayAmount(notify.getAmount()); // 使用 BigDecimal
    // ... 设置其他字段

    // 2. 构建分账记录（如果有）
    List<ReconOrderSplitSubDO> splitDOs = new ArrayList<>();
    // ...

    // 3. 执行对账
    boolean success = easyReconTemplate.doRealtimeRecon(mainDO, splitDOs);
    
    if (!success) {
        log.error("订单 {} 对账失败", notify.getOrderNo());
        // 处理失败逻辑
    }
}
```

### 3. 实时对账（异步）

适用于对响应时间敏感的接口，将对账操作放入后台线程池执行。

```java
public void handlePaymentCallbackAsync(PaymentNotify notify) {
    // ... 构建对象 (同上)

    // 执行异步对账
    easyReconTemplate.doRealtimeReconAsync(mainDO, splitDOs)
        .thenAccept(result -> {
            if (result) {
                log.info("异步对账成功");
            } else {
                log.warn("异步对账失败");
            }
        });
}
```

### 4. 退款对账

在退款流程中使用，支持部分退款和全额退款。

```java
public void handleRefund(String orderNo, BigDecimal refundAmount) {
    // 构建分账退款详情 (可选，Key为商户ID，Value为退款分账金额)
    Map<String, BigDecimal> splitDetails = new HashMap<>();
    splitDetails.put("MCH001", new BigDecimal("10.00"));

    // 执行退款对账
    boolean success = easyReconTemplate.reconRefund(
        orderNo, 
        refundAmount, 
        LocalDateTime.now(), 
        1, // 1=部分退款, 2=全额退款
        splitDetails
### 5. 退款对账（异步）

```java
public void handleRefundAsync(String orderNo, BigDecimal refundAmount) {
    // ... 构建参数

    easyReconTemplate.reconRefundAsync(orderNo, refundAmount, LocalDateTime.now(), 1, splitDetails)
        .thenAccept(result -> {
            // 处理异步结果
        });
}
```

## 许可证

Apache License, Version 2.0
