# Node.js SDK API 参考

## 1. 核心模块

### 1.1 core/EasyReconTemplate

**描述**：SDK 的核心模板类，提供对账相关的核心方法。

#### 1.1.1 EasyReconTemplate

**描述**：SDK 的核心模板类，提供对账相关的核心方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `constructor(realtimeReconService, timingReconService)` | realtimeReconService: 实时对账服务<br>timingReconService: 定时核账服务 | - | 初始化模板实例 |
| `doRealtimeRecon(orderMain, splitSubs)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `Promise<boolean>`：对账结果 | 执行实时对账 |
| `doTimingRecon(dateStr)` | dateStr: 对账日期（yyyy-MM-dd） | `Promise<boolean>`：对账结果 | 执行定时对账 |

**使用示例**：

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

### 1.2 service/RealtimeReconService

**描述**：实时对账服务，处理实时对账逻辑。

#### 1.2.1 RealtimeReconService

**描述**：实时对账服务，处理实时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `constructor(repo, alarmService)` | repo: 存储库<br>alarmService: 告警服务 | - | 初始化服务实例 |
| `reconcile(orderMain, splitSubs)` | orderMain: 订单主记录<br>splitSubs: 分账子记录列表 | `Promise<boolean>`：对账结果 | 执行实时对账逻辑 |

### 1.3 service/TimingReconService

**描述**：定时核账服务，处理定时对账逻辑。

#### 1.3.1 TimingReconService

**描述**：定时核账服务，处理定时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `constructor(repo, alarmService)` | repo: 存储库<br>alarmService: 告警服务 | - | 初始化服务实例 |
| `reconcile(dateStr)` | dateStr: 对账日期（yyyy-MM-dd） | `Promise<boolean>`：对账结果 | 执行定时对账逻辑 |

### 1.4 service/AlarmService

**描述**：告警服务，处理对账过程中的异常告警。

#### 1.4.1 AlarmService

**描述**：告警服务，处理对账过程中的异常告警。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `constructor(alarmStrategy)` | alarmStrategy: 告警策略 | - | 初始化服务实例 |
| `alarm(exception)` | exception: 异常记录 | `Promise<void>` | 发送告警 |

#### 1.4.2 LogAlarmStrategy

**描述**：日志告警策略，将异常信息记录到日志。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `alarm(exception)` | exception: 异常记录 | `Promise<void>` | 发送日志告警 |

#### 1.4.3 DingTalkAlarmStrategy

**描述**：钉钉告警策略，通过钉钉机器人发送告警消息。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `constructor(webhookUrl)` | webhookUrl: 钉钉机器人 Webhook URL | - | 初始化策略实例 |
| `alarm(exception)` | exception: 异常记录 | `Promise<void>` | 发送钉钉告警 |

### 1.5 repository/SQLReconRepository

**描述**：SQL 实现的对账存储库，处理数据库操作。

#### 1.5.1 SQLReconRepository

**描述**：SQL 实现的对账存储库，处理数据库操作。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `constructor(connection, dialect)` | connection: 数据库连接<br>dialect: 数据库方言 | - | 初始化存储库实例 |
| `saveOrderMain(orderMain)` | orderMain: 订单主记录 | `Promise<boolean>`：保存结果 | 保存对账订单主记录 |
| `batchSaveOrderSplitSub(splitSubs)` | splitSubs: 分账子记录列表 | `Promise<boolean>`：保存结果 | 批量保存分账子记录 |
| `saveException(exception)` | exception: 异常记录 | `Promise<boolean>`：保存结果 | 保存异常记录 |
| `batchSaveException(exceptions)` | exceptions: 异常记录列表 | `Promise<boolean>`：保存结果 | 批量保存异常记录 |
| `getOrderMainByOrderNo(orderNo)` | orderNo: 订单号 | `Promise<ReconOrderMain>`：订单主记录 | 根据订单号查询对账订单主记录 |
| `getOrderSplitSubByOrderNo(orderNo)` | orderNo: 订单号 | `Promise<Array<ReconOrderSplitSub>>`：分账子记录列表 | 根据订单号查询分账子记录 |
| `getPendingReconOrders(dateStr, offset, limit)` | dateStr: 日期<br>offset: 偏移量<br>limit: 限制数量 | `Promise<Array<ReconOrderMain>>`：待核账订单列表 | 查询指定日期的待核账订单（分页） |
| `updateReconStatus(orderNo, reconStatus)` | orderNo: 订单号<br>reconStatus: 对账状态 | `Promise<boolean>`：更新结果 | 更新对账状态 |

### 1.6 dialect/ReconDatabaseDialect

**描述**：数据库方言模块，处理不同数据库的语法差异。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `createDialect(connection)` | connection: 数据库连接 | `ReconDatabaseDialect`：数据库方言实例 | 根据数据库连接创建对应的数据库方言实例 |

#### 1.6.1 ReconDatabaseDialect

**描述**：数据库方言接口，定义了数据库相关的方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `getInsertOrderMainSQL()` | 无 | `string`：插入订单主记录的 SQL 语句 | 获取插入订单主记录的 SQL 语句 |
| `getInsertOrderSplitSubSQL()` | 无 | `string`：插入分账子记录的 SQL 语句 | 获取插入分账子记录的 SQL 语句 |
| `getInsertExceptionSQL()` | 无 | `string`：插入异常记录的 SQL 语句 | 获取插入异常记录的 SQL 语句 |
| `getSelectOrderMainByOrderNoSQL()` | 无 | `string`：根据订单号查询订单主记录的 SQL 语句 | 获取根据订单号查询订单主记录的 SQL 语句 |
| `getSelectOrderSplitSubByOrderNoSQL()` | 无 | `string`：根据订单号查询分账子记录的 SQL 语句 | 获取根据订单号查询分账子记录的 SQL 语句 |
| `getSelectPendingReconOrdersSQL()` | 无 | `string`：查询待核账订单的 SQL 语句 | 获取查询待核账订单的 SQL 语句 |
| `getUpdateReconStatusSQL()` | 无 | `string`：更新对账状态的 SQL 语句 | 获取更新对账状态的 SQL 语句 |

#### 1.6.2 MySQLDialect

**描述**：MySQL 数据库方言实现。

**方法**：实现了 `ReconDatabaseDialect` 接口的所有方法。

#### 1.6.3 PostgreSQLDialect

**描述**：PostgreSQL 数据库方言实现。

**方法**：实现了 `ReconDatabaseDialect` 接口的所有方法。

## 2. 实体模块

### 2.1 entity/ReconOrderMain

**描述**：对账订单主记录实体类。

#### 2.1.1 ReconOrderMain

**描述**：对账订单主记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `number` | 主键 |
| `orderNo` | `string` | 订单号 |
| `merchantId` | `string` | 商户ID |
| `merchantName` | `string` | 商户名称 |
| `orderAmount` | `number` | 订单金额 |
| `actualAmount` | `number` | 实际金额 |
| `reconStatus` | `number` | 对账状态（0: 待对账, 1: 已对账, 2: 对账异常） |
| `orderTime` | `Date` | 订单时间 |
| `payTime` | `Date` | 支付时间 |
| `reconTime` | `Date` | 对账时间 |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

### 2.2 entity/ReconOrderSplitSub

**描述**：对账订单分账子记录实体类。

#### 2.2.1 ReconOrderSplitSub

**描述**：对账订单分账子记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `number` | 主键 |
| `orderNo` | `string` | 订单号 |
| `subOrderNo` | `string` | 分账子订单号 |
| `merchantId` | `string` | 商户ID |
| `splitAmount` | `number` | 分账金额 |
| `status` | `number` | 状态（0: 待处理, 1: 已处理, 2: 处理异常） |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

### 2.3 entity/ReconException

**描述**：对账异常记录实体类。

#### 2.3.1 ReconException

**描述**：对账异常记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `number` | 主键 |
| `orderNo` | `string` | 订单号 |
| `merchantId` | `string` | 商户ID |
| `exceptionType` | `number` | 异常类型（1: 金额不匹配, 2: 订单不存在, 3: 其他异常） |
| `exceptionMsg` | `string` | 异常消息 |
| `exceptionStep` | `number` | 异常步骤（1: 数据获取, 2: 数据匹配, 3: 状态更新） |
| `createTime` | `Date` | 创建时间 |
| `updateTime` | `Date` | 更新时间 |

## 3. 工具模块

### 3.1 util/reconUtil

**描述**：对账工具模块，提供对账相关的工具方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `isAmountMatch(expected, actual, tolerance)` | expected: 期望金额<br>actual: 实际金额<br>tolerance: 容差 | `boolean`：是否匹配 | 检查金额是否匹配 |
| `formatDate(dateObj, pattern)` | dateObj: 日期对象<br>pattern: 日期格式 | `string`：格式化后的日期 | 格式化日期 |
| `parseDate(dateStr, pattern)` | dateStr: 日期字符串<br>pattern: 日期格式 | `Date`：解析后的日期 | 解析日期字符串 |
| `generateOrderNo()` | 无 | `string`：订单号 | 生成订单号 |
| `generateSubOrderNo()` | 无 | `string`：分账子订单号 | 生成分账子订单号 |

### 3.2 util/encryptUtil

**描述**：加密工具模块，提供配置加密和解密方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `encrypt(plainText, secretKey)` | plainText: 明文<br>secretKey: 密钥 | `string`：加密后的字符串 | 加密字符串 |
| `decrypt(encryptedText, secretKey)` | encryptedText: 加密后的字符串<br>secretKey: 密钥 | `string`：解密后的明文 | 解密字符串 |

## 4. 配置

### 4.1 数据库配置

**示例**：

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

// 初始化数据库连接
const mysql = require('mysql2/promise');
const connection = await mysql.createConnection(dbConfig);
```

