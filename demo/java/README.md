# Easy Recon Java Demo

Easy Recon Java SDK 的基于 Spring Boot 的演示程序，包含 11 个完整的对账场景。

## 前置条件

- JDK 17+
- Maven 3.6+
- PostgreSQL 或 MySQL 数据库

## 运行

### 1. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改 `spring.datasource` 配置：

```yaml
spring:
  datasource:
    # PostgreSQL 配置
    url: jdbc:postgresql://localhost:5432/easy_recon_demo
    username: postgresql
    password: postgresql
    driverClassName: org.postgresql.Driver

    # MySQL 配置示例
    # url: jdbc:mysql://localhost:3306/easy_recon_demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    # driverClassName: com.mysql.cj.jdbc.Driver
    # username: root
    # password: password
```

### 2. 运行 Demo

```bash
mvn spring-boot:run
```

## 演示场景

Demo 启动后会自动依次执行以下 11 个场景：

| # | 场景 | 说明 |
|---|------|------|
| 1 | **同步实时对账** | 演示最基础的订单支付核账，包含多维度金额校验和多方分账对账。 |
| 2 | **退款对账** | 演示订单发生部分退款时的对账处理，自动调整子商户的分账金额。 |
| 3 | **异步对账** | 演示非阻塞式对账（使用 `CompletableFuture`），适用于高并发场景。 |
| 4 | **异常处理** | 模拟金额不一致，演示 SDK 如何记录异常步骤和原因。 |
| 5 | **定时核账** | 手动触发指定日期的全量离线核账任务。 |
| 6 | **报表统计** | 演示对账汇总快照查询和明细分页检索。 |
| 7 | **通知状态演变** | 演示商户通知处理中（PENDING）到处理完成（SUCCESS）的状态自动流转。 |
| 8 | **回调接口演练** | 演示使用 `reconNotify` 专有接口更新通知状态。 |
| 9 | **多商户闭环** | 演示多商户分账场景下，逐个更新子商户通知状态，最终完成主订单闭环。 |
| 10 | **子订单退款** | 演示仅凭子订单号（subOrderNo）和商户号进行退款对账。 |
| 11 | **原始单号对账** | 演示基于商户原始订单号（merchantOrderNo）的全链路对账（核账、通知、退款）。 |

## 预期输出

控制台将输出每个场景的执行日志，例如：

```
=== Easy Recon SDK 功能演示开始 ===

--- [场景 1] 同步实时对账 (订单号: ORD-JAVA-170...) ---
同步实时对账成功: 成功

--- [场景 4] 异常处理演示 (模拟金额不匹配) ---
对账结果 (预期失败): 金额校验失败，实付金额与计算金额不一致
查询该订单的异常明细:
 - 异常步骤: [4], 原因: 金额校验失败，实付金额与计算金额不一致

...

=== Easy Recon SDK 功能演示结束 ===
```
