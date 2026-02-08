# Easy Recon SDK 文档

## 1. 项目概述

### 1.1 项目背景

在现代支付系统中，交易数据的准确性和一致性至关重要。多商户支付场景下，交易数据量大、交易类型复杂，人工对账效率低、容易出错。Easy Recon SDK 应运而生，旨在提供一套自动化、高效、可靠的支付对账解决方案。

### 1.2 项目目标

- **自动化对账**：减少人工干预，提高对账效率
- **实时性**：支持实时对账，及时发现和处理异常
- **可靠性**：确保交易数据的准确性和一致性
- **可扩展性**：支持多种数据库、多种告警方式
- **多语言支持**：提供不同语言版本的 SDK，满足不同技术栈的需求

### 1.3 核心价值

- **降低运营成本**：自动化对账减少人工成本
- **提高数据准确性**：避免人工对账的错误
- **及时发现异常**：实时告警机制，及时处理异常交易
- **简化集成**：提供简洁易用的 API，降低集成难度

### 1.4 项目架构

Easy Recon SDK 采用分层架构设计，各层职责明确，便于扩展和维护：

**核心架构层次**：
1. **实体层**：定义数据模型，对应数据库表结构
2. **存储层**：负责数据库操作，支持多种数据库方言
3. **服务层**：实现核心业务逻辑，包括实时对账和定时核账
4. **告警层**：处理异常告警，支持多种告警策略
5. **模板层**：提供统一的对外接口，简化使用
6. **配置层**：管理 SDK 配置，支持不同环境的配置

**跨语言架构**：
- 每种语言版本保持相同的核心功能和 API 设计
- 采用语言特定的最佳实践实现
- 共享相同的数据库结构和业务逻辑
- 提供统一的文档和使用指南

## 2. 支持的语言版本

- **Java Spring Boot Starter** (`sdk/spring-boot-starter/`)：适用于 Java Spring Boot 项目
- **Go** (`sdk/go/`)：适用于 Go 语言项目
- **Python** (`sdk/python/`)：适用于 Python 语言项目
- **Node.js** (`sdk/node.js/`)：适用于 Node.js 语言项目

## 3. 核心功能

### 3.1 实时对账

**功能说明**：实时处理支付订单，确保交易数据的准确性。当收到支付回调或支付结果时，立即进行对账处理。

**使用场景**：
- 支付完成后立即对账
- 交易状态变更时进行对账
- 需要实时确认交易状态的场景

**实现原理**：
1. 接收支付订单数据
2. 保存订单主记录和分账子记录
3. 执行对账逻辑
4. 更新对账状态
5. 处理异常情况

**技术亮点**：
- **异步处理**：Java 版本使用 CompletableFuture 实现异步操作，提高并发处理能力
- **事务管理**：确保数据一致性，避免部分更新导致的数据错误
- **批量处理**：支持批量提交，减少数据库交互次数

### 3.2 定时核账

**功能说明**：定时对未对账订单进行批量处理，确保所有订单都能被正确对账。

**使用场景**：
- 每日/每周定期对账
- 处理因网络问题等原因未实时对账的订单
- 批量处理历史订单

**实现原理**：
1. 按照设定的时间周期执行
2. 查询待核账订单
3. 批量处理每个订单
4. 生成对账报告
5. 处理异常情况

**技术亮点**：
- **任务调度**：Java 版本集成 Spring 定时任务，支持 cron 表达式配置
- **分页查询**：避免一次性加载大量数据导致内存溢出
- **并行处理**：利用线程池并行处理多个订单，提高处理效率

### 3.3 异常告警

**功能说明**：对对账过程中的异常进行告警，支持多种告警方式，确保异常能被及时发现和处理。

**支持的告警方式**：
- 日志告警：将异常信息记录到日志
- 钉钉告警：通过钉钉机器人发送告警消息

**实现原理**：
1. 监控对账过程中的异常
2. 根据配置的告警策略发送告警
3. 记录告警历史

**技术亮点**：
- **策略模式**：采用策略模式实现不同告警方式的切换
- **可扩展性**：支持自定义告警策略
- **告警记录**：详细记录告警历史，便于后续分析

### 3.4 自动建表

**功能说明**：使用 Flyway 自动创建数据库表结构，简化数据库初始化过程。

**支持的数据库**：
- MySQL
- PostgreSQL

**实现原理**：
1. 集成 Flyway 数据库迁移工具
2. 提供数据库表结构的 SQL 脚本
3. 应用启动时自动执行数据库迁移

**技术亮点**：
- **版本管理**：通过版本号管理数据库结构变更
- **自动执行**：无需手动执行 SQL 脚本
- **回滚支持**：支持数据库结构的回滚

### 3.5 多数据库支持

**功能说明**：支持多种数据库，提供统一的数据库操作接口，屏蔽不同数据库的语法差异。

**支持的数据库**：
- MySQL
- PostgreSQL

**实现原理**：
1. 抽象数据库方言接口
2. 为每种数据库实现对应的方言
3. 根据数据库类型自动选择对应的方言

**技术亮点**：
- **方言抽象**：通过接口定义统一的数据库操作方法
- **自动检测**：根据数据库连接自动检测并选择对应的方言
- **可扩展性**：支持添加新的数据库方言