### 4.2 线程池配置

**示例**：

```javascript
// 线程池配置
const { Worker, isMainThread, parentPort, workerData } = require('worker_threads');
const os = require('os');

const threadCount = os.cpus().length;

// 使用 worker_threads 实现并发
function processOrders(orders) {
    return new Promise((resolve, reject) => {
        const results = [];
        let completed = 0;
        
        for (let i = 0; i < threadCount; i++) {
            const worker = new Worker('./worker.js', {
                workerData: {
                    orders: orders.slice(i * Math.ceil(orders.length / threadCount), (i + 1) * Math.ceil(orders.length / threadCount))
                }
            });
            
            worker.on('message', (message) => {
                results.push(...message);
                completed++;
                if (completed === threadCount) {
                    resolve(results);
                }
            });
            
            worker.on('error', reject);
        }
    });
}
```

### 4.3 告警配置

**示例**：

```javascript
// 告警配置
const alarmConfig = {
    type: 'log',
    dingtalk: {
        webhookUrl: 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN'
    }
};

// 使用日志告警
const { AlarmService, LogAlarmStrategy } = require('./service/AlarmService');

const alarmService = new AlarmService(new LogAlarmStrategy());

// 使用钉钉告警
const { DingTalkAlarmStrategy } = require('./service/AlarmService');

const dingtalkStrategy = new DingTalkAlarmStrategy(alarmConfig.dingtalk.webhookUrl);
const alarmService = new AlarmService(dingtalkStrategy);
```

