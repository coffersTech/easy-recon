# Node.js 集成指南

## 环境要求

- Node.js 14.x 或更高版本
- npm 6.x 或更高版本
- MySQL 5.7+ 或 PostgreSQL 10+

## 集成步骤

### 1. 创建项目

```bash
mkdir my-recon-app
cd my-recon-app
npm init -y
```

### 2. 安装 SDK

```bash
npm install @cofferstech/easy-recon-sdk
npm install mysql2 pg dotenv
```

### 3. 配置数据源

创建配置文件 `config/config.js`：

```javascript
require('dotenv').config();

class DBConfig {
  constructor() {
    this.host = process.env.DB_HOST || 'localhost';
    this.port = parseInt(process.env.DB_PORT || '3306');
    this.user = process.env.DB_USER || 'root';
    this.password = process.env.DB_PASSWORD || 'password';
    this.database = process.env.DB_DATABASE || 'easy_recon';
    this.connectionLimit = parseInt(process.env.DB_CONNECTION_LIMIT || '10');
  }

  getConnectionParams() {
    return {
      host: this.host,
      user: this.user,
      password: this.password,
      database: this.database,
      port: this.port,
      connectionLimit: this.connectionLimit
    };
  }
}

class AlarmConfig {
  constructor() {
    this.type = process.env.ALARM_TYPE || 'log';
    this.dingtalk = {
      webhookUrl: process.env.DINGTALK_WEBHOOK || ''
    };
  }
}

const dbConfig = new DBConfig();
const alarmConfig = new AlarmConfig();

module.exports = {
  dbConfig,
  alarmConfig
};
```

创建 `.env` 文件：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=password
DB_DATABASE=easy_recon
DB_CONNECTION_LIMIT=10

# 告警配置
ALARM_TYPE=log
DINGTALK_WEBHOOK=
```

### 4. 初始化 SDK

创建初始化文件 `src/init.js`：

```javascript
const mysql = require('mysql2/promise');
const { EasyReconApi, ReconConfig } = require('@cofferstech/easy-recon-sdk');
const { dbConfig, alarmConfig } = require('./config/config'); // 假设 config.js 保持不变

async function initReconService() {
  """初始化对账服务"""
  try {
    // 1. 连接数据库
    const connection = await mysql.createConnection(dbConfig.getConnectionParams());
    
    // 2. 创建配置
    const config = new ReconConfig({
        enabled: true,
        alarm: {
            type: alarmConfig.type,
            dingtalk: alarmConfig.dingtalk
        }
    });

    // 3. 创建 API 实例
    const api = new EasyReconApi(config, connection);
    
    console.log('Recon service initialized successfully');
    return api;
  } catch (error) {
    console.error('Failed to initialize recon service:', error);
    throw error;
  }
}

module.exports = {
  initReconService
};
```

// 创建主文件 `src/main.js`：

```javascript
const { initReconService } = require('./init');
const { ReconOrderMain, ReconOrderSplitSub } = require('@cofferstech/easy-recon-sdk');

async function main() {
  """主函数"""
  try {
    // 1. 初始化 SDK
    const api = await initReconService();
    
    // 2. 测试实时对账
    console.log('Testing real-time reconciliation...');
    await testRealtimeRecon(api);
    
    // 3. 测试定时对账
    console.log('Testing timing reconciliation...');
    await testTimingRecon(api);
    
    console.log('Integration test completed');
  } catch (error) {
    console.error('Error:', error);
  }
}

async function testRealtimeRecon(api) {
  """测试实时对账"""
  try {
    // 创建订单主记录
    // 注意：根据 SDK 版本，构造函数参数可能有所不同，请参考 entity 定义
    const orderMain = new ReconOrderMain();
    orderMain.orderNo = `ORDER_${Date.now()}`;
    // ... 设置其他属性
    
    const splitSubs = [];
    // ...
    
    // 执行对账 (API 方法名为 reconOrder)
    const result = await api.reconOrder(orderMain, splitSubs);
    if (result) {
      console.log('Real-time recon succeeded');
    } else {
      console.log('Real-time recon failed');
    }
  } catch (error) {
    console.error('Error in real-time recon:', error);
  }
}

