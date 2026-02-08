# Java Spring Boot Starter API 参考

## 1. 核心类

### 1.1 EasyReconTemplate

**描述**：SDK 的核心模板类，提供对账相关的核心方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `doRealtimeRecon(ReconOrderMainDO, List<ReconOrderSplitSubDO>)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `boolean`：对账结果 | 执行实时对账 |
| `doRealtimeReconAsync(ReconOrderMainDO, List<ReconOrderSplitSubDO>)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `CompletableFuture<Boolean>`：异步对账结果 | 异步执行实时对账 |
| `doTimingRecon(String)` | dateStr: 对账日期（yyyy-MM-dd） | `boolean`：对账结果 | 执行定时对账 |

**使用示例**：

```java
@Autowired
private EasyReconTemplate easyReconTemplate;

// 同步对账
boolean result = easyReconTemplate.doRealtimeRecon(orderMain, splitSubs);

// 异步对账
CompletableFuture<Boolean> future = easyReconTemplate.doRealtimeReconAsync(orderMain, splitSubs);
future.thenAccept(success -> {
    if (success) {
        System.out.println("对账成功");
    } else {
        System.out.println("对账失败");
    }
});

// 定时对账
String dateStr = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
boolean timingResult = easyReconTemplate.doTimingRecon(dateStr);
```

### 1.2 ReconRepository

**描述**：对账数据访问接口，定义了数据库操作方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `saveOrderMain(ReconOrderMainDO)` | orderMain: 订单主记录 | `boolean`：保存结果 | 保存对账订单主记录 |
| `batchSaveOrderSplitSub(List<ReconOrderSplitSubDO>)` | splitSubs: 分账子记录列表 | `boolean`：保存结果 | 批量保存分账子记录 |
| `saveException(ReconExceptionDO)` | exception: 异常记录 | `boolean`：保存结果 | 保存异常记录 |
| `batchSaveException(List<ReconExceptionDO>)` | exceptions: 异常记录列表 | `boolean`：保存结果 | 批量保存异常记录 |
| `getOrderMainByOrderNo(String)` | orderNo: 订单号 | `ReconOrderMainDO`：订单主记录 | 根据订单号查询对账订单主记录 |
| `getOrderSplitSubByOrderNo(String)` | orderNo: 订单号 | `List<ReconOrderSplitSubDO>`：分账子记录列表 | 根据订单号查询分账子记录 |
| `getPendingReconOrders(String, int, int)` | dateStr: 日期<br>offset: 偏移量<br>limit: 限制数量 | `List<ReconOrderMainDO>`：待核账订单列表 | 查询指定日期的待核账订单（分页） |
| `updateReconStatus(String, int)` | orderNo: 订单号<br>reconStatus: 对账状态 | `boolean`：更新结果 | 更新对账状态 |

### 1.3 RealtimeReconService

