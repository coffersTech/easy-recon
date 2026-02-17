# Easy Recon Go SDK

Easy Recon Go SDK 是一个支付对账 SDK，提供实时对账、定时核账、退款对账、异常告警等功能，支持 MySQL 和 PostgreSQL。

## 项目结构

```
sdk/go/
├── api/          # API 接口定义
├── config/       # 配置管理
├── core/         # 核心模板 (EasyReconTemplate)
├── dialect/      # 数据库方言 (MySQL / PostgreSQL)
├── entity/       # 数据实体
├── repository/   # 数据存储层
└── service/      # 业务服务层
```

## 安装

```bash
go get github.com/coffersTech/easy-recon/sdk/go
```

## 快速开始

### 1. 初始化数据库连接

```go
import (
    "database/sql"
    _ "github.com/lib/pq"            // PostgreSQL
    // _ "github.com/go-sql-driver/mysql" // MySQL
)

db, err := sql.Open("postgres",
    "host=localhost user=postgres password=password dbname=easy_recon_demo sslmode=disable")
```

### 2. 创建 SDK 实例

```go
import (
    "github.com/coffersTech/easy-recon/sdk/go/config"
    "github.com/coffersTech/easy-recon/sdk/go/core"
    "github.com/coffersTech/easy-recon/sdk/go/dialect"
    "github.com/coffersTech/easy-recon/sdk/go/repository"
    "github.com/coffersTech/easy-recon/sdk/go/service"
)

cfg, _ := config.LoadConfig("config.yaml")
d := dialect.CreateDialect(db)
repo := repository.NewSQLReconRepository(db, d, cfg)
alarmSvc := service.NewAlarmService(&service.LogAlarmStrategy{})
realtimeSvc := service.NewRealtimeReconService(repo, alarmSvc, cfg)
timingSvc := service.NewTimingReconService(repo, alarmSvc, cfg)
template := core.NewEasyReconTemplate(realtimeSvc, timingSvc, repo, alarmSvc)
```

### 3. 执行对账

```go
// 实时对账
orderMain := &entity.ReconOrderMain{
    OrderNo:     "ORD-001",
    MerchantId:  "MCH-001",
    OrderAmount: decimal.NewFromFloat(100.00),
    // ...
}
splitSubs := []*entity.ReconOrderSplitSub{...}
result := template.DoRealtimeRecon(orderMain, splitSubs)

// 定时核账
template.DoTimingRecon("2024-01-01")

// 退款对账
template.DoReconRefund("ORD-001", refundSplitSubs)

// 通知对账
template.DoReconNotify("ORD-001", "SUB-001", 1, "success")
```

## 配置说明

通过 YAML 文件配置 SDK：

```yaml
easy-recon:
  enabled: true              # 是否启用
  auto-init-tables: true     # 自动建表
  amount-tolerance: 0.01     # 金额容差
  batch-size: 500            # 批量处理大小
  alarm:
    type: "log"              # 告警方式: log / dingtalk
```

## 数据库表

SDK 使用以下固定表名（与 Java SDK 一致）：

| 表名 | 说明 |
|------|------|
| `easy_recon_order_main` | 对账订单主记录 |
| `easy_recon_order_split_sub` | 对账订单分账子记录 |
| `easy_recon_order_refund_split_sub` | 退款分账子记录 |
| `easy_recon_exception` | 对账异常记录 |
| `easy_recon_summary` | 对账汇总统计 |
| `easy_recon_notify_log` | 对账通知日志 |

开启 `auto-init-tables: true` 后 SDK 会自动建表。

## 支持的数据库

- **MySQL** 5.7+
- **PostgreSQL** 10+

SDK 通过 `dialect.CreateDialect(db)` 自动检测数据库类型并使用对应的 SQL 语法。

## 许可证

MIT