### 3.6 性能优化

**功能说明**：通过多种技术手段优化系统性能，提高处理能力和响应速度。

**优化策略**：
- **线程池优化**：根据系统资源合理配置线程池参数
- **异步操作**：使用异步方式处理非关键任务
- **批量处理**：批量提交数据库操作，减少网络开销
- **缓存机制**：缓存热点数据，减少数据库查询

**技术实现**：
- Java 版本：使用 ThreadPoolExecutor 自定义线程池
- Go 版本：使用 goroutine 实现并发处理
- Python 版本：使用线程池或异步 I/O
- Node.js 版本：利用事件循环和 Promise 实现异步操作

### 3.7 安全最佳实践

**功能说明**：采用多种安全措施，保护系统和数据安全。

**安全措施**：
- **配置加密**：敏感配置信息加密存储
- **SQL 注入防护**：使用参数化查询，防止 SQL 注入攻击
- **权限控制**：严格的数据库权限管理
- **日志脱敏**：敏感信息脱敏处理，避免信息泄露

**技术实现**：
- Java 版本：使用 Jasypt 进行配置加密
- 其他版本：实现相应的加密和安全机制

## 4. 数据库表结构

### 4.1 recon_order_main（对账订单主记录）

| 字段名 | 数据类型 | 描述 | 索引 | 约束 |
|--------|----------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| order_no | VARCHAR(64) | 订单号 | UNIQUE INDEX | NOT NULL |
| merchant_id | VARCHAR(32) | 商户ID | INDEX | NOT NULL |
| merchant_name | VARCHAR(128) | 商户名称 | | NOT NULL |
| order_amount | DECIMAL(12,2) | 订单金额 | | NOT NULL |
| actual_amount | DECIMAL(12,2) | 实际金额 | | NOT NULL |
| recon_status | INT | 对账状态（0: 待对账, 1: 已对账, 2: 对账异常） | INDEX | NOT NULL |
| order_time | DATETIME | 订单时间 | INDEX | NOT NULL |
| pay_time | DATETIME | 支付时间 | | |
| recon_time | DATETIME | 对账时间 | | |
| create_time | DATETIME | 创建时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| update_time | DATETIME | 更新时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 4.2 recon_order_split_sub（对账订单分账子记录）

| 字段名 | 数据类型 | 描述 | 索引 | 约束 |
|--------|----------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| order_no | VARCHAR(64) | 订单号 | INDEX | NOT NULL |
| sub_order_no | VARCHAR(64) | 分账子订单号 | UNIQUE INDEX | NOT NULL |
| merchant_id | VARCHAR(32) | 商户ID | INDEX | NOT NULL |
| split_amount | DECIMAL(12,2) | 分账金额 | | NOT NULL |
| status | INT | 状态（0: 待处理, 1: 已处理, 2: 处理异常） | | NOT NULL |
| create_time | DATETIME | 创建时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| update_time | DATETIME | 更新时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 4.3 recon_exception（对账异常记录）

| 字段名 | 数据类型 | 描述 | 索引 | 约束 |
|--------|----------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| order_no | VARCHAR(64) | 订单号 | INDEX | NOT NULL |
| merchant_id | VARCHAR(32) | 商户ID | INDEX | NOT NULL |
| exception_type | INT | 异常类型（1: 金额不匹配, 2: 订单不存在, 3: 其他异常） | | NOT NULL |
| exception_msg | VARCHAR(256) | 异常消息 | | NOT NULL |
| exception_step | INT | 异常步骤（1: 数据获取, 2: 数据匹配, 3: 状态更新） | | NOT NULL |
| create_time | DATETIME | 创建时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| update_time | DATETIME | 更新时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 4.4 recon_notify_log（对账通知日志）

| 字段名 | 数据类型 | 描述 | 索引 | 约束 |
|--------|----------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| order_no | VARCHAR(64) | 订单号 | INDEX | NOT NULL |
| merchant_id | VARCHAR(32) | 商户ID | INDEX | NOT NULL |
| notify_type | INT | 通知类型（1: 对账结果, 2: 异常告警） | | NOT NULL |
| notify_url | VARCHAR(256) | 通知URL | | |
| notify_content | TEXT | 通知内容 | | |
| notify_status | INT | 通知状态（0: 待通知, 1: 通知成功, 2: 通知失败） | | NOT NULL |
| retry_count | INT | 重试次数 | | NOT NULL, DEFAULT 0 |
| create_time | DATETIME | 创建时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| update_time | DATETIME | 更新时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 4.5 recon_rule（对账规则）

| 字段名 | 数据类型 | 描述 | 索引 | 约束 |
|--------|----------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| rule_name | VARCHAR(64) | 规则名称 | UNIQUE INDEX | NOT NULL |
| rule_type | INT | 规则类型（1: 金额匹配, 2: 订单存在性, 3: 自定义规则） | | NOT NULL |
| rule_config | TEXT | 规则配置（JSON格式） | | NOT NULL |
| is_enabled | BOOLEAN | 是否启用 | | NOT NULL, DEFAULT TRUE |
| create_time | DATETIME | 创建时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| update_time | DATETIME | 更新时间 | | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 4.6 索引优化

