# Easy Recon Go Demo

Easy Recon Go SDK 的演示程序，包含 11 个完整的对账场景。

## 前置条件

- Go 1.20+
- PostgreSQL 或 MySQL 数据库

## 运行

### 1. 配置数据库连接

编辑 `config.yaml`，修改数据库连接信息：

```yaml
database:
  driver: "postgres"   # 或 "mysql"
  dsn: "host=localhost user=postgres password=password dbname=easy_recon_demo port=5432 sslmode=disable"
```

MySQL DSN 格式：

```yaml
database:
  driver: "mysql"
  dsn: "root:password@tcp(localhost:3306)/easy_recon_demo?charset=utf8mb4&parseTime=True&loc=Local"
```

### 2. 运行 Demo

```bash
./run_demo.sh
```

## 演示场景

| # | 场景 | 说明 |
|---|------|------|
| 1 | 实时对账（成功） | 标准成功流程 |
| 2 | 实时对账（异常） | 金额不一致，触发告警 |
| 3 | 定时核账 | 批量处理待核账订单 |
| 4 | 退款对账 | 同步退款对账 |
| 5 | 异步退款对账 | 异步退款处理 |
| 6 | 通知对账 | 直接通知状态更新 |
| 7 | 按子订单号通知 | 通过子订单号查找并通知 |
| 8 | 按商户订单号通知 | 通过商户原始订单号查找并通知 |
| 9 | 按子订单号退款 | 通过子订单号查找并退款 |
| 10 | 按商户订单号退款 | 通过商户原始订单号查找并退款 |
| 11 | 查询状态和汇总 | 查询对账状态与统计汇总 |

## 预期输出

```
--- [Scenario] 1. Sync Realtime Recon (Success) ---
✅ Success. Order: ORD-SYNC-xxx

--- [Scenario] 2. Sync Realtime Recon (Exception) ---
❌ Failed. Order: ORD-ERR-xxx, Msg: 金额校验失败，实付金额与计算金额不一致

--- [Scenario] 3. Timing Recon ---
Timing Recon Executed. Success: true

...（后续场景类似）
```

> **注意**：场景 2 的 ❌ 是预期行为，用于演示金额不一致时的异常告警。

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `enabled` | `true` | 是否启用 SDK |
| `auto-init-tables` | `true` | 自动创建数据库表 |
| `amount-tolerance` | `0.01` | 金额校验容差 |
| `batch-size` | `500` | 批量处理大小 |
| `alarm.type` | `"log"` | 告警方式（log / dingtalk） |
