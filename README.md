# Easy Recon SDK

多语言支付对账 SDK，支持实时对账、定时核账、异常告警和自动建表等功能。

## 支持的语言版本

- **Java Spring Boot Starter** (`sdk/spring-boot-starter/`)
- **Go** (`sdk/go/`)
- **Python** (`sdk/python/`)
- **Node.js** (`sdk/node.js/`)

## 核心功能

1. **实时对账**：实时处理支付订单，确保交易数据的准确性
2. **定时核账**：定时对未对账订单进行批量处理
3. **异常告警**：对对账过程中的异常进行告警，支持多种告警方式
4. **自动建表**：使用 Flyway 自动创建数据库表结构
5. **多数据库支持**：支持 MySQL 和 PostgreSQL
6. **多语言支持**：提供 Java、Go、Python、Node.js 版本的 SDK

## 项目结构

```
easy-recon-sdk/
├── sdk/
│   ├── spring-boot-starter/  # Java Spring Boot 版本
│   ├── go/                   # Go 版本
│   ├── python/               # Python 版本
│   └── node.js/              # Node.js 版本
├── docs/                     # 共享文档
├── scripts/                  # 构建和发布脚本
└── README.md                 # 根目录 README
```

## 快速开始

### 1. Java Spring Boot Starter

#### 依赖配置

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.coffers</groupId>
    <artifactId>easy-recon-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 配置数据源

在 `application.yml` 中配置数据源：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/easy_recon
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

easy-recon:
  enabled: true
  alarm:
    type: log
  thread-pool:
    core-pool-size: 10
    max-pool-size: 20
```

#### 启用 SDK

在启动类上添加注解：

```java
@SpringBootApplication
@EnableEasyRecon
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### 使用示例

```java
@Autowired
private EasyReconTemplate easyReconTemplate;

// 执行实时对账
ReconOrderMainDO orderMain = new ReconOrderMainDO();
// 设置订单信息...
List<ReconOrderSplitSubDO> splitSubs = new ArrayList<>();
// 添加分账信息...
easyReconTemplate.doRealtimeRecon(orderMain, splitSubs);

// 执行定时对账
easyReconTemplate.doTimingRecon("2024-01-01");
```

### 2. Go SDK

#### 安装依赖

```bash
cd sdk/go
go mod tidy
```

#### 使用示例

```go
// 初始化数据库连接
db, err := sql.Open("mysql", "root:password@tcp(localhost:3306)/easy_recon")
if err != nil {
    log.Fatal(err)
}

// 创建方言
dialect := dialect.CreateDialect(db)

// 创建存储库
repo := repository.NewSQLReconRepository(db, dialect)

// 创建告警服务
alarmService := service.NewAlarmService(&service.LogAlarmStrategy{})

// 创建对账服务
realtimeReconService := service.NewRealtimeReconService(repo, alarmService)
timingReconService := service.NewTimingReconService(repo, alarmService)

// 创建模板
template := core.NewEasyReconTemplate(realtimeReconService, timingReconService)

// 执行实时对账
orderMain := &entity.ReconOrderMain{
    // 设置订单信息...
}
splitSubs := []*entity.ReconOrderSplitSub{}
// 添加分账信息...
template.DoRealtimeRecon(orderMain, splitSubs)

// 执行定时对账
template.DoTimingRecon("2024-01-01")
```

### 3. Python SDK

#### 安装依赖

```bash
cd sdk/python
pip install -r requirements.txt
```

#### 使用示例

```python
import mysql.connector
from repository.sql_recon_repository import SQLReconRepository
from dialect.recon_database_dialect import create_dialect
from service.alarm_service import AlarmService, LogAlarmStrategy
from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService
from core.easy_recon_template import EasyReconTemplate
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub

# 初始化数据库连接
connection = mysql.connector.connect(
    host="localhost",
    user="root",
    password="password",
    database="easy_recon"
)

# 创建方言
dialect = create_dialect(connection)

# 创建存储库
repo = SQLReconRepository(connection, dialect)

# 创建告警服务
alarm_service = AlarmService(LogAlarmStrategy())

# 创建对账服务
realtime_recon_service = RealtimeReconService(repo, alarm_service)
timing_recon_service = TimingReconService(repo, alarm_service)

# 创建模板
template = EasyReconTemplate(realtime_recon_service, timing_recon_service)

# 执行实时对账
order_main = ReconOrderMain()
# 设置订单信息...
split_subs = []
# 添加分账信息...
template.do_realtime_recon(order_main, split_subs)

# 执行定时对账
template.do_timing_recon("2024-01-01")
```

### 4. Node.js SDK

#### 安装依赖

```bash
cd sdk/node.js
npm install
```

#### 使用示例

```javascript
const mysql = require('mysql2/promise');
const SQLReconRepository = require('./repository/SQLReconRepository');
const { createDialect } = require('./dialect/ReconDatabaseDialect');
const { AlarmService, LogAlarmStrategy } = require('./service/AlarmService');
const RealtimeReconService = require('./service/RealtimeReconService');
const TimingReconService = require('./service/TimingReconService');
const EasyReconTemplate = require('./core/EasyReconTemplate');
const ReconOrderMain = require('./entity/ReconOrderMain');

async function main() {
    // 初始化数据库连接
    const connection = await mysql.createConnection({
        host: 'localhost',
        user: 'root',
        password: 'password',
        database: 'easy_recon'
    });

    // 创建方言
    const dialect = createDialect(connection);

    // 创建存储库
    const repo = new SQLReconRepository(connection, dialect);

    // 创建告警服务
    const alarmService = new AlarmService(new LogAlarmStrategy());

    // 创建对账服务
    const realtimeReconService = new RealtimeReconService(repo, alarmService);
    const timingReconService = new TimingReconService(repo, alarmService);

    // 创建模板
    const template = new EasyReconTemplate(realtimeReconService, timingReconService);

    // 执行实时对账
    const orderMain = new ReconOrderMain();
    // 设置订单信息...
    const splitSubs = [];
    // 添加分账信息...
    await template.doRealtimeRecon(orderMain, splitSubs);

    // 执行定时对账
    await template.doTimingRecon('2024-01-01');

    // 关闭连接
    await connection.end();
}

main().catch(console.error);
```

## 数据库表结构

- `recon_order_main`：对账订单主记录
- `recon_order_split_sub`：对账订单分账子记录
- `recon_exception`：对账异常记录
- `recon_notify_log`：对账通知日志
- `recon_rule`：对账规则

## 构建和发布

使用构建脚本构建所有语言版本的 SDK：

```bash
./scripts/build.sh --all
```

## 文档

详细文档请查看 `docs/` 目录。

## 版本历史

- **1.0.0**：初始版本，支持 Java、Go、Python、Node.js 语言

## 许可证

MIT