async function testTimingRecon(template) {
  """测试定时对账"""
  try {
    // 执行定时对账
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const dateStr = yesterday.toISOString().split('T')[0];
    
    const result = await api.runTimingRecon(dateStr);
    if (result) {
      console.log('Timing recon succeeded');
    } else {
      console.log('Timing recon failed');
    }
  } catch (error) {
    console.error('Error in timing recon:', error);
  }
}

// 运行主函数
main();
```

### 7. 异步处理示例

```javascript
async function asyncProcessOrders(template, orders) {
  """异步处理多个订单"""
  try {
    const results = await Promise.all(
      orders.map(async (order) => {
        // 创建分账子记录
        const splitSubs = [
          new ReconOrderSplitSub(
            order.orderNo,
            `SUB_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            order.merchantId,
            order.orderAmount * 0.8,
            0,
            new Date(),
            new Date()
          )
        ];
        
        // 执行对账
        return template.doRealtimeRecon(order, splitSubs);
      })
    );
    
    return results;
  } catch (error) {
    console.error('Error in async process:', error);
    throw error;
  }
}

async function testAsyncProcessing(template) {
  """测试异步处理"""
  try {
    // 创建多个订单
    const orders = [];
    for (let i = 0; i < 5; i++) {
      const order = new ReconOrderMain(
        `ORDER_${Date.now()}_${i}`,
        'MERCHANT_001',
        '测试商户',
        100.00,
        100.00,
        0,
        new Date(),
        new Date(),
        new Date(),
        new Date()
      );
      orders.push(order);
    }
    
    // 异步处理
    const results = await asyncProcessOrders(template, orders);
    console.log('Async processing results:', results);
    console.log('Success count:', results.filter(r => r).length);
    console.log('Failure count:', results.filter(r => !r).length);
  } catch (error) {
    console.error('Error in async test:', error);
  }
}
```

## 配置管理

### 1. 环境变量配置

| 环境变量 | 描述 | 默认值 |
|---------|------|-------|
| DB_HOST | 数据库主机 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_USER | 数据库用户 | root |
| DB_PASSWORD | 数据库密码 | password |
| DB_DATABASE | 数据库名称 | easy_recon |
| DB_CONNECTION_LIMIT | 连接池大小 | 10 |
| ALARM_TYPE | 告警类型（log 或 dingtalk） | log |
| DINGTALK_WEBHOOK | 钉钉告警 Webhook URL | - |

### 2. 配置文件

也可以使用 JSON 配置文件：

```javascript
// config.json
{
  "database": {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "password",
    "database": "easy_recon",
    "connectionLimit": 10
  },
  "alarm": {
    "type": "log",
    "dingtalk": {
      "webhookUrl": ""
    }
  }
}

// 加载配置
const config = require('./config.json');
const dbHost = config.database.host;
// 其他配置...
```

## 错误处理

### 1. 基本错误处理

```javascript
try {
  const template = await initReconService();
  const result = await template.doRealtimeRecon(orderMain, splitSubs);
  if (result) {
    console.log('Recon succeeded');
  } else {
    console.log('Recon failed');
  }
} catch (error) {
  console.error('Error:', error);
  // 处理错误，例如重试或告警
}
```

### 2. 自定义错误

定义自定义错误以便更好地处理不同类型的错误：

```javascript
class ReconError extends Error {
  constructor(message) {
    super(message);
    this.name = 'ReconError';
  }
}

class DatabaseError extends ReconError {
  constructor(message) {
    super(message);
    this.name = 'DatabaseError';
  }
}

class AmountMismatchError extends ReconError {
  constructor(message) {
    super(message);
    this.name = 'AmountMismatchError';
  }
}

class OrderNotFoundError extends ReconError {
  constructor(message) {
    super(message);
    this.name = 'OrderNotFoundError';
  }
}

// 错误处理示例
try {
  const result = await template.doRealtimeRecon(orderMain, splitSubs);
} catch (error) {
  if (error instanceof DatabaseError) {
    console.error('Database error:', error.message);
  } else if (error instanceof AmountMismatchError) {
    console.error('Amount mismatch:', error.message);
  } else if (error instanceof OrderNotFoundError) {
    console.error('Order not found:', error.message);
  } else if (error instanceof ReconError) {
    console.error('Recon error:', error.message);
  } else {
    console.error('Unknown error:', error);
  }
}
```

## 监控与维护

### 1. 日志管理

使用 winston 进行日志管理：

```javascript
const winston = require('winston');

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' })
  ]
});

// 添加控制台输出
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.simple()
  }));
}

// 记录对账结果
function logReconResult(orderNo, success, error) {
  const logData = {
    timestamp: new Date().toISOString(),
    orderNo,
    success,
    error: error ? error.message : null
  };
  
  if (success) {
    logger.info('Reconciliation succeeded', logData);
  } else {
    logger.error('Reconciliation failed', logData);
  }
}

// 使用示例
try {
  const result = await template.doRealtimeRecon(orderMain, splitSubs);
  logReconResult(orderMain.orderNo, result);
} catch (error) {
  logReconResult(orderMain.orderNo, false, error);
}
```

### 2. 健康检查

实现健康检查函数，监控系统状态：

```javascript
const mysql = require('mysql2/promise');
const { dbConfig } = require('../config/config');

async function healthCheck() {
  """健康检查"""
  try {
    // 检查数据库连接
    const pool = mysql.createPool(dbConfig.getConnectionParams());
    const [rows] = await pool.execute('SELECT 1');
    await pool.end();
    const dbStatus = 'ok';
    
    return {
      status: 'ok',
      timestamp: new Date().toISOString(),
      components: {
        database: dbStatus
      }
    };
  } catch (error) {
    return {
      status: 'error',
      timestamp: new Date().toISOString(),
      error: error.message,
      components: {
        database: `error: ${error.message}`
      }
    };
  }
}

// 使用示例
async function testHealthCheck() {
  const result = await healthCheck();
  console.log(JSON.stringify(result, null, 2));
}
```

## 部署

### 1. 直接运行

```bash
# 设置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=password
export DB_DATABASE=easy_recon

# 运行应用
npm start
```

### 2. Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM node:14-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install --only=production

COPY . .

EXPOSE 8080

ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_USER=root
ENV DB_PASSWORD=password
ENV DB_DATABASE=easy_recon

CMD ["npm", "start"]
```

创建 `.dockerignore` 文件：

```
nodemodules
.npm
.env
Dockerfile
.dockerignore
```

构建并运行 Docker 容器：

```bash
# 构建镜像
docker build -t recon-app .

# 运行容器
docker run -d --name recon-app \
  -p 8080:8080 \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  -e DB_DATABASE=easy_recon \
  recon-app
```

### 3. PM2 部署

使用 PM2 管理 Node.js 应用：

```bash
# 安装 PM2
npm install -g pm2

# 启动应用
pm run build
pm run start:prod

# 或者直接使用 PM2
pm run build
pm run start:prod
```

## 常见问题与解决方案

### 1. 数据库连接失败

**原因**：
- 数据库服务未启动
- 连接信息配置错误
- 网络连接问题

**解决方案**：
- 检查数据库服务状态
- 验证连接信息
- 检查网络连接
- 增加连接超时设置

### 2. 对账结果不准确

**原因**：
- 数据格式错误
- 金额计算错误
- 并发访问导致数据不一致

**解决方案**：
- 检查数据格式
- 验证金额计算
- 使用事务保证数据一致性
- 增加数据验证

### 3. 性能问题

**原因**：
- 连接池配置不合理
- 数据库查询慢
- 并发度不够

**解决方案**：
- 调整连接池大小
- 优化数据库索引
- 增加并发度
- 使用缓存减少数据库访问

### 4. 告警不触发

**原因**：
- 告警配置错误
- 网络问题
- 告警服务未启动

**解决方案**：
- 检查告警配置
- 验证网络连接
- 测试告警通道
- 增加告警日志

## 最佳实践

1. **使用连接池**：配置合理的数据库连接池大小
2. **错误处理**：使用 try-catch 捕获并处理异常
3. **异步处理**：使用 async/await 处理异步操作
4. **监控告警**：配置合适的告警方式，及时发现异常
5. **配置管理**：使用环境变量或配置文件管理配置
6. **健康检查**：实现健康检查函数，监控系统状态
7. **日志管理**：使用结构化日志，便于分析问题
8. **性能测试**：在生产环境部署前进行性能测试
9. **备份数据**：定期备份数据库，防止数据丢失
10. **代码组织**：按照功能模块组织代码，提高可维护性

## 示例项目

完整的示例项目代码可参考：
- GitHub: https://github.com/example/easy-recon-sdk-examples