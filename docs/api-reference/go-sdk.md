# Go SDK API 参考

## 1. 核心包

### 1.1 core

**描述**：核心包，包含 SDK 的核心模板类。

#### 1.1.1 EasyReconTemplate

**描述**：SDK 的核心模板类，提供对账相关的核心方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `DoRealtimeRecon(orderMain *entity.ReconOrderMain, splitSubs []*entity.ReconOrderSplitSub)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `bool`：对账结果<br>`error`：错误信息 | 执行实时对账 |
| `DoTimingRecon(dateStr string)` | dateStr: 对账日期（yyyy-MM-dd） | `bool`：对账结果<br>`error`：错误信息 | 执行定时对账 |

**使用示例**：

```go
// 创建模板
template := core.NewEasyReconTemplate(realtimeService, timingService)

// 执行实时对账
orderMain := &entity.ReconOrderMain{
    OrderNo:      "ORDER_123",
    MerchantId:   "MERCHANT_001",
    MerchantName: "测试商户",
    OrderAmount:  100.00,
    ActualAmount: 100.00,
    ReconStatus:  0,
    OrderTime:    time.Now(),
    PayTime:      time.Now(),
    CreateTime:   time.Now(),
    UpdateTime:   time.Now(),
}

splitSubs := []*entity.ReconOrderSplitSub{
    {
        OrderNo:     "ORDER_123",
        SubOrderNo:  "SUB_123",
        MerchantId:  "MERCHANT_001",
        SplitAmount: 80.00,
        Status:      0,
        CreateTime:  time.Now(),
        UpdateTime:  time.Now(),
    },
}

success, err := template.DoRealtimeRecon(orderMain, splitSubs)
if err != nil {
    log.Printf("对账失败: %v", err)
} else if success {
    log.Println("对账成功")
} else {
    log.Println("对账失败")
}

// 执行定时对账
dateStr := time.Now().AddDate(0, 0, -1).Format("2006-01-02")
success, err = template.DoTimingRecon(dateStr)
if err != nil {
    log.Printf("定时对账失败: %v", err)
} else if success {
    log.Println("定时对账成功")
} else {
    log.Println("定时对账失败")
}
```

### 1.2 service

**描述**：服务包，包含对账相关的服务类。

#### 1.2.1 RealtimeReconService

**描述**：实时对账服务，处理实时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `NewRealtimeReconService(repo repository.ReconRepository, alarmService *AlarmService)` | repo: 存储库<br>alarmService: 告警服务 | `*RealtimeReconService`：实时对账服务实例 | 创建实时对账服务实例 |
| `Reconcile(orderMain *entity.ReconOrderMain, splitSubs []*entity.ReconOrderSplitSub)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `bool`：对账结果<br>`error`：错误信息 | 执行实时对账逻辑 |

#### 1.2.2 TimingReconService

**描述**：定时核账服务，处理定时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `NewTimingReconService(repo repository.ReconRepository, alarmService *AlarmService)` | repo: 存储库<br>alarmService: 告警服务 | `*TimingReconService`：定时核账服务实例 | 创建定时核账服务实例 |
| `Reconcile(dateStr string)` | dateStr: 对账日期（yyyy-MM-dd） | `bool`：对账结果<br>`error`：错误信息 | 执行定时对账逻辑 |

#### 1.2.3 AlarmService

**描述**：告警服务，处理对账过程中的异常告警。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `NewAlarmService(strategy AlarmStrategy)` | strategy: 告警策略 | `*AlarmService`：告警服务实例 | 创建告警服务实例 |
| `Alarm(exception *entity.ReconException)` | exception: 异常记录 | `error`：错误信息 | 发送告警 |

**告警策略**：

| 策略名 | 描述 |
|--------|------|
| `LogAlarmStrategy` | 日志告警策略，将异常信息记录到日志 |
| `DingTalkAlarmStrategy` | 钉钉告警策略，通过钉钉机器人发送告警消息 |

### 1.3 repository

**描述**：存储库包，包含数据访问相关的接口和实现。

#### 1.3.1 ReconRepository

**描述**：对账数据访问接口，定义了数据库操作方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `SaveOrderMain(orderMain *entity.ReconOrderMain)` | orderMain: 订单主记录 | `error`：错误信息 | 保存对账订单主记录 |
| `BatchSaveOrderSplitSub(splitSubs []*entity.ReconOrderSplitSub)` | splitSubs: 分账子记录列表 | `error`：错误信息 | 批量保存分账子记录 |
| `SaveException(exception *entity.ReconException)` | exception: 异常记录 | `error`：错误信息 | 保存异常记录 |
| `BatchSaveException(exceptions []*entity.ReconException)` | exceptions: 异常记录列表 | `error`：错误信息 | 批量保存异常记录 |
| `GetOrderMainByOrderNo(orderNo string)` | orderNo: 订单号 | `*entity.ReconOrderMain`：订单主记录<br>`error`：错误信息 | 根据订单号查询对账订单主记录 |
| `GetOrderSplitSubByOrderNo(orderNo string)` | orderNo: 订单号 | `[]*entity.ReconOrderSplitSub`：分账子记录列表<br>`error`：错误信息 | 根据订单号查询分账子记录 |
| `GetPendingReconOrders(dateStr string, offset, limit int)` | dateStr: 日期<br>offset: 偏移量<br>limit: 限制数量 | `[]*entity.ReconOrderMain`：待核账订单列表<br>`error`：错误信息 | 查询指定日期的待核账订单（分页） |
| `UpdateReconStatus(orderNo string, reconStatus int)` | orderNo: 订单号<br>reconStatus: 对账状态 | `error`：错误信息 | 更新对账状态 |