为了提高查询性能，系统在以下字段上创建了索引：

1. **recon_order_main**：
   - `PRIMARY KEY`：`id`
   - `UNIQUE INDEX`：`order_no`
   - `INDEX`：`merchant_id`, `recon_status`, `order_time`

2. **recon_order_split_sub**：
   - `PRIMARY KEY`：`id`
   - `UNIQUE INDEX`：`sub_order_no`
   - `INDEX`：`order_no`, `merchant_id`

3. **recon_exception**：
   - `PRIMARY KEY`：`id`
   - `INDEX`：`order_no`, `merchant_id`

4. **recon_notify_log**：
   - `PRIMARY KEY`：`id`
   - `INDEX`：`order_no`, `merchant_id`

5. **recon_rule**：
   - `PRIMARY KEY`：`id`
   - `UNIQUE INDEX`：`rule_name`

### 4.7 外键关系

系统通过以下逻辑外键关系维护数据一致性：

1. **recon_order_split_sub.order_no** → **recon_order_main.order_no**：分账子记录关联到主订单记录
2. **recon_exception.order_no** → **recon_order_main.order_no**：异常记录关联到主订单记录
3. **recon_notify_log.order_no** → **recon_order_main.order_no**：通知日志关联到主订单记录

> 注：为了提高性能，系统使用逻辑外键而非物理外键，通过应用代码确保数据一致性。

## 5. 快速开始

### 5.1 Java Spring Boot Starter

#### 5.1.1 添加依赖

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.coffers</groupId>
    <artifactId>easy-recon-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 5.1.2 配置数据源

在 `application.yml` 中配置数据源和 SDK 相关配置：

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

eas-recon:
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

#### 5.1.3 启用 SDK

在启动类上添加 `@EnableEasyRecon` 注解：

```java
@SpringBootApplication
@EnableEasyRecon
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### 5.1.4 高级配置

**异步对账示例**：

```java
@Autowired
private EasyReconTemplate easyReconTemplate;