## 5. 错误处理

**示例**：

```javascript
// 错误处理
try {
    const result = await template.doRealtimeRecon(orderMain, splitSubs);
    if (result) {
        console.log('对账成功');
    } else {
        console.log('对账失败');
    }
} catch (error) {
    // 处理异常
    console.error('对账异常:', error);
    // 记录日志
    logger.error('对账异常:', error);
}
```

## 6. 批量处理

**示例**：

```javascript
async function batchProcessOrders(template, orders) {
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
    
    return results;
}

// 使用示例
const orders = [];
for (let i = 0; i < 100; i++) {
    const order = new ReconOrderMain();
    order.orderNo = `ORDER_${Date.now()}_${i}`;
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

const results = await batchProcessOrders(template, orders);
console.log(`总处理订单数: ${results.length}`);
console.log(`成功数: ${results.filter(r => r).length}`);
console.log(`失败数: ${results.filter(r => !r).length}`);
```

## 7. 最佳实践

### 7.1 数据库连接管理

- 使用连接池管理数据库连接
- 及时关闭不再使用的连接
- 处理数据库连接异常

### 7.2 错误处理

- 使用 try-catch 捕获异常
- 记录详细的错误信息
- 区分不同类型的错误

### 7.3 异步处理

- 使用 async/await 处理异步操作
- 对于批量操作，使用 Promise.all 并发处理
- 合理设置并发数，避免系统过载

### 7.4 配置管理

- 使用配置文件或环境变量管理配置
- 对敏感配置进行加密
- 提供合理的默认值

### 7.5 日志管理

- 使用结构化日志
- 设置适当的日志级别
- 记录关键操作和错误信息

### 7.6 性能优化

- 使用批量操作减少数据库交互
- 合理使用缓存
- 优化查询语句
- 使用 worker_threads 处理 CPU 密集型任务