#### 1.3.2 SQLReconRepository

**描述**：SQL 实现的对账存储库。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `NewSQLReconRepository(db *sql.DB, dialect dialect.ReconDatabaseDialect)` | db: 数据库连接<br>dialect: 数据库方言 | `*SQLReconRepository`：SQL 对账存储库实例 | 创建 SQL 对账存储库实例 |
| 实现了 `ReconRepository` 接口的所有方法 | - | - | - |

### 1.4 dialect

**描述**：方言包，包含数据库方言相关的接口和实现。

#### 1.4.1 ReconDatabaseDialect

**描述**：数据库方言接口，定义了数据库相关的方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `GetInsertOrderMainSQL()` | 无 | `string`：插入订单主记录的 SQL 语句 | 获取插入订单主记录的 SQL 语句 |
| `GetInsertOrderSplitSubSQL()` | 无 | `string`：插入分账子记录的 SQL 语句 | 获取插入分账子记录的 SQL 语句 |
| `GetInsertExceptionSQL()` | 无 | `string`：插入异常记录的 SQL 语句 | 获取插入异常记录的 SQL 语句 |
| `GetSelectOrderMainByOrderNoSQL()` | 无 | `string`：根据订单号查询订单主记录的 SQL 语句 | 获取根据订单号查询订单主记录的 SQL 语句 |
| `GetSelectOrderSplitSubByOrderNoSQL()` | 无 | `string`：根据订单号查询分账子记录的 SQL 语句 | 获取根据订单号查询分账子记录的 SQL 语句 |
| `GetSelectPendingReconOrdersSQL()` | 无 | `string`：查询待核账订单的 SQL 语句 | 获取查询待核账订单的 SQL 语句 |
| `GetUpdateReconStatusSQL()` | 无 | `string`：更新对账状态的 SQL 语句 | 获取更新对账状态的 SQL 语句 |

#### 1.4.2 MySQLDialect

**描述**：MySQL 数据库方言实现。

**方法**：实现了 `ReconDatabaseDialect` 接口的所有方法。

#### 1.4.3 PostgreSQLDialect

**描述**：PostgreSQL 数据库方言实现。

**方法**：实现了 `ReconDatabaseDialect` 接口的所有方法。

**工具方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `CreateDialect(db *sql.DB)` | db: 数据库连接 | `dialect.ReconDatabaseDialect`：数据库方言实例 | 根据数据库连接创建对应的数据库方言实例 |

### 1.5 entity

**描述**：实体包，包含对账相关的实体类。

#### 1.5.1 ReconOrderMain

**描述**：对账订单主记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `ID` | `int64` | 主键 |
| `OrderNo` | `string` | 订单号 |
| `MerchantId` | `string` | 商户ID |
| `MerchantName` | `string` | 商户名称 |
| `OrderAmount` | `float64` | 订单金额 |
| `ActualAmount` | `float64` | 实际金额 |
| `ReconStatus` | `int` | 对账状态（0: 待对账, 1: 已对账, 2: 对账异常） |
| `OrderTime` | `time.Time` | 订单时间 |
| `PayTime` | `time.Time` | 支付时间 |
| `ReconTime` | `time.Time` | 对账时间 |
| `CreateTime` | `time.Time` | 创建时间 |
| `UpdateTime` | `time.Time` | 更新时间 |

#### 1.5.2 ReconOrderSplitSub

**描述**：对账订单分账子记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `ID` | `int64` | 主键 |
| `OrderNo` | `string` | 订单号 |
| `SubOrderNo` | `string` | 分账子订单号 |
| `MerchantId` | `string` | 商户ID |
| `SplitAmount` | `float64` | 分账金额 |
| `Status` | `int` | 状态（0: 待处理, 1: 已处理, 2: 处理异常） |
| `CreateTime` | `time.Time` | 创建时间 |
| `UpdateTime` | `time.Time` | 更新时间 |

#### 1.5.3 ReconException

**描述**：对账异常记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `ID` | `int64` | 主键 |
| `OrderNo` | `string` | 订单号 |
| `MerchantId` | `string` | 商户ID |
| `ExceptionType` | `int` | 异常类型（1: 金额不匹配, 2: 订单不存在, 3: 其他异常） |
| `ExceptionMsg` | `string` | 异常消息 |
| `ExceptionStep` | `int` | 异常步骤（1: 数据获取, 2: 数据匹配, 3: 状态更新） |
| `CreateTime` | `time.Time` | 创建时间 |
| `UpdateTime` | `time.Time` | 更新时间 |