// 异步执行实时对账
public CompletableFuture<Boolean> doReconAsync() {
    // 创建订单主记录
    ReconOrderMainDO orderMain = new ReconOrderMainDO();
    orderMain.setOrderNo("ORDER_" + System.currentTimeMillis());
    orderMain.setMerchantId("MERCHANT_001");
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

    // 异步执行对账
    return easyReconTemplate.doRealtimeReconAsync(orderMain, splitSubs);
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

#### 5.1.5 使用示例

```java
@Autowired
private EasyReconTemplate easyReconTemplate;

// 执行实时对账
public boolean doRecon() {
    // 创建订单主记录
    ReconOrderMainDO orderMain = new ReconOrderMainDO();
    orderMain.setOrderNo("ORDER_" + System.currentTimeMillis());
    orderMain.setMerchantId("MERCHANT_001");
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
    return easyReconTemplate.doRealtimeRecon(orderMain, splitSubs);
}

// 执行定时对账
public boolean doTimingRecon() {
    String dateStr = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return easyReconTemplate.doTimingRecon(dateStr);
}
```

### 5.2 Go SDK

#### 5.2.1 安装依赖

```bash
cd sdk/go
go mod tidy
```

#### 5.2.2 高级配置

**配置选项**：

```go
// 数据库连接配置
dbConfig := &DBConfig{
    Host:     "localhost",
    Port:     3306,
    User:     "root",
    Password: "password",
    Database: "easy_recon",
    MaxIdle:  10,
    MaxOpen:  100,
}

// 线程池配置
threadPoolConfig := &ThreadPoolConfig{
    CoreSize:  10,
    MaxSize:   20,
    QueueSize: 1000,
}

// 告警配置
alarmConfig := &AlarmConfig{
    Type: "log",
    DingTalk: &DingTalkConfig{
        WebhookURL: "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN",
    },
}
```

**并发对账示例**：

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

#### 5.2.3 使用示例

```go
package main

import (
	"database/sql"
	"fmt"
	"log"

	"easy-recon-sdk/core"
	"easy-recon-sdk/dialect"
	"easy-recon-sdk/entity"
	"easy-recon-sdk/repository"
	"easy-recon-sdk/service"

	_ "github.com/go-sql-driver/mysql"
)

func main() {
	// 1. 初始化数据库连接
	db, err := sql.Open("mysql", "root:password@tcp(localhost:3306)/easy_recon")
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	// 2. 创建数据库方言
	dbDialect := dialect.CreateDialect(db)

	// 3. 创建存储库
	reconRepo := repository.NewSQLReconRepository(db, dbDialect)

	// 4. 创建告警服务
	alarmStrategy := &service.LogAlarmStrategy{}
	alarmService := service.NewAlarmService(alarmStrategy)

	// 5. 创建对账服务
	realtimeService := service.NewRealtimeReconService(reconRepo, alarmService)
	timingService := service.NewTimingReconService(reconRepo, alarmService)

	// 6. 创建模板
	template := core.NewEasyReconTemplate(realtimeService, timingService)

	// 7. 执行实时对账
	orderMain := &entity.ReconOrderMain{
		OrderNo:      "ORDER_" + fmt.Sprintf("%d", time.Now().Unix()),
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
			OrderNo:      orderMain.OrderNo,
			SubOrderNo:   "SUB_" + fmt.Sprintf("%d", time.Now().Unix()),
			MerchantId:   "MERCHANT_001",
			SplitAmount:  80.00,
			Status:       0,
			CreateTime:   time.Now(),
			UpdateTime:   time.Now(),
		},
	}

	success, err := template.DoRealtimeRecon(orderMain, splitSubs)
	if err != nil {
		log.Printf("实时对账失败: %v", err)
	} else if success {
		log.Println("实时对账成功")
	} else {
		log.Println("实时对账失败")
	}

	// 8. 执行定时对账
	dateStr := time.Now().AddDate(0, 0, -1).Format("2006-01-02")
	success, err = template.DoTimingRecon(dateStr)
	if err != nil {
		log.Printf("定时对账失败: %v", err)
	} else if success {
		log.Println("定时对账成功")
	} else {
		log.Println("定时对账失败")
	}
}
```

### 5.3 Python SDK

#### 5.3.1 安装依赖

```bash
cd sdk/python
pip install -r requirements.txt
```

#### 5.3.2 高级配置

**配置选项**：

```python
# 数据库连接配置
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'password',
    'database': 'easy_recon',
    'port': 3306,
    'pool_size': 10,
    'charset': 'utf8mb4'
}

# 线程池配置
thread_pool_config = {
    'max_workers': 10
}

# 告警配置
alarm_config = {
    'type': 'log',
    'dingtalk': {
        'webhook_url': 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN'
    }
}
```

**异步处理示例**：

```python
import asyncio
from concurrent.futures import ThreadPoolExecutor

async def async_process_orders(template, orders):
    """异步处理多个订单"""
    results = []
    
    # 使用线程池执行同步操作
    with ThreadPoolExecutor(max_workers=10) as executor:
        loop = asyncio.get_event_loop()
        tasks = []
        
        for order in orders:
            # 创建分账子记录
            split_subs = []
            sub = ReconOrderSplitSub()
            sub.order_no = order.order_no
            sub.sub_order_no = f"SUB_{int(time.time())}"
            sub.merchant_id = order.merchant_id
            sub.split_amount = order.order_amount * 0.8
            sub.status = 0
            sub.create_time = datetime.now()
            sub.update_time = datetime.now()
            split_subs.append(sub)
            
            # 提交任务到线程池
            task = loop.run_in_executor(
                executor,
                template.do_realtime_recon,
                order,
                split_subs
            )
            tasks.append(task)
        
        # 等待所有任务完成
        results = await asyncio.gather(*tasks)
    
    return results

# 使用示例
async def main():
    # 初始化模板...
    
    # 创建多个订单
    orders = []
    for i in range(10):
        order = ReconOrderMain()
        order.order_no = f"ORDER_{int(time.time())}_{i}"
        order.merchant_id = "MERCHANT_001"
        order.merchant_name = "测试商户"
        order.order_amount = 100.00
        order.actual_amount = 100.00
        order.recon_status = 0
        order.order_time = datetime.now()
        order.pay_time = datetime.now()
        order.create_time = datetime.now()
        order.update_time = datetime.now()
        orders.append(order)
    
    # 异步处理
    results = await async_process_orders(template, orders)
    print(f"处理结果: {results}")

# 运行异步函数
if __name__ == "__main__":
    asyncio.run(main())
```

#### 5.3.3 使用示例

```python
import mysql.connector
from datetime import datetime
from repository.sql_recon_repository import SQLReconRepository
from dialect.recon_database_dialect import create_dialect
from service.alarm_service import AlarmService, LogAlarmStrategy
from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService
from core.easy_recon_template import EasyReconTemplate
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub

# 1. 初始化数据库连接
connection = mysql.connector.connect(
    host="localhost",
    user="root",
    password="password",
    database="easy_recon"
)

# 2. 创建数据库方言
dialect = create_dialect(connection)

# 3. 创建存储库
repo = SQLReconRepository(connection, dialect)

# 4. 创建告警服务
alarm_service = AlarmService(LogAlarmStrategy())

# 5. 创建对账服务
realtime_recon_service = RealtimeReconService(repo, alarm_service)
timing_recon_service = TimingReconService(repo, alarm_service)

# 6. 创建模板
template = EasyReconTemplate(realtime_recon_service, timing_recon_service)

# 7. 执行实时对账
order_main = ReconOrderMain()
order_main.order_no = f"ORDER_{datetime.now().timestamp()}"
order_main.merchant_id = "MERCHANT_001"
order_main.merchant_name = "测试商户"
order_main.order_amount = 100.00
order_main.actual_amount = 100.00
order_main.recon_status = 0
order_main.order_time = datetime.now()
order_main.pay_time = datetime.now()
order_main.create_time = datetime.now()
order_main.update_time = datetime.now()

# 创建分账子记录
split_subs = []
sub = ReconOrderSplitSub()
sub.order_no = order_main.order_no
sub.sub_order_no = f"SUB_{datetime.now().timestamp()}"
sub.merchant_id = "MERCHANT_001"
sub.split_amount = 80.00
sub.status = 0
sub.create_time = datetime.now()
sub.update_time = datetime.now()
split_subs.append(sub)

# 执行对账
result = template.do_realtime_recon(order_main, split_subs)
print(f"实时对账结果: {result}")

# 8. 执行定时对账
from datetime import date, timedelta
date_str = (date.today() - timedelta(days=1)).strftime("%Y-%m-%d")
result = template.do_timing_recon(date_str)
print(f"定时对账结果: {result}")

# 9. 关闭连接
connection.close()
```

### 5.4 Node.js SDK

#### 5.4.1 安装依赖

```bash
cd sdk/node.js
npm install
```

#### 5.4.2 高级配置

**配置选项**：

```javascript
// 数据库连接配置
const dbConfig = {
    host: 'localhost',
    user: 'root',
    password: 'password',
    database: 'easy_recon',
    port: 3306,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
};

// 告警配置
const alarmConfig = {
    type: 'log',
    dingtalk: {
        webhookUrl: 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN'
    }
};

// 批量处理配置
const batchConfig = {
    batchSize: 1000,
    concurrency: 10
};
```

**批量处理示例**：

```javascript
const mysql = require('mysql2/promise');
const SQLReconRepository = require('./repository/SQLReconRepository');
const { createDialect } = require('./dialect/ReconDatabaseDialect');
const { AlarmService, LogAlarmStrategy } = require('./service/AlarmService');
const RealtimeReconService = require('./service/RealtimeReconService');
const EasyReconTemplate = require('./core/EasyReconTemplate');
const ReconOrderMain = require('./entity/ReconOrderMain');
const ReconOrderSplitSub = require('./entity/ReconOrderSplitSub');

async function batchProcessOrders() {
    // 初始化数据库连接
    const connection = await mysql.createConnection({
        host: 'localhost',
        user: 'root',
        password: 'password',
        database: 'easy_recon'
    });
    
    try {
        // 创建依赖组件
        const dialect = createDialect(connection);
        const repo = new SQLReconRepository(connection, dialect);
        const alarmService = new AlarmService(new LogAlarmStrategy());
        const realtimeReconService = new RealtimeReconService(repo, alarmService);
        const template = new EasyReconTemplate(realtimeReconService, null);
        
        // 生成测试订单
        const orders = [];
        for (let i = 0; i < 100; i++) {
            const orderNo = `ORDER_${Date.now()}_${i}`;
            const order = new ReconOrderMain();
            order.orderNo = orderNo;
            order.merchantId = 'MERCHANT_001';
            order.merchantName = '测试商户';
            order.orderAmount = 100.00;
            order.actualAmount = 100.00;
            order.reconStatus = 0;
            order.orderTime = new Date();
            order.payTime = new Date();
            order.createTime = new Date();
            order.updateTime = new Date();
            
            orders.push(order);
        }
        
        // 批量处理（并发限制为10）
        const batchSize = 10;
        const results = [];
        
        for (let i = 0; i < orders.length; i += batchSize) {
            const batch = orders.slice(i, i + batchSize);
            const batchResults = await Promise.all(
                batch.map(async (order) => {
                    // 创建分账子记录
                    const splitSubs = [];
                    const sub = new ReconOrderSplitSub();
                    sub.orderNo = order.orderNo;
                    sub.subOrderNo = `SUB_${Date.now()}`;
                    sub.merchantId = order.merchantId;
                    sub.splitAmount = order.orderAmount * 0.8;
                    sub.status = 0;
                    sub.createTime = new Date();
                    sub.updateTime = new Date();
                    splitSubs.push(sub);
                    
                    // 执行对账
                    return await template.doRealtimeRecon(order, splitSubs);
                })
            );
            
            results.push(...batchResults);
            console.log(`处理批次 ${Math.floor(i / batchSize) + 1} 完成`);
        }
        
        console.log(`总处理订单数: ${results.length}`);
        console.log(`成功数: ${results.filter(r => r).length}`);
        console.log(`失败数: ${results.filter(r => !r).length}`);
        
    } finally {
        // 关闭连接
        await connection.end();
    }
}

// 运行批量处理
batchProcessOrders().catch(console.error);
```

#### 5.4.3 使用示例

```javascript
const mysql = require('mysql2/promise');
const SQLReconRepository = require('./repository/SQLReconRepository');
const { createDialect } = require('./dialect/ReconDatabaseDialect');
const { AlarmService, LogAlarmStrategy } = require('./service/AlarmService');
const RealtimeReconService = require('./service/RealtimeReconService');
const TimingReconService = require('./service/TimingReconService');
const EasyReconTemplate = require('./core/EasyReconTemplate');
const ReconOrderMain = require('./entity/ReconOrderMain');
const ReconOrderSplitSub = require('./entity/ReconOrderSplitSub');

async function main() {
    // 1. 初始化数据库连接
    const connection = await mysql.createConnection({
        host: 'localhost',
        user: 'root',
        password: 'password',
        database: 'easy_recon'
    });

    // 2. 创建数据库方言
    const dialect = createDialect(connection);

    // 3. 创建存储库
    const repo = new SQLReconRepository(connection, dialect);

    // 4. 创建告警服务
    const alarmService = new AlarmService(new LogAlarmStrategy());

    // 5. 创建对账服务
    const realtimeReconService = new RealtimeReconService(repo, alarmService);
    const timingReconService = new TimingReconService(repo, alarmService);

    // 6. 创建模板
    const template = new EasyReconTemplate(realtimeReconService, timingReconService);

    // 7. 执行实时对账
    const orderMain = new ReconOrderMain();
    orderMain.orderNo = `ORDER_${Date.now()}`;
    orderMain.merchantId = 'MERCHANT_001';
    orderMain.merchantName = '测试商户';
    orderMain.orderAmount = 100.00;
    orderMain.actualAmount = 100.00;
    orderMain.reconStatus = 0;
    orderMain.orderTime = new Date();
    orderMain.payTime = new Date();
    orderMain.createTime = new Date();
    orderMain.updateTime = new Date();

    // 创建分账子记录
    const splitSubs = [];
    const sub = new ReconOrderSplitSub();
    sub.orderNo = orderMain.orderNo;
    sub.subOrderNo = `SUB_${Date.now()}`;
    sub.merchantId = 'MERCHANT_001';
    sub.splitAmount = 80.00;
    sub.status = 0;
    sub.createTime = new Date();
    sub.updateTime = new Date();
    splitSubs.push(sub);

    // 执行对账
    const result1 = await template.doRealtimeRecon(orderMain, splitSubs);
    console.log(`实时对账结果: ${result1}`);

    // 8. 执行定时对账
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const dateStr = yesterday.toISOString().split('T')[0];
    const result2 = await template.doTimingRecon(dateStr);
    console.log(`定时对账结果: ${result2}`);

    // 9. 关闭连接
    await connection.end();
}

main().catch(console.error);
```

## 6. 配置参考

### 6.1 Java Spring Boot Starter 配置

| 配置项 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| easy-recon.enabled | boolean | true | 是否启用 SDK |
| easy-recon.amount-tolerance | double | 0.01 | 金额容差 |
| easy-recon.batch-size | int | 1000 | 定时核账批次大小 |
| easy-recon.timing-cron | string | 0 0 2 * * ? | 定时核账 cron 表达式 |
| easy-recon.alarm.type | string | log | 告警类型（log 或 dingtalk） |
| easy-recon.alarm.dingtalk.webhook-url | string | - | 钉钉告警机器人 Webhook |
| easy-recon.thread-pool.core-pool-size | int | 10 | 线程池核心线程数 |
| easy-recon.thread-pool.max-pool-size | int | 20 | 线程池最大线程数 |
| easy-recon.config-secret-key | string | - | 配置解密密钥 |

### 6.2 Go SDK 配置

Go SDK 使用代码配置方式，主要配置项包括：

- 数据库连接信息
- 告警策略配置
- 线程池配置

### 6.3 Python SDK 配置

Python SDK 使用代码配置方式，主要配置项包括：

- 数据库连接信息
- 告警策略配置
- 批量处理大小

### 6.4 Node.js SDK 配置

Node.js SDK 使用代码配置方式，主要配置项包括：

- 数据库连接信息
- 告警策略配置
- 批量处理大小

## 7. API 文档

### 7.1 Java Spring Boot Starter API

#### 7.1.1 EasyReconTemplate

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| doRealtimeRecon(ReconOrderMainDO, List<ReconOrderSplitSubDO>) | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | boolean: 对账结果 | 执行实时对账 |
| doRealtimeReconAsync(ReconOrderMainDO, List<ReconOrderSplitSubDO>) | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | CompletableFuture<Boolean>: 异步对账结果 | 异步执行实时对账 |
| doTimingRecon(String) | dateStr: 对账日期（yyyy-MM-dd） | boolean: 对账结果 | 执行定时对账 |

#### 7.1.2 ReconRepository

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| saveOrderMain(ReconOrderMainDO) | orderMain: 订单主记录 | boolean: 保存结果 | 保存对账订单主记录 |
| batchSaveOrderSplitSub(List<ReconOrderSplitSubDO>) | splitSubs: 分账子记录列表 | boolean: 保存结果 | 批量保存分账子记录 |
| saveException(ReconExceptionDO) | exception: 异常记录 | boolean: 保存结果 | 保存异常记录 |
| batchSaveException(List<ReconExceptionDO>) | exceptions: 异常记录列表 | boolean: 保存结果 | 批量保存异常记录 |
| getOrderMainByOrderNo(String) | orderNo: 订单号 | ReconOrderMainDO: 订单主记录 | 根据订单号查询对账订单主记录 |
| getOrderSplitSubByOrderNo(String) | orderNo: 订单号 | List<ReconOrderSplitSubDO>: 分账子记录列表 | 根据订单号查询分账子记录 |
| getPendingReconOrders(String, int, int) | dateStr: 日期<br>offset: 偏移量<br>limit: 限制数量 | List<ReconOrderMainDO>: 待核账订单列表 | 查询指定日期的待核账订单（分页） |
| updateReconStatus(String, int) | orderNo: 订单号<br>reconStatus: 对账状态 | boolean: 更新结果 | 更新对账状态 |

### 7.2 Go SDK API

#### 7.2.1 EasyReconTemplate

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| DoRealtimeRecon(*entity.ReconOrderMain, []*entity.ReconOrderSplitSub) | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | bool: 对账结果<br>error: 错误信息 | 执行实时对账 |
| DoTimingRecon(string) | dateStr: 对账日期（yyyy-MM-dd） | bool: 对账结果<br>error: 错误信息 | 执行定时对账 |

### 7.3 Python SDK API

#### 7.3.1 EasyReconTemplate

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| do_realtime_recon(order_main, split_subs) | order_main: 订单主记录<br>split_subs: 分账子记录列表 | bool: 对账结果 | 执行实时对账 |
| do_timing_recon(date_str) | date_str: 对账日期（yyyy-MM-dd） | bool: 对账结果 | 执行定时对账 |

### 7.4 Node.js SDK API

#### 7.4.1 EasyReconTemplate

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| doRealtimeRecon(orderMain, splitSubs) | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | Promise<boolean>: 对账结果 | 执行实时对账 |
| doTimingRecon(dateStr) | dateStr: 对账日期（yyyy-MM-dd） | Promise<boolean>: 对账结果 | 执行定时对账 |

## 8. 部署指南

### 8.1 数据库准备

1. **创建数据库**：
   ```sql
   CREATE DATABASE easy_recon CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **创建用户**：
   ```sql
   CREATE USER 'recon_user'@'%' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON easy_recon.* TO 'recon_user'@'%';
   FLUSH PRIVILEGES;
   ```

3. **数据库配置建议**：
   - 开启 binlog 用于数据恢复
   - 配置合理的 innodb_buffer_pool_size（建议为服务器内存的 50-70%）
   - 启用慢查询日志，优化查询性能
   - 定期备份数据库

### 8.2 应用部署

#### 8.2.1 Java Spring Boot 应用

**标准部署**：

1. **打包应用**：
   ```bash
   mvn clean package -DskipTests
   ```

2. **运行应用**：
   ```bash
   java -jar target/your-application.jar
   ```

**Docker 部署**：

1. **创建 Dockerfile**：
   ```dockerfile
   FROM openjdk:11-jre-slim
   
   WORKDIR /app
   
   COPY target/your-application.jar app.jar
   
   EXPOSE 8080
   
   ENV SPRING_PROFILES_ACTIVE=prod
   
   CMD ["java", "-jar", "app.jar"]
   ```

2. **构建镜像**：
   ```bash
   docker build -t easy-recon-app .
   ```

3. **运行容器**：
   ```bash
   docker run -d --name easy-recon \
     -p 8080:8080 \
     -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/easy_recon \
     -e SPRING_DATASOURCE_USERNAME=recon_user \
     -e SPRING_DATASOURCE_PASSWORD=password \
     easy-recon-app
   ```

#### 8.2.2 Go 应用

**标准部署**：

1. **编译应用**：
   ```bash
   go build -o recon-app main.go
   ```

2. **运行应用**：
   ```bash
   ./recon-app
   ```

**Docker 部署**：

1. **创建 Dockerfile**：
   ```dockerfile
   FROM golang:1.18-alpine as builder
   
   WORKDIR /app
   
   COPY . .
   
   RUN go mod tidy
   RUN go build -o recon-app main.go
   
   FROM alpine:latest
   
   WORKDIR /app
   
   COPY --from=builder /app/recon-app .
   
   EXPOSE 8080
   
   CMD ["./recon-app"]
   ```

2. **构建镜像**：
   ```bash
   docker build -t easy-recon-go .
   ```

3. **运行容器**：
   ```bash
   docker run -d --name easy-recon-go \
     -p 8080:8080 \
     -e DB_HOST=mysql \
     -e DB_PORT=3306 \
     -e DB_USER=recon_user \
     -e DB_PASSWORD=password \
     -e DB_NAME=easy_recon \
     easy-recon-go
   ```

#### 8.2.3 Python 应用

**标准部署**：

1. **安装依赖**：
   ```bash
   pip install -r requirements.txt
   ```

2. **运行应用**：
   ```bash
   python main.py
   ```

**Docker 部署**：

1. **创建 Dockerfile**：
   ```dockerfile
   FROM python:3.9-slim
   
   WORKDIR /app
   
   COPY . .
   
   RUN pip install --no-cache-dir -r requirements.txt
   
   EXPOSE 8080
   
   CMD ["python", "main.py"]
   ```

2. **构建镜像**：
   ```bash
   docker build -t easy-recon-python .
   ```

3. **运行容器**：
   ```bash
   docker run -d --name easy-recon-python \
     -p 8080:8080 \
     -e DB_HOST=mysql \
     -e DB_PORT=3306 \
     -e DB_USER=recon_user \
     -e DB_PASSWORD=password \
     -e DB_NAME=easy_recon \
     easy-recon-python
   ```

#### 8.2.4 Node.js 应用

**标准部署**：

1. **安装依赖**：
   ```bash
   npm install
   ```

2. **运行应用**：
   ```bash
   node main.js
   ```

**Docker 部署**：

1. **创建 Dockerfile**：
   ```dockerfile
   FROM node:14-alpine
   
   WORKDIR /app
   
   COPY package*.json ./
   
   RUN npm install --no-cache
   
   COPY . .
   
   EXPOSE 8080
   
   CMD ["node", "main.js"]
   ```

2. **构建镜像**：
   ```bash
   docker build -t easy-recon-node .
   ```

3. **运行容器**：
   ```bash
   docker run -d --name easy-recon-node \
     -p 8080:8080 \
     -e DB_HOST=mysql \
     -e DB_PORT=3306 \
     -e DB_USER=recon_user \
     -e DB_PASSWORD=password \
     -e DB_NAME=easy_recon \
     easy-recon-node
   ```

#### 8.2.5 Docker Compose 部署

**创建 docker-compose.yml**：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: easy_recon
      MYSQL_USER: recon_user
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    restart: always

  easy-recon-java:
    build:
      context: ./sdk/spring-boot-starter
      dockerfile: Dockerfile
    container_name: easy-recon-java
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/easy_recon
      SPRING_DATASOURCE_USERNAME: recon_user
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - mysql
    restart: always

volumes:
  mysql_data:
```

**启动服务**：

```bash
docker-compose up -d
```

## 9. 监控与维护

### 9.1 监控指标

**核心业务指标**：
- **对账成功率**：成功对账的订单数 / 总订单数
- **异常率**：异常订单数 / 总订单数
- **对账延迟**：从订单产生到完成对账的时间
- **定时对账覆盖率**：定时对账处理的订单数 / 待对账订单总数

**系统性能指标**：
- **系统处理能力**：每秒可处理的订单数
- **响应时间**：对账操作的平均响应时间
- **线程池使用率**：线程池的使用情况
- **数据库连接池使用率**：数据库连接池的使用情况

**资源使用指标**：
- **CPU 使用率**：系统 CPU 使用率
- **内存使用率**：系统内存使用率
- **磁盘使用率**：系统磁盘使用率
- **网络带宽**：网络带宽使用情况

### 9.2 日志管理

**日志级别**：
- **DEBUG**：详细的调试信息
- **INFO**：普通的信息日志
- **WARN**：警告信息
- **ERROR**：错误信息
- **FATAL**：致命错误信息

**日志分类**：
- **对账日志**：记录对账过程和结果
- **异常日志**：记录对账过程中的异常
- **告警日志**：记录告警信息
- **性能日志**：记录系统性能指标
- **安全日志**：记录安全相关事件

**日志存储**：
- 本地文件存储
- 集中式日志管理（如 ELK Stack）
- 云服务日志存储

### 9.3 监控工具集成

**Prometheus + Grafana**：
- 采集和存储监控指标
- 可视化监控面板
- 设置告警规则

**ELK Stack**：
- 收集和分析日志
- 实时监控日志流
- 可视化日志分析结果

**Spring Boot Actuator**（Java 版本）：
- 暴露应用健康状态
- 提供性能指标
- 支持远程诊断

### 9.4 常见问题与解决方案

#### 9.4.1 数据库连接失败

**原因**：
- 数据库服务未启动
- 连接信息配置错误
- 网络连接问题
- 数据库连接池耗尽

**解决方案**：
- 检查数据库服务状态
- 验证连接信息
- 检查网络连接
- 调整连接池配置
- 增加连接池大小

#### 9.4.2 对账异常

**原因**：
- 金额不匹配
- 订单不存在
- 数据格式错误
- 网络延迟导致的时序问题

**解决方案**：
- 检查交易数据的准确性
- 验证订单是否存在
- 检查数据格式是否符合要求
- 增加重试机制
- 调整对账逻辑

#### 9.4.3 告警不触发

**原因**：
- 告警配置错误
- 网络连接问题
- 告警服务未启动
- 告警阈值设置不合理

**解决方案**：
- 检查告警配置
- 验证网络连接
- 检查告警服务状态
- 调整告警阈值
- 测试告警通道

#### 9.4.4 系统性能下降

**原因**：
- 数据库查询慢
- 线程池配置不合理
- 内存泄漏
- 网络带宽不足

**解决方案**：
- 优化数据库查询
- 添加索引
- 调整线程池配置
- 分析内存使用情况
- 增加系统资源

### 9.5 维护建议

**日常维护**：
- 定期检查系统日志
- 监控系统性能指标
- 备份数据库
- 更新依赖包

**周维护**：
- 分析对账异常原因
- 优化系统配置
- 清理过期数据
- 检查磁盘空间

**月维护**：
- 生成系统运行报告
- 进行性能测试
- 检查安全漏洞
- 规划系统升级

**季度维护**：
- 数据库优化
- 系统架构评估
- 新技术调研
- 制定改进计划

## 10. 版本历史

| 版本号 | 发布日期 | 主要变更 |
|--------|----------|----------|
| 1.0.0 | 2026-02-08 | 初始版本，支持 Java、Go、Python、Node.js 语言 |

## 11. 许可证

MIT License

## 12. 联系方式

如有问题或建议，欢迎联系我们：

- Email: ryan@example.com
- GitHub: https://github.com/example/easy-recon-sdk