**描述**：实时对账服务，处理实时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `reconcile(ReconOrderMainDO, List<ReconOrderSplitSubDO>)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `boolean`：对账结果 | 执行实时对账逻辑 |

### 1.4 TimingReconService

**描述**：定时核账服务，处理定时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `reconcile(String)` | dateStr: 对账日期（yyyy-MM-dd） | `boolean`：对账结果 | 执行定时对账逻辑 |

### 1.5 AlarmService

**描述**：告警服务，处理对账过程中的异常告警。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `alarm(ReconExceptionDO)` | exception: 异常记录 | `void` | 发送告警 |

## 2. 实体类

### 2.1 ReconOrderMainDO

**描述**：对账订单主记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `Long` | 主键 |
| `orderNo` | `String` | 订单号 |
| `merchantId` | `String` | 商户ID |
| `merchantName` | `String` | 商户名称 |
| `orderAmount` | `BigDecimal` | 订单金额 |
| `actualAmount` | `BigDecimal` | 实际金额 |
| `reconStatus` | `Integer` | 对账状态（0: 待对账, 1: 已对账, 2: 对账异常） |
| `orderTime` | `Date` | 订单时间 |
| `payTime` | `Date` | 支付时间 |
| `reconTime` | `Date` | 对账时间 |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

### 2.2 ReconOrderSplitSubDO

**描述**：对账订单分账子记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `Long` | 主键 |
| `orderNo` | `String` | 订单号 |
| `subOrderNo` | `String` | 分账子订单号 |
| `merchantId` | `String` | 商户ID |
| `splitAmount` | `BigDecimal` | 分账金额 |
| `status` | `Integer` | 状态（0: 待处理, 1: 已处理, 2: 处理异常） |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

### 2.3 ReconExceptionDO

**描述**：对账异常记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `Long` | 主键 |
| `orderNo` | `String` | 订单号 |
| `merchantId` | `String` | 商户ID |
| `exceptionType` | `Integer` | 异常类型（1: 金额不匹配, 2: 订单不存在, 3: 其他异常） |
| `exceptionMsg` | `String` | 异常消息 |
| `exceptionStep` | `Integer` | 异常步骤（1: 数据获取, 2: 数据匹配, 3: 状态更新） |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

### 2.4 ReconNotifyLogDO

**描述**：对账通知日志实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `Long` | 主键 |
| `orderNo` | `String` | 订单号 |
| `merchantId` | `String` | 商户ID |
| `notifyType` | `Integer` | 通知类型（1: 对账结果, 2: 异常告警） |
| `notifyUrl` | `String` | 通知URL |
| `notifyContent` | `String` | 通知内容 |
| `notifyStatus` | `Integer` | 通知状态（0: 待通知, 1: 通知成功, 2: 通知失败） |
| `retryCount` | `Integer` | 重试次数 |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

### 2.5 ReconRuleDO

**描述**：对账规则实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `Long` | 主键 |
| `ruleName` | `String` | 规则名称 |
| `ruleType` | `Integer` | 规则类型（1: 金额匹配, 2: 订单存在性, 3: 自定义规则） |
| `ruleConfig` | `String` | 规则配置（JSON格式） |
| `isEnabled` | `Boolean` | 是否启用 |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

## 3. 配置类

### 3.1 ReconSdkProperties

**描述**：SDK 配置属性类。

**字段**：

| 字段名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| `enabled` | `boolean` | `true` | 是否启用 SDK |
| `amountTolerance` | `double` | `0.01` | 金额容差 |
| `batchSize` | `int` | `1000` | 定时核账批次大小 |
| `timingCron` | `String` | `0 0 2 * * ?` | 定时核账 cron 表达式 |
| `alarm` | `AlarmConfig` | - | 告警配置 |
| `threadPool` | `ThreadPoolConfig` | - | 线程池配置 |
| `configSecretKey` | `String` | - | 配置解密密钥 |

### 3.2 AlarmConfig

**描述**：告警配置类。

**字段**：

| 字段名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| `type` | `String` | `log` | 告警类型（log 或 dingtalk） |
| `dingtalk` | `DingTalkConfig` | - | 钉钉告警配置 |

### 3.3 DingTalkConfig

**描述**：钉钉告警配置类。

**字段**：

| 字段名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| `webhookUrl` | `String` | - | 钉钉告警机器人 Webhook |

### 3.4 ThreadPoolConfig

**描述**：线程池配置类。

**字段**：

| 字段名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| `corePoolSize` | `int` | `10` | 线程池核心线程数 |
| `maxPoolSize` | `int` | `20` | 线程池最大线程数 |
| `queueCapacity` | `int` | `1000` | 线程池队列容量 |

## 4. 自动配置

### 4.1 ReconSdkAutoConfiguration

**描述**：SDK 自动配置类，负责自动配置 SDK 的相关组件。

**功能**：
- 自动配置数据源
- 自动配置 Flyway 数据库迁移
- 自动配置 SDK 相关组件
- 自动启用定时任务

**条件注解**：
- `@ConditionalOnEnabledAutoConfiguration`：当启用自动配置时生效
- `@ConditionalOnProperty`：当配置属性满足条件时生效

## 5. 异常类

### 5.1 ReconException

**描述**：对账异常基类。

**继承关系**：`RuntimeException`

**构造方法**：
- `ReconException(String message)`：通过错误消息创建异常
- `ReconException(String message, Throwable cause)`：通过错误消息和原因创建异常

### 5.2 DatabaseException

**描述**：数据库异常。

**继承关系**：`ReconException`

### 5.3 AlarmException

**描述**：告警异常。

**继承关系**：`ReconException`

## 6. 枚举类

### 6.1 ReconStatus

**描述**：对账状态枚举。

**值**：
- `PENDING` (0)：待对账
- `SUCCESS` (1)：已对账
- `FAILED` (2)：对账异常

### 6.2 ExceptionType

**描述**：异常类型枚举。

**值**：
- `AMOUNT_MISMATCH` (1)：金额不匹配
- `ORDER_NOT_FOUND` (2)：订单不存在
- `OTHER` (3)：其他异常

### 6.3 ExceptionStep

**描述**：异常步骤枚举。

**值**：
- `DATA_FETCH` (1)：数据获取
- `DATA_MATCH` (2)：数据匹配
- `STATUS_UPDATE` (3)：状态更新

### 6.4 NotifyType

**描述**：通知类型枚举。

**值**：
- `RECON_RESULT` (1)：对账结果
- `EXCEPTION_ALARM` (2)：异常告警

### 6.5 NotifyStatus

**描述**：通知状态枚举。

**值**：
- `PENDING` (0)：待通知
- `SUCCESS` (1)：通知成功
- `FAILED` (2)：通知失败

### 6.6 RuleType

**描述**：规则类型枚举。

**值**：
- `AMOUNT_MATCH` (1)：金额匹配
- `ORDER_EXISTENCE` (2)：订单存在性
- `CUSTOM` (3)：自定义规则

## 7. 工具类

### 7.1 ReconUtils

**描述**：对账工具类，提供对账相关的工具方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `isAmountMatch(BigDecimal, BigDecimal, double)` | expected: 期望金额<br>actual: 实际金额<br>tolerance: 容差 | `boolean`：是否匹配 | 检查金额是否匹配 |
| `formatDate(Date, String)` | date: 日期<br>pattern: 日期格式 | `String`：格式化后的日期 | 格式化日期 |
| `parseDate(String, String)` | dateStr: 日期字符串<br>pattern: 日期格式 | `Date`：解析后的日期 | 解析日期字符串 |
| `generateOrderNo()` | 无 | `String`：订单号 | 生成订单号 |
| `generateSubOrderNo()` | 无 | `String`：分账子订单号 | 生成分账子订单号 |

### 7.2 EncryptionUtils

**描述**：加密工具类，提供配置加密和解密方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `encrypt(String, String)` | plainText: 明文<br>secretKey: 密钥 | `String`：加密后的字符串 | 加密字符串 |
| `decrypt(String, String)` | encryptedText: 加密后的字符串<br>secretKey: 密钥 | `String`：解密后的明文 | 解密字符串 |