### 1.6 util

**描述**：工具包，包含对账相关的工具类。

#### 1.6.1 reconutil

**描述**：对账工具类，提供对账相关的工具方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `IsAmountMatch(expected, actual, tolerance float64)` | expected: 期望金额<br>actual: 实际金额<br>tolerance: 容差 | `bool`：是否匹配 | 检查金额是否匹配 |
| `FormatDate(t time.Time, layout string)` | t: 时间<br>layout: 日期格式 | `string`：格式化后的日期 | 格式化日期 |
| `ParseDate(dateStr, layout string)` | dateStr: 日期字符串<br>layout: 日期格式 | `time.Time`：解析后的时间<br>`error`：错误信息 | 解析日期字符串 |
| `GenerateOrderNo()` | 无 | `string`：订单号 | 生成订单号 |
| `GenerateSubOrderNo()` | 无 | `string`：分账子订单号 | 生成分账子订单号 |

#### 1.6.2 encryptutil

**描述**：加密工具类，提供配置加密和解密方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `Encrypt(plainText, secretKey string)` | plainText: 明文<br>secretKey: 密钥 | `string`：加密后的字符串<br>`error`：错误信息 | 加密字符串 |
| `Decrypt(encryptedText, secretKey string)` | encryptedText: 加密后的字符串<br>secretKey: 密钥 | `string`：解密后的明文<br>`error`：错误信息 | 解密字符串 |

## 2. 配置

### 2.1 数据库配置

**示例**：

```go
// 数据库连接配置
db, err := sql.Open("mysql", "root:password@tcp(localhost:3306)/easy_recon")
if err != nil {
    log.Fatal(err)
}

db.SetMaxIdleConns(10)
db.SetMaxOpenConns(100)
db.SetConnMaxLifetime(time.Hour)
```

### 2.2 线程池配置

**示例**：

```go
// 线程池配置
type ThreadPoolConfig struct {
    CoreSize  int
    MaxSize   int
    QueueSize int
}

config := &ThreadPoolConfig{
    CoreSize:  10,
    MaxSize:   20,
    QueueSize: 1000,
}
```

### 2.3 告警配置

**示例**：

```go
// 告警配置
type AlarmConfig struct {
    Type      string
    DingTalk  *DingTalkConfig
}

type DingTalkConfig struct {
    WebhookURL string
}

config := &AlarmConfig{
    Type: "log",
    DingTalk: &DingTalkConfig{
        WebhookURL: "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN",
    },
}
```

## 3. 错误处理

**示例**：

```go
// 错误处理
success, err := template.DoRealtimeRecon(orderMain, splitSubs)
if err != nil {
    // 处理错误
    switch {
    case errors.Is(err, repository.ErrDatabase):
        log.Printf("数据库错误: %v", err)
    case errors.Is(err, service.ErrAmountMismatch):
        log.Printf("金额不匹配: %v", err)
    case errors.Is(err, service.ErrOrderNotFound):
        log.Printf("订单不存在: %v", err)
    default:
        log.Printf("对账错误: %v", err)
    }
} else if success {
    log.Println("对账成功")
} else {
    log.Println("对账失败")
}
```

## 4. 并发处理

**示例**：

```go
// 并发处理多个订单
func processMultipleOrders(template *core.EasyReconTemplate, orders []*entity.ReconOrderMain) {
    var wg sync.WaitGroup
    semaphore := make(chan struct{}, 10) // 限制并发数为10
    
    for _, order := range orders {
        wg.Add(1)
        semaphore <- struct{}{} // 获取信号量
        
        go func(o *entity.ReconOrderMain) {
            defer wg.Done()
            defer func() { <-semaphore }() // 释放信号量
            
            // 执行对账
            success, err := template.DoRealtimeRecon(o, nil)
            if err != nil {
                log.Printf("对账失败: %v", err)
            } else if success {
                log.Printf("订单 %s 对账成功", o.OrderNo)
            } else {
                log.Printf("订单 %s 对账失败", o.OrderNo)
            }
        }(order)
    }
    
    wg.Wait()
}
```

## 5. 最佳实践

### 5.1 数据库连接管理

- 使用连接池管理数据库连接
- 设置合理的连接池参数
- 及时关闭不再使用的连接

### 5.2 错误处理

- 对所有错误进行适当的处理
- 使用错误包装提供更多上下文信息
- 区分不同类型的错误

### 5.3 并发控制

- 合理使用并发提高性能
- 使用信号量限制并发数
- 避免竞态条件

### 5.4 配置管理

- 使用环境变量或配置文件管理配置
- 对敏感配置进行加密
- 提供合理的默认值

### 5.5 日志管理

- 使用结构化日志
- 设置适当的日志级别
- 记录关键操作和错误信息
