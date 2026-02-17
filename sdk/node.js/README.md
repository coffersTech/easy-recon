# Easy Recon SDK for Node.js

多商户支付对账 SDK Node.js 版本，提供统一的标准接口，支持 MySQL 和 PostgreSQL 数据库。

## 特性

*   **多数据库支持**: 内置 MySQL 和 PostgreSQL 方言。
*   **全场景覆盖**: 支持实时对账、退款对账、异步通知回调、定时全量核账。
*   **易于集成**: 零配置即可运行，提供开箱即用的 API Facade。
*   **告警机制**: 集成钉钉告警和日志告警。

## 安装

```bash
npm install @cofferstech/easy-recon-sdk
```

## 快速开始

### 1. 初始化

```javascript
const { EasyReconApi } = require('@cofferstech/easy-recon-sdk');
const mysql = require('mysql2/promise'); // 或 require('pg');

// 数据库连接 (以 mysql2 为例)
const connection = await mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: 'password',
  database: 'easy_recon_demo'
});

// 配置
const config = {
  enabled: true,
  amountTolerance: '0.01',
  alarm: {
    type: 'dingtalk',
    dingtalk: {
      webhookUrl: 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN'
    }
  }
};

// 实例化 SDK
const sdk = new EasyReconApi(config, connection);

// 初始化表结构 (可选)
const ddl = sdk.getCreateTableSQL();
for (const sql of ddl) {
  await connection.execute(sql);
}
```

### 2. 实时对账

```javascript
const { ReconOrderMain, ReconOrderSplitSub } = require('@cofferstech/easy-recon-sdk');

// 构造主订单
const main = new ReconOrderMain();
main.orderNo = 'ORD001';
main.merchantId = 'MCH001';
main.orderAmount = '100.00';
main.payAmount = '100.00';
main.payStatus = 1; // SUCCESS
// ... 其他字段

// 构造分账详情
const sub = new ReconOrderSplitSub();
sub.subOrderNo = 'SUB001';
sub.splitAmount = '100.00';

// 执行对账
await sdk.reconOrder(main, [sub]);
```

## License

MIT
